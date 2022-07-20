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
package com.clieb.kitchen.server.service;

import com.clieb.kitchen.server.model.RecipeHtml;
import com.clieb.kitchen.server.repository.RecipeHtmlRepository;
import com.clieb.kitchen.server.repository.RecipeHtmlRunRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author chris
 */
@Singleton
public class CrawlService {

    @Inject
    private RecipeHtmlRepository recipeHtmlRepository;

    public Mono<Long> processUrlCrawl(Long htmlRunId, String url, String compressedHtml) {
        return Mono.from(recipeHtmlRepository.existsByUrl(url)
                .flatMap(e -> {
                    if (e) {
                        return Mono.from(recipeHtmlRepository.findByUrl(url).last().map(rh -> rh.recipeId()));
                    } else {
                        RecipeHtml rh = RecipeHtml.createRecipeHtml(htmlRunId, url, compressedHtml);
                        return Mono.from(recipeHtmlRepository.save(rh)).map(r -> r.recipeId());
                    }
                }));
    }

    public static record CompressedUrlIdPair(String compressedUrl, Long recipeId) {

    }

    public Flux<CompressedUrlIdPair> getVisitedURLsCompressed() {
        return recipeHtmlRepository.findAll().map(rh -> new CompressedUrlIdPair(RecipeHtml.compressString(rh.url()), rh.recipeId()));
    }
}
