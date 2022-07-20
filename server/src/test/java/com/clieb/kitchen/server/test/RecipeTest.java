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
package com.clieb.kitchen.server.test;

import com.clieb.kitchen.server.model.Recipe;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.StreamingHttpClient;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

/**
 *
 * @author Chris
 */
@MicronautTest
public class RecipeTest {

    @Inject
    EmbeddedApplication<?> application;
    private static EmbeddedServer server;
    private static StreamingHttpClient client;

    @BeforeEach
    public void setupServer() {
        server = application.getApplicationContext().getBean(EmbeddedServer.class);
        client = server
                .getApplicationContext().createBean(StreamingHttpClient.class, server.getURL());
    }

    @AfterEach
    public void stopServer() {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void allRecipesHasARecipe() {
        Flux.from(client.jsonStream(HttpRequest.GET("/api/recipe/all"), Argument.of(Recipe.class)))
                .doOnEach(sc -> {
                    Recipe recipe = sc.get();
                    if (recipe != null) {
                        String title = recipe.title();
                        String url = recipe.url();
                        Assertions.assertNotNull(title);
                        Assertions.assertNotNull(url);
                    }
                })
                .buffer()
                .blockLast();
    }
}
