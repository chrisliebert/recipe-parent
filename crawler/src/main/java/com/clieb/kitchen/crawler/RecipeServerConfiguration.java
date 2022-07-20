

/* Copyright 2022 Chris Liebert
 * 
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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

/**
 *
 * @author chris
 */
@ConfigurationProperties(RecipeServerConfiguration.API_RECIPE_PREFIX)
@Requires(property = RecipeServerConfiguration.API_RECIPE_PREFIX)
public class RecipeServerConfiguration {

    public static final String API_RECIPE_PREFIX = "/api/recipe";
    public static final String API_RECIPE_URL = "http://localhost:8080";

    
//    private String organization;
//    private String repo;
//    private String username;
//    private String token;
//
//    public String getOrganization() {
//        return organization;
//    }
//
//    public void setOrganization(String organization) {
//        this.organization = organization;
//    }
//
//    public String getRepo() {
//        return repo;
//    }
//
//    public void setRepo(String repo) {
//        this.repo = repo;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getToken() {
//        return token;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }
}
