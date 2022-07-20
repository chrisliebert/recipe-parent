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
package com.clieb.kitchen.frontend.controller;

import com.clieb.kitchen.frontend.RecipeJavaFXApplication;
import com.clieb.kitchen.frontend.subscriber.RecipeInstructionSubscriber;
import com.clieb.kitchen.frontend.subscriber.RecipeIngredientSubscriber;
import com.clieb.kitchen.frontend.subscriber.RecipeSubscriber;
import com.clieb.kitchen.frontend.model.RecipeIngredient;
import com.clieb.kitchen.frontend.model.RecipeInstruction;
import com.clieb.kitchen.frontend.model.Recipe;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.StreamingHttpClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class PrimaryController implements Initializable, ChangeListener<Recipe> {

    private final String serverUrl = "http://localhost:8080";

    @FXML
    private TextField recipeSearchTextField;

    @FXML
    private Button recipeSearchButton;

    @FXML
    private TableView<Recipe> recipeResultsTable;

    @FXML
    private TableColumn<Recipe, String> recipeTitleColumn, recipeUrlColumn;

    @FXML
    private ListView<RecipeIngredient> ingredientsListView;

    @FXML
    private FlowPane instructionFlowPane;

    @FXML
    private MenuItem preferencesMenuItem;

    @FXML
    private MenuItem quitMenuItem;

    @FXML
    private void switchToSecondary() {
        try {
            RecipeJavaFXApplication.setRoot("preferences");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    Alert errorAlert = null;
    private ObservableList<Recipe> listItems = null;
    private ObservableList<RecipeIngredient> recipeIngredientListItems = null;

    private RecipeSubscriber recipeSubscriber;
    private RecipeIngredientSubscriber recipeIngredientSubscriber;
    private RecipeInstructionSubscriber recipeInstructionSubscriber;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listItems = recipeResultsTable.getItems();
        recipeIngredientListItems = ingredientsListView.getItems();
        recipeResultsTable.setPlaceholder(new Label("No search conducted."));
        recipeTitleColumn.setCellValueFactory((TableColumn.CellDataFeatures<Recipe, String> param)
                -> new SimpleStringProperty(param.getValue().title()));
        recipeUrlColumn.setCellValueFactory((TableColumn.CellDataFeatures<Recipe, String> param)
                -> new SimpleStringProperty(param.getValue().url()));
        recipeSearchButton.setOnAction(eh -> loadResults());
        recipeSearchTextField.setOnAction(eh -> loadResults());
        recipeResultsTable.getSelectionModel().selectedItemProperty().addListener(this);
        preferencesMenuItem.setOnAction(eh -> switchToSecondary());
        quitMenuItem.setOnAction(eh -> Platform.exit());
        errorAlert = new Alert(Alert.AlertType.ERROR);
        recipeSubscriber = new RecipeSubscriber(errorAlert, listItems);
        recipeIngredientSubscriber = new RecipeIngredientSubscriber(errorAlert, recipeIngredientListItems);
        recipeInstructionSubscriber = new RecipeInstructionSubscriber(errorAlert, instructionFlowPane);
    }

    private void loadResults() {
        searchByTitle();
    }

    private void searchByTitle() {
        final String searchTerm;
        try {
            searchTerm = URLEncoder
                    .encode(recipeSearchTextField.getText()
                            .replaceAll(" ", "%20"),
                            StandardCharsets.UTF_8.toString());
            if (searchTerm != null) {
                HttpRequest<Recipe> request = HttpRequest.GET(searchTerm.isEmpty() ? "/api/recipe/all" : "/api/recipe/find/title/" + searchTerm);
                URL url = new URL(serverUrl);
                StreamingHttpClient
                        .create(url)
                        .jsonStream(request, Argument.of(Recipe.class))
                        .subscribe(recipeSubscriber);
            }
        } catch (UnsupportedEncodingException | MalformedURLException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(ex.getClass() == null ? "Exception" : ex.getClass().getName());
                alert.setContentText(ex.getLocalizedMessage());
                alert.showAndWait();
            });
        }
    }

    private void loadIngredients(final Long recipeId) {
        if (recipeId != null) {
            HttpRequest<Object> request = HttpRequest.GET("/api/recipe/ingredient/recipe/" + recipeId);
            try {
                URL url = new URL(serverUrl);
                StreamingHttpClient
                        .create(url)
                        .jsonStream(request, Argument.of(RecipeIngredient.class))
                        .subscribe(recipeIngredientSubscriber
                        );

            } catch (MalformedURLException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error Loading Ingredients");
                    alert.setHeaderText(ex.getClass() == null ? "Exception" : ex.getClass().getName());
                    alert.setContentText(ex.getLocalizedMessage());
                    alert.showAndWait();
                });
            }
        }
    }

    private void loadInstructions(final Long recipeId) {
        if (recipeId != null) {

            HttpRequest<Object> request = HttpRequest.GET("/api/recipe/instruction/recipe/" + recipeId);
            try {
                URL url = new URL(serverUrl);
                StreamingHttpClient
                        .create(url)
                        .jsonStream(request, Argument.of(RecipeInstruction.class))
                        .subscribe(
                                recipeInstructionSubscriber
                        );

            } catch (MalformedURLException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error Loading Instructions");
                    alert.setHeaderText(ex.getClass() == null ? "Exception" : ex.getClass().getName());
                    alert.setContentText(ex.getLocalizedMessage());
                    alert.showAndWait();
                });
            }
        }
    }

    @Override
    public void changed(ObservableValue<? extends Recipe> ov, Recipe t, Recipe t1) {
        final Long recipeId = t1 == null ? null : t1.recipeId();
        if (recipeId != null) {
            Platform.runLater(() -> {
                loadIngredients(recipeId);
                loadInstructions(recipeId);
            });
        }
    }
}
