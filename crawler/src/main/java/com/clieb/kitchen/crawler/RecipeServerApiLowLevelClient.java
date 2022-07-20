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

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import jakarta.inject.Singleton;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.CONTENT_TYPE;
import static io.micronaut.http.HttpHeaders.USER_AGENT;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import java.util.List;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 *
 * @author chris
 */
@Singleton
public class RecipeServerApiLowLevelClient implements RecipeServerApiClient {

    private final HttpClient httpClient;
    private final UriBuilder urlBuilder;

    public RecipeServerApiLowLevelClient(@Client(RecipeServerConfiguration.API_RECIPE_URL) HttpClient httpClient,
            RecipeServerConfiguration configuration) {
        this.httpClient = httpClient;
        urlBuilder = UriBuilder
                .of(RecipeServerConfiguration.API_RECIPE_URL)
                .path(RecipeServerConfiguration.API_RECIPE_PREFIX);
    }

    @Override
    public Mono<HttpResponse<Recipe>> postRecipe(final Recipe recipe) {
        HttpRequest<Recipe> req = HttpRequest.POST(
                urlBuilder.path("/create")
                        .build()
                        .toString(),
                recipe
        )
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        Publisher<HttpResponse<Recipe>> responsePublisher = httpClient.exchange(req, Recipe.class);
        return Mono.from(responsePublisher);
    }

    @Override
    public Mono<HttpResponse<RecipeIngredient>> postRecipeIngredient(RecipeIngredient recipeIngredient) {
        HttpRequest<RecipeIngredient> req = HttpRequest.POST(
                UriBuilder
                        .of(RecipeServerConfiguration.API_RECIPE_URL)
                        .path(RecipeServerConfiguration.API_RECIPE_PREFIX + "/ingredient/create")
                        .build()
                        .toString(),
                recipeIngredient
        )
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON);

        Publisher<HttpResponse<RecipeIngredient>> responsePublisher = httpClient.exchange(req, RecipeIngredient.class);
        return Mono.from(responsePublisher);
    }

    @Override
    public Mono<HttpResponse<RecipeInstruction>> postRecipeInstruction(RecipeInstruction recipeInstruction) {
        HttpRequest<RecipeInstruction> req = HttpRequest.POST(
                UriBuilder
                        .of(RecipeServerConfiguration.API_RECIPE_URL)
                        .path(RecipeServerConfiguration.API_RECIPE_PREFIX + "/instruction/create")
                        .build()
                        .toString(),
                recipeInstruction
        )
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        Publisher<HttpResponse<RecipeInstruction>> responsePublisher = httpClient.exchange(req, RecipeInstruction.class);
        return Mono.from(responsePublisher);
    }

    @Override
    public Mono<String> postRecipeHtml(RecipeHtml recipeHtml) {
        HttpRequest<RecipeHtml> req = HttpRequest.POST(
                UriBuilder
                        .of(RecipeServerConfiguration.API_RECIPE_URL)
                        .path(RecipeServerConfiguration.API_RECIPE_PREFIX + "/html/create")
                        .build()
                        .toString(),
                recipeHtml
        )
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        //Publisher<String> responsePublisher = httpClient.retrieve(req, Argument.of(String.class));
        Publisher<String> p = httpClient.retrieve(req);
        return Mono.from(p);
    }

    @Override
    public Mono<HttpResponse<Long>> getNewRunId() {
        HttpRequest<Long> req = HttpRequest.POST(
                UriBuilder
                        .of(RecipeServerConfiguration.API_RECIPE_URL)
                        .path(RecipeServerConfiguration.API_RECIPE_PREFIX + "/html/run/new")
                        .build()
                        .toString(),
                -1L
        )
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        //Publisher<String> responsePublisher = httpClient.retrieve(req, Argument.of(String.class));
        Publisher<Long> p = httpClient.retrieve(req, Argument.of(Long.class));
        return Mono.from(p).map(l -> HttpResponse.ok(l));
    }

    public static record CompressedUrlIdPair(String compressedUrl, Long recipeId) {

    }

    @Override
    public Mono<List<CompressedUrlIdPair>> getVisitedURLs() {
        HttpRequest<Object> req = HttpRequest.GET(UriBuilder
                .of(RecipeServerConfiguration.API_RECIPE_URL)
                .path(RecipeServerConfiguration.API_RECIPE_PREFIX + "/crawl/url/visited/")
                .build()
                .toString())
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        Publisher<List<CompressedUrlIdPair>> p = httpClient.retrieve(req, Argument.listOf(CompressedUrlIdPair.class));
        return Mono.from(p);
    }

    private static record SingleUrlParameter(Long htmlRunId, String url, String compressedHtml) {

    }

    @Override
    public Mono<HttpResponse<Long>> processRecipeURL(final Long htmlRunId, final String url, final String compressedHtml) {
        HttpRequest<SingleUrlParameter> req = HttpRequest.POST(
                UriBuilder
                        .of(RecipeServerConfiguration.API_RECIPE_URL)
                        .path(RecipeServerConfiguration.API_RECIPE_PREFIX + "/crawl/url")
                        .build()
                        .toString(),
                new SingleUrlParameter(htmlRunId, url, compressedHtml)
        )
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        Publisher<Long> p = httpClient.retrieve(req, Argument.of(Long.class));
        return Mono.from(p).map(l -> HttpResponse.ok(l));
    }
}


































