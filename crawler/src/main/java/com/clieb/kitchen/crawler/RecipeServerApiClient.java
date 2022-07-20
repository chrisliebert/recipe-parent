
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
package com.clieb.kitchen.crawler;

import com.clieb.kitchen.crawler.RecipeServerApiLowLevelClient.CompressedUrlIdPair;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.CONTENT_TYPE;
import static io.micronaut.http.HttpHeaders.USER_AGENT;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import java.sql.Timestamp;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 *
 * @author chris
 */
@Client(RecipeServerConfiguration.API_RECIPE_URL)
@Header(name = USER_AGENT, value = "Micronaut HTTP Client")
@Header(name = ACCEPT, value = "application/json")
@Header(name = CONTENT_TYPE, value = "application/json")
public interface RecipeServerApiClient {

    public static record Recipe(Long recipeId,
            String title,
            String url,
            String author,
            Double rating,
            String summary,
            Timestamp creationDate,
            Timestamp lastModified) implements com.clieb.kitchen.database.model.Recipe {

    }

    public static record RecipeIngredient(Long recipeIngredientId,
            Long recipeId,
            String description,
            Timestamp creationDate,
            Timestamp lastModified,
            String baseDescription,
            String quantityDescription,
            String quantityAmount,
            String quantityUom) implements com.clieb.kitchen.database.model.RecipeIngredient {

    }

    public static record RecipeInstruction(Long recipeInstructionId,
            Long recipeId,
            Integer stepNumber,
            String text,
            Timestamp creationDate,
            Timestamp lastModified) implements com.clieb.kitchen.database.model.RecipeInstruction {

    }

    public static record RecipeHtml(Long recipeId,
            Long htmlRunId,
            String html,
            String compressedHtml,
            String url,
            Timestamp creationDate,
            Timestamp lastModified) implements com.clieb.kitchen.database.model.RecipeHtml {

    }

    @Post("/html/create")
    Mono<String> postRecipeHtml(final RecipeHtml recipeHtml);

    @Post("/create")
    Mono<HttpResponse<Recipe>> postRecipe(final Recipe recipe);

    @Post("/ingredient/create")
    Mono<HttpResponse<RecipeIngredient>> postRecipeIngredient(final RecipeIngredient recipeIngredient);

    @Post("/instruction/create")
    Mono<HttpResponse<RecipeInstruction>> postRecipeInstruction(final RecipeInstruction recipeInstruction);

    @Post("/html/run/new")
    Mono<HttpResponse<Long>> getNewRunId();

    @Post("/crawl/url")
    Mono<HttpResponse<Long>> processRecipeURL(final Long htmlRunId, final String url, final String compressedHtml);

    @Get("/crawl/url/visited/")
    Mono<List<CompressedUrlIdPair>> getVisitedURLs();
}












