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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@MicronautTest
class RecipeServerTest {

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
    public void testItWorks() {
        Assertions.assertNotNull(server);
        Assertions.assertNotNull(client);
    }

    @Test
    public void getSweetCornCakes() {
        List<Recipe> searchResults = client.toBlocking()
                .retrieve(HttpRequest.GET("/api/recipe/find/title/Sweet%20Corn%20Cakes"), Argument.listOf(Recipe.class));
        Assertions.assertTrue(searchResults.size() <= 1);
        searchResults.forEach(recipe -> {
            String title = recipe.title();
            String url = recipe.url();
            Assertions.assertNotNull(title);
            Assertions.assertNotNull(url);
        });
    }
}
