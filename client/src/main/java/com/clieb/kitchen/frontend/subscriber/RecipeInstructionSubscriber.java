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

import com.clieb.kitchen.frontend.model.RecipeInstruction;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 *
 * @author Chris
 */
public class RecipeInstructionSubscriber implements Subscriber<RecipeInstruction> {

    Alert alert;
    private Subscription recipeInstructionSubscription = null;
    final private long bufferSize = 1L;
    private FlowPane instructionFlowPane = null;

    public RecipeInstructionSubscriber(Alert alert, FlowPane instructionFlowPane) {
        this.alert = alert;
        this.instructionFlowPane = instructionFlowPane;
    }

    public void cancel() {
        if (recipeInstructionSubscription != null) {
            recipeInstructionSubscription.cancel();
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        cancel();
        recipeInstructionSubscription = s;
        Platform.runLater(() -> {
            this.recipeInstructionSubscription = s;
            instructionFlowPane.getChildren().clear();
            recipeInstructionSubscription.request(bufferSize);
        });
    }

    @Override
    public void onNext(RecipeInstruction t) {
        Platform.runLater(() -> {
            HBox hbox = new HBox();
            Label stepNumber = new Label(t.stepNumber().toString());
            stepNumber.setPadding(new Insets(10));
            stepNumber.setScaleX(2);
            stepNumber.setScaleY(2);
            TextArea text = new TextArea();
            text.setMaxHeight(120);
            text.setPadding(new Insets(5));
            String formattedText = t.text();
            text.setText(formattedText);
            text.setWrapText(true);
            text.setEditable(false);
            hbox.getChildren().add(stepNumber);
            hbox.getChildren().add(text);
            hbox.setPadding(new Insets(5));
            Pane pane = new Pane();
            pane.getChildren().add(hbox);
            instructionFlowPane.getChildren().add(pane);
        });
        recipeInstructionSubscription.request(bufferSize);
    }

    @Override
    public void onError(Throwable t) {
        Platform.runLater(() -> {
            if (!alert.isShowing()) {
                alert.setTitle("Error Loading Instructions");
                alert.setHeaderText("Check Server");
                alert.setContentText(t.getLocalizedMessage());
                alert.show();
            }
        });
    }

    @Override
    public void onComplete() {
    }
}
