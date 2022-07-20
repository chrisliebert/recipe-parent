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

import com.clieb.kitchen.frontend.model.Recipe;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 *
 * @author Chris
 */
public class RecipeSubscriber implements Subscriber<Recipe> {

    Alert alert;
    private ObservableList<Recipe> listItems;
    private Subscription recipeSubscription = null;
    private final long bufferSize = 1L;

    public RecipeSubscriber(Alert alert, ObservableList<Recipe> listItems) {
        this.alert = alert;
        this.listItems = listItems;
    }

    public void cancel() {
        if (recipeSubscription != null) {
            recipeSubscription.cancel();
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        cancel();
        this.recipeSubscription = s;
        Platform.runLater(() -> {
            listItems.clear();
            s.request(bufferSize);
        });
    }

    @Override
    public void onNext(Recipe t) {
        Platform.runLater(() -> {
            listItems.add(t);
            recipeSubscription.request(bufferSize);
        });
    }

    @Override
    public void onError(Throwable t) {
        Platform.runLater(() -> {
            if (!alert.isShowing()) {
                alert.setTitle("Error Fetching Results");
                alert.setHeaderText("Check Server");
                alert.setContentText(t.getLocalizedMessage());
                alert.show();
            }
        });
    }

    @Override
    public void onComplete() {
        Platform.runLater(() -> {
            if (listItems.isEmpty()) {
                Alert infoAlert = new Alert(AlertType.INFORMATION);
                infoAlert.setTitle("Not Found");
                infoAlert.setHeaderText("No results found.");
                infoAlert.setContentText("Search failed.");
                infoAlert.showAndWait();
            }
        });
    }
};
