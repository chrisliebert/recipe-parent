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
package com.clieb.kitchen.frontend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.*;
import java.sql.Timestamp;

/**
 *
 * @author Chris
 */
public record Recipe(@GeneratedValue
        Long recipeId,
        String title,
        String url,
        String author,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Nullable
        Double rating,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Nullable
        String summary,
        @DateCreated
        Timestamp creationDate,
        @DateUpdated
        Timestamp lastModified)
        implements com.clieb.kitchen.database.model.Recipe {

}
