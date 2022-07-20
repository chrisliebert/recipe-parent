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

import com.clieb.kitchen.server.model.Recipe;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author Chris
 */
@R2dbcRepository(dialect = Dialect.MYSQL)
public interface RecipeRepository extends ReactiveStreamsCrudRepository<Recipe, Long> {

    @Override
    Mono<Recipe> findById(@NonNull Long id);

    @Override
    Flux<Recipe> findAll();

    Mono<Recipe> findByTitle(@NonNull String title);

    Mono<Recipe> findByUrl(@NonNull String url);

    @Query("""  
SELECT
    imatches.RECIPE_ID
    ,imatches.TITLE
    ,imatches.URL
    ,imatches.AUTHOR
    ,imatches.RATING
    ,imatches.SUMMARY
    ,imatches.CREATION_DATE
    ,imatches.LAST_MODIFIED
    ,count(1) num_in_inventory
FROM  (
SELECT
    R.RECIPE_ID
   ,R.TITLE
   ,R.URL
   ,R.AUTHOR
   ,R.RATING
   ,R.SUMMARY
   ,R.CREATION_DATE
   ,R.LAST_MODIFIED
FROM RECIPE R, RECIPE_INGREDIENT RE, INVENTORY_INGREDIENT II
WHERE RE.RECIPE_ID = R.RECIPE_ID
AND UPPER(RE.BASE_DESCRIPTION) IN (UPPER(II.SINGULAR_DESCRIPTION), UPPER(II.PLURAL_DESCRIPTION))
)imatches
GROUP BY imatches.RECIPE_ID, imatches.TITLE, imatches.URL, imatches.AUTHOR
, imatches.RATING, imatches.SUMMARY, imatches.CREATION_DATE, imatches.LAST_MODIFIED
HAVING COUNT(1) = (
    SELECT COUNT(1) 
           FROM RECIPE R, RECIPE_INGREDIENT RE
           WHERE RE.RECIPE_ID = R.RECIPE_ID)
    """)
    Flux<Recipe> findFromInventory();
}
