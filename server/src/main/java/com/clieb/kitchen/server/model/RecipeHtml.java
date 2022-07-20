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

import com.clieb.kitchen.server.service.RecipeService;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import io.micronaut.core.annotation.Nullable;
import reactor.core.publisher.Mono;

/**
 *
 * @author chris
 */
@MappedEntity(value = "RECIPE_HTML")
        public record RecipeHtml(
        @GeneratedValue
        @Id
        Long recipeId,
        Long htmlRunId,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Nullable
        String compressedHtml,
        String url,
        @DateCreated
        Timestamp creationDate,
        @DateUpdated
        Timestamp lastModified)
        implements com.clieb.kitchen.database.model.RecipeHtml {

    public static record RecipeData(Long recipeId, Recipe recipe,
            List<RecipeIngredient> recipeIngredients,
            List<RecipeInstruction> recipeInstructions,
            List<RecipeNote> recipeNotes) {

    }

    public RecipeData extractRecipe() {
        Recipe recipe = null;
        String html = "";
        try ( InputStream recipeHtmlStream = new ByteArrayInputStream(Base64.getDecoder()
                .decode(this.compressedHtml()));  GZIPInputStream gis = new GZIPInputStream(recipeHtmlStream)) {
            html = new String(gis.readAllBytes());
        } catch (IOException ex) {
            Logger.getLogger(RecipeData.class.getName()).log(Level.SEVERE, null, ex);
        }

        String headline = "";
        List<String> ingredients = new ArrayList<>();
        List<String> instructions = new ArrayList<>();
        List<RecipeIngredient> recipeIngredients = new ArrayList<>();
        List<RecipeInstruction> recipeInstructions = new ArrayList<>();
        List<RecipeNote> recipeNotes = new ArrayList<>();
        HashMap<String, String> metaItems = new HashMap<>();
        Document doc = Jsoup.parse(html);
        Elements headlineWrappers = doc.getElementsByClass("headline-wrapper");
        if (headlineWrappers != null && headlineWrappers.first() != null) {
            Element headlineWrapper = headlineWrappers.first();
            Elements titleElements = headlineWrapper.getElementsByTag("h1");
            // Elements headlineElements = doc.getElementsByClass("headline heading-content");
            if (titleElements != null && !titleElements.isEmpty()) {
                headline = titleElements.first().text();
                String author = "unknown";
                Elements authorNameElements = doc.getElementsByClass("author-name");
                if (authorNameElements != null && authorNameElements.first() != null) {
                    author = authorNameElements.first() == null ? author : authorNameElements.first().text();
                }

                Double rating = null;
                Elements ratingStarTextElements = doc.getElementsByClass("review-star-text");
                if (ratingStarTextElements != null && ratingStarTextElements.first() != null && ratingStarTextElements.first().text() != null) {
                    String ratingText = ratingStarTextElements.first().text().replace("Rating: ", "").replace(" stars", "");
                    if (!"Unrated".equals(ratingText)) {
                        try {
                            rating = Double.parseDouble(ratingText);
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                }

                StringBuilder summaryStringBuilder = new StringBuilder();
                Elements recipeSummaryElements = doc.getElementsByClass("recipe-summary");
                int numSummaryElements = 0;
                if (recipeSummaryElements != null && recipeSummaryElements.select("p") != null) {
                    for (Element summaryElement : recipeSummaryElements.select("p")) {
                        if (summaryElement.text() != null) {
                            if (numSummaryElements > 0) {
                                summaryStringBuilder.append(" ");
                            }
                            summaryStringBuilder.append(summaryElement.text());
                            numSummaryElements++;
                        }
                    }
                }
                String summary = summaryStringBuilder.toString();

                Elements ingredientsElements = doc.getElementsByClass("ingredients-item-name");
                ingredientsElements.forEach(a -> ingredients.add(a.text()));
                // instructions-section__fieldset
                Elements instructionsElements = doc.getElementsByClass("instructions-section__fieldset");
                instructionsElements.select("p").forEach(i -> instructions.add(i.text()));
                Elements recipeMetaElements = doc.getElementsByClass("recipe-meta-item");
                recipeMetaElements.forEach(a -> {
                    String itemHeader = a.getElementsByClass("recipe-meta-item-header").first().text();
                    String itemBody = a.getElementsByClass("recipe-meta-item-body").first().text();
                    metaItems.put(itemHeader, itemBody);
                    recipeNotes.add(new RecipeNote(null, this.recipeId(), itemHeader, itemBody, null, null));
                });
                recipe = new Recipe(this.recipeId(), headline, this.url(), author, rating, summary, null, null);
                RecipeIngredient recipeIngredient = null;
                for (String ingredient : ingredients) {
                    recipeIngredient = new RecipeIngredient(null,
                            recipe.recipeId(),
                            ingredient,
                            null, null,
                            null, null, null, null);
                    recipeIngredient = recipeIngredient.processRecipeIngredient();
                    recipeIngredients.add(recipeIngredient);
                }
                int i = 0;
                for (String instruction : instructions) {
                    recipeInstructions.add(new RecipeInstruction(null,
                            recipe.recipeId(),
                            ++i,
                            instruction,
                            null,
                            null));
                }
            }
        }
        return new RecipeData(this.recipeId(), recipe, recipeIngredients, recipeInstructions, recipeNotes);
    }

    public static String compressString(String input) {
        ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
        try (
                 GZIPOutputStream gzipStream = new GZIPOutputStream(compressedOutputStream);  ByteArrayInputStream gzipInputStream = new ByteArrayInputStream((input == null ? "" : input).getBytes())) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) != -1) {
                gzipStream.write(buffer, 0, len);
            }
            gzipStream.finish();
        } catch (IOException ex) {
            Logger.getLogger(RecipeService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Base64.getEncoder().encodeToString(compressedOutputStream.toByteArray());
    }

    public static String decompressString(String compressed) {

        String decompressedHtml = null;
        try ( InputStream recipeHtmlStream = new ByteArrayInputStream(Base64.getDecoder()
                .decode(compressed));  GZIPInputStream gis = new GZIPInputStream(recipeHtmlStream)) {
            decompressedHtml = new String(gis.readAllBytes());
        } catch (IOException ex) {
            Logger.getLogger(RecipeData.class.getName()).log(Level.SEVERE, null, ex);
            // throw ex;
        }
        return decompressedHtml;
    }

    public static RecipeHtml createRecipeHtml(Long htmlRunId, String url, String compressedHtml) {
        String html = "";
        return new RecipeHtml(null, htmlRunId, compressedHtml, url, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
    }

    @Override
    public String html() {
        return decompressString(compressedHtml);
    }
}
