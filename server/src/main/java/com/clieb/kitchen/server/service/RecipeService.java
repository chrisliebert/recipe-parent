/* Copyright 2022 Chris Liebert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.clieb.kitchen.server.service;

import com.clieb.kitchen.server.model.Recipe;
import com.clieb.kitchen.server.model.RecipeHtml;
import com.clieb.kitchen.server.model.RecipeHtml.RecipeData;
import com.clieb.kitchen.server.model.RecipeHtmlRun;
import com.clieb.kitchen.server.model.RecipeIngredient;
import com.clieb.kitchen.server.model.RecipeInstruction;
import com.clieb.kitchen.server.repository.RecipeIngredientRepository;
import com.clieb.kitchen.server.repository.RecipeInstructionRepository;
import com.clieb.kitchen.server.repository.RecipeNoteRepository;
import com.clieb.kitchen.server.repository.RecipeRepository;
import com.clieb.kitchen.server.repository.RecipeHtmlRepository;
import com.clieb.kitchen.server.repository.RecipeHtmlRunRepository;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author Chris
 */
@Singleton
public class RecipeService {

    @Inject
    private RecipeHtmlRepository recipeHtmlRepository;

    @Inject
    private RecipeHtmlRunRepository recipeHtmlRunRepository;

    @Inject
    private RecipeRepository recipeRepository;

    @Inject
    private RecipeIngredientRepository recipeIngredientRepository;

    @Inject
    private RecipeInstructionRepository recipeInstructionRepository;

    @Inject
    private RecipeNoteRepository recipeNoteRepository;

    @Inject
    private TaskScheduler taskScheduler;

    public Flux<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public Mono<Long> count() {
        return Mono.from(recipeRepository.count());
    }

    public Mono<Recipe> findById(final Long recipeId) {
        return recipeRepository.findById(recipeId);
    }

    public Mono<RecipeData> extract(final Long recipeId) {
        return recipeHtmlRepository.findById(recipeId)
                .map(rh -> rh.extractRecipe());
    }

    public Mono<Recipe> findByTitle(final String title) {
        return recipeRepository.findByTitle(title);
    }

    public Flux<RecipeIngredient> getRecipeIngredients(Long recipeId) {
        return recipeIngredientRepository.findByRecipeId(recipeId);
    }

    public Flux<RecipeInstruction> getRecipeInstructions(Long recipeId) {
        return recipeInstructionRepository.findByRecipeId(recipeId);
    }

    public Flux<RecipeIngredient> getRecipeIngredientAll() {
        return recipeIngredientRepository.findAll();
    }

    public Flux<String> processRecipeHtml(final Mono<RecipeHtml> recipeHtml) {
        return Flux.from(recipeHtml).map(r -> r.extractRecipe())
                .filter(rd -> rd.recipe() != null)
                .map(ed
                        -> {
                    taskScheduler.schedule(Duration.ZERO, () -> {
                        Mono.from(recipeRepository.save(ed.recipe())).block();
                        Flux.from(recipeIngredientRepository.saveAll(ed.recipeIngredients())).blockLast();
                        Flux.from(recipeInstructionRepository.saveAll(ed.recipeInstructions())).blockLast();
                        Flux.from(recipeNoteRepository.saveAll(ed.recipeNotes())).blockLast();
                        //Runtime.getRuntime().gc();
                    });
                    return ed.recipe().url();
                });

    }

    public Flux<String> processRecipeHtml(final Long recipeId) {
        return processRecipeHtml(recipeHtmlRepository.findById(recipeId));
    }

    public Flux<String> processAllRecipeHtml() {
        return recipeHtmlRepository.findAll()
                .buffer(4)
                .flatMap(rhl -> {
                    Flux<String> q = Flux.empty();
                    for (RecipeHtml rh : rhl) {
                        q = q.mergeWith(processRecipeHtml(Mono.just(rh)));
                    }
                    return q;
                });
    }

    public void loadRecipeHtml(final RecipeHtml recipeHtml) {
        Publisher<Boolean> recipeExistsPublisher = recipeHtmlRepository.existsByUrl(recipeHtml.url());
        recipeExistsPublisher.subscribe(new Subscriber<Boolean>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1L);
            }

            @Override
            public void onNext(Boolean recipeExists) {
                if (recipeExists) {
                    // Logger.getLogger(RecipeService.class.getName()).log(Level.INFO, "Skipping existing recipe {0}", recipeHtml.url());
                } else {
                    recipeHtmlRepository.save(recipeHtml).subscribe(new Subscriber<RecipeHtml>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(1L);
                        }

                        @Override
                        public void onNext(RecipeHtml t) {
                            //  Logger.getLogger(RecipeService.class.getName()).log(Level.INFO, "Saved {0} : {1}", new Object[]{recipeHtml.recipeId(), recipeHtml.url()});
                            processRecipeHtml(Mono.just(t)).subscribe();
                            // .subscribe(s -> Logger.getLogger(RecipeService.class.getName()).log(Level.INFO, "Saved {0}", s));
                        }

                        @Override
                        public void onError(Throwable t) {
                            Logger.getLogger(RecipeService.class.getName()).log(Level.INFO, t.getMessage(), t.fillInStackTrace());
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable t) {
                Logger.getLogger(RecipeService.class.getName()).log(Level.SEVERE, t.getMessage(), t.fillInStackTrace());
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public Recipe loadRecipe(final Recipe recipe) {
        Recipe theRecipe = recipeRepository.findByUrl(recipe.url()).block();
        if (theRecipe == null) {
            theRecipe = Mono.from(recipeRepository.save(recipe)).block();
        } else {
            theRecipe = Mono.from(recipeRepository.update(new Recipe(theRecipe.recipeId(), theRecipe.title(), theRecipe.url(), theRecipe.author(),
                    theRecipe.rating(), theRecipe.summary(), theRecipe.creationDate(), Timestamp.from(Instant.now())))).block();
        }
        return theRecipe;
    }

    public RecipeIngredient loadRecipeIngredient(final RecipeIngredient recipeIngredient) {
        RecipeIngredient theIngredient = Mono.from(recipeIngredientRepository.findById(recipeIngredient.recipeIngredientId())).block();
        if (theIngredient == null) {
            theIngredient = Mono.from(recipeIngredientRepository.save(recipeIngredient)).block();
        } else {
            theIngredient = Mono.from(recipeIngredientRepository.update(
                    new RecipeIngredient(theIngredient.recipeIngredientId(), theIngredient.recipeId(), theIngredient.description(),
                            theIngredient.creationDate(), Timestamp.from(Instant.now()), theIngredient.baseDescription(),
                            theIngredient.quantityDescription(), theIngredient.quantityAmount(), theIngredient.quantityUom()))).block();
        }
        return theIngredient;
    }

    public RecipeInstruction loadRecipeInstruction(final RecipeInstruction recipeInstruction) {
        RecipeInstruction theInstruction = Mono.from(recipeInstructionRepository.findById(recipeInstruction.recipeInstructionId())).block();
        if (theInstruction == null) {
            theInstruction = Mono.from(recipeInstructionRepository.save(recipeInstruction)).block();
        } else {
            theInstruction = Mono.from(recipeInstructionRepository.update(
                    new RecipeInstruction(theInstruction.recipeInstructionId(), theInstruction.recipeId(), theInstruction.stepNumber(),
                            theInstruction.text(), theInstruction.creationDate(), theInstruction.lastModified()))).block();
        }
        return theInstruction;

    }

    public Mono<String> getRecipeHtml(final Long recipeHtmlId) {
        return recipeHtmlRepository.findById(recipeHtmlId)
                .map(rh -> rh.html());
    }

    public Mono<Long> createNewHtmlRunId() {
        return Mono.from(recipeHtmlRunRepository.save(new RecipeHtmlRun(null, null))).map(rhri -> rhri.recipeHtmlRunId());
    }

    public Flux<Recipe> findFromInventory() {
        return recipeRepository.findFromInventory();
    }
}
