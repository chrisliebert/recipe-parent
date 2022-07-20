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
package com.clieb.kitchen.server.repository;

import com.clieb.kitchen.server.model.RecipeHtml;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import javax.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author chris
 */
@R2dbcRepository(dialect = Dialect.MYSQL)
public interface RecipeHtmlRepository extends ReactiveStreamsCrudRepository<RecipeHtml, Long> {

    @Override
    Mono<RecipeHtml> findById(@NotNull Long recipeId);

    @Override
    Flux<RecipeHtml> findAll();

    Flux<RecipeHtml> findByUrl(@NotNull String url);

    Mono<Boolean> existsByUrl(@NotNull String url);
}
