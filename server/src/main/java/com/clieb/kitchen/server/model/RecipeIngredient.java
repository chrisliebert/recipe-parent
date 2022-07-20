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
package com.clieb.kitchen.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.micronaut.data.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;
import io.micronaut.core.annotation.Nullable;

/**
 *
 * @author Chris
 */
@MappedEntity(value = "RECIPE_INGREDIENT")
        public record RecipeIngredient(
        @GeneratedValue @Id Long recipeIngredientId,
        Long recipeId,
        String description,
        @DateCreated Timestamp creationDate,
        @DateUpdated Timestamp lastModified,
        @JsonInclude(Include.ALWAYS)
        @Nullable String baseDescription,
        @JsonInclude(Include.ALWAYS)
        @Nullable String quantityDescription,
        @JsonInclude(Include.ALWAYS)
        @Nullable String quantityAmount,
        @JsonInclude(Include.ALWAYS)
        @Nullable String quantityUom)
        implements com.clieb.kitchen.database.model.RecipeIngredient {

    public RecipeIngredient processRecipeIngredient() {
        int uomLocation = -1;
        String uom = "";
        for (String possibleUom : new String[]{"pound","pounds", "tablespoon", "tablespoons", "teaspoon", "teaspoons", "fluid ounces", "fluid ounce", "ounce","ounces", "cup", "cups"}) {
            uomLocation = description.toLowerCase().indexOf(possibleUom + " ");
            if (uomLocation > -1) {
                uomLocation += uom.length();
                uom = possibleUom;
                break;
            }
        }
        String quantityDescription = uomLocation == -1 ? "" : description.substring(0, uomLocation + uom.length());
        String baseDescription = uomLocation == -1 ? description : description.substring(uomLocation + uom.length()).trim();
        String amountStr = uomLocation == -1 ? "" : quantityDescription.replace(uom, "").trim();
        try {
            amountStr = Double.toString(Double.parseDouble(amountStr));
        } catch (NumberFormatException nfe) {
            amountStr = "";
        }

        //  String uom =  = uomLocation == -1? "": description : description.substring(0, uomLocation);
        RecipeIngredient extractedIngredient = new RecipeIngredient(this.recipeIngredientId(),
                this.recipeId(), description, this.creationDate(), Timestamp.from(Instant.now()),
                baseDescription, quantityDescription, amountStr, uom);

        return extractedIngredient;
    }
}
