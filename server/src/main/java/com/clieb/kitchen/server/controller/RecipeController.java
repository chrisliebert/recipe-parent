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
package com.clieb.kitchen.server.controller;

/**
 *
 * @author Chris
 */
import com.clieb.kitchen.server.model.Recipe;
import com.clieb.kitchen.server.model.RecipeHtml;
import com.clieb.kitchen.server.model.RecipeHtml.RecipeData;
import com.clieb.kitchen.server.model.RecipeIngredient;
import com.clieb.kitchen.server.model.RecipeInstruction;
import com.clieb.kitchen.server.service.CrawlService;
import com.clieb.kitchen.server.service.RecipeService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import jakarta.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller("/api/recipe/")
public class RecipeController {

    @Inject
    private RecipeService recipeService;

    @Inject
    private CrawlService crawlService;

    @Get("/all")
    Flux<Recipe> all() {
        return recipeService.findAll().sort((a, b) -> a.title().compareToIgnoreCase(b.title()));
    }

    @Get("/count")
    Mono<Long> count() {
        return recipeService.count();
    }

    @Get("/find/id/{recipeId}")
    Mono<Recipe> get(@PathVariable("recipeId") Long recipeId) {
        return recipeService.findById(recipeId);
    }

    @Get("/extract/{recipeId}")
    Mono<RecipeData> extract(@PathVariable("recipeId") Long recipeId) {
        return recipeService.extract(recipeId);
    }

    @Get("/get/title/{title}")
    Mono<Recipe> getByTitle(String title) throws UnsupportedEncodingException {
        title = URLDecoder.decode(title, StandardCharsets.UTF_8.name());
        return recipeService.findByTitle(title);
    }

    @Get("/find/title/{title}")
    Flux<Recipe> findLikeTitle(String title) throws UnsupportedEncodingException {
        final String decodedTitle = URLDecoder.decode(title, StandardCharsets.UTF_8.name()).toUpperCase();
        return recipeService.findAll().filter(p -> p.title().toUpperCase().contains(decodedTitle)).sort((a, b) -> a.title().compareToIgnoreCase(b.title()));
    }

    @Get("/ingredient/recipe/{recipeId}")
    Flux<RecipeIngredient> getRecipeIngredients(@PathVariable("recipeId") Long recipeId) {
        return recipeService.getRecipeIngredients(recipeId);
    }

    @Get("/ingredient/all")
    Flux<RecipeIngredient> getRecipeIngredientsAll() {
        return recipeService.getRecipeIngredientAll();
    }

    @Get("/instruction/recipe/{recipeId}")
    Flux<RecipeInstruction> getRecipeInstructions(@PathVariable("recipeId") Long recipeId) {
        return recipeService.getRecipeInstructions(recipeId);
    }

    @Post(value = "/html/create")
    Mono<HttpResponse<String>> createRecipeHtml(RecipeHtml recipeHtml) {
        recipeService.loadRecipeHtml(recipeHtml);
        return Mono.just(HttpResponse.ok("Recipe processing " + recipeHtml.url()));
    }

    @Get("/html/{recipeId}")
    @Produces(MediaType.TEXT_HTML)
    Mono<String> getRecipeHtml(@PathVariable("recipeId") Long recipeId) {
        return recipeService.getRecipeHtml(recipeId);
    }

    @Post(value = "/html/run/new")
    Mono<Long> createNewRunId() {
        return recipeService.createNewHtmlRunId();
    }

    @Get(value = "/html/process/all")
    Flux<String> processAllRecipeHtml() {
        return recipeService.processAllRecipeHtml().retry(10000).onErrorMap(ex
                -> {
            Logger.getLogger(RecipeData.class.getName()).log(Level.SEVERE, null, ex);
            return ex;
        });
    }

    @Get(value = "/html/process/{recipeId}")
    Flux<String> processRecipeHtml(@PathVariable("recipeId") Long recipeId) {
        return recipeService.processRecipeHtml(recipeId);
    }

    @Post(value = "/create")
    Mono<HttpResponse<Recipe>> createRecipe(Recipe recipe) {
        //System.out.println("Create recipe " + recipe.title());
        return Mono.just(HttpResponse.ok(recipeService.loadRecipe(recipe)));
    }

    @Post(value = "/ingredient/create")
    Mono<HttpResponse<RecipeIngredient>> createRecipeIngredient(RecipeIngredient recipeIngredient) {
        //System.out.println("Create RecipeIngredient " + recipeIngredient.description());
        return Mono.just(HttpResponse.ok(recipeService.loadRecipeIngredient(recipeIngredient)));
    }

    @Post(value = "/instruction/create")
    Mono<HttpResponse<RecipeInstruction>> createRecipeInstruction(RecipeInstruction recipeInstruction) {
        //System.out.println("Create RecipeInstruction " + recipeInstruction.text());
        return Mono.just(HttpResponse.ok(recipeService.loadRecipeInstruction(recipeInstruction)));
    }

    @Get(value = "/crawl/url/visited/")
    Flux<CrawlService.CompressedUrlIdPair> getVisitedURLsCompressed() {
        return crawlService.getVisitedURLsCompressed();
    }

    @Post(value = "/crawl/url")
    Mono<Long> processUrlCrawl(Long htmlRunId, String url, String compressedHtml) {
        //System.out.println("Process url " + url);
        return crawlService.processUrlCrawl(htmlRunId, url, compressedHtml);
    }
}
