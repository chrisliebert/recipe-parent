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
package com.clieb.kitchen.frontend.subscriber;

import com.clieb.kitchen.frontend.model.RecipeIngredient;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 *
 * @author Chris
 */
public class RecipeIngredientSubscriber implements Subscriber<RecipeIngredient> {
    Alert alert;
    ObservableList<RecipeIngredient> recipeIngredientListItems;
    private Subscription ingredientSubscription = null;
    private final long bufferSize = 1L;

    public RecipeIngredientSubscriber(Alert alert, ObservableList<RecipeIngredient> recipeIngredientListItems) {
        this.alert = alert;
        this.recipeIngredientListItems = recipeIngredientListItems;
    }

    public void cancel() {
        if (ingredientSubscription != null) {
            ingredientSubscription.cancel();
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        cancel();
        ingredientSubscription = s;
        Platform.runLater(() -> {
            recipeIngredientListItems.clear();
            ingredientSubscription = s;
            ingredientSubscription.request(bufferSize);
        });
    }

    @Override
    public void onNext(RecipeIngredient t) {
        Platform.runLater(() -> {
            recipeIngredientListItems.add(t);
            ingredientSubscription.request(bufferSize);
        });
    }

    @Override
    public void onError(Throwable t) {
        Platform.runLater(() -> {
            //Alert alert = new Alert(Alert.AlertType.ERROR);
            if (!alert.isShowing()) {
                alert.setTitle("Error Loading Ingredients");
                alert.setHeaderText("Check Server");
                alert.setContentText(t.getLocalizedMessage());
                alert.show();
            }
        });
    }

    @Override
    public void onComplete() {
    }
};
