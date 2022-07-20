/*
 * Copyright 2020 Chris Liebert
 * 
 * 
 */
package com.clieb.kitchen.crawler;

import com.clieb.kitchen.crawler.RecipeServerApiClient;
import com.clieb.kitchen.crawler.RecipeServerApiClient.RecipeHtml;
import com.clieb.kitchen.crawler.RecipeServerApiLowLevelClient;
import com.clieb.kitchen.crawler.RecipeServerConfiguration;
import java.util.regex.Pattern;

import io.micronaut.http.client.HttpClient;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author Chris
 */
public class RecipeWebCrawler { 
//    extends WebCrawler {
//
//    private final HttpClient httpClient;
//    private final RecipeServerApiClient recipeClient;
//
//    private int numVisited;
//    //private int numErrors;
//    private final Long runId;
//
//    private Flux<String> submittedHtmlPageResponses;
//
//    public RecipeWebCrawler(HttpClient HttpClient, final Long recipeHtmlRunId) {
//        this.httpClient = HttpClient;
//        RecipeServerConfiguration configuration = new RecipeServerConfiguration();
//        recipeClient = new RecipeServerApiLowLevelClient(httpClient, configuration);
//        runId = recipeHtmlRunId;
//    }
//
//    private final static Pattern EXCLUSIONS
//            = Pattern.compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");
//
//    @Override
//    public void onStart() {
//        //RecipeServerConfiguration configuration = new RecipeServerConfiguration();
//        //recipeClient = new RecipeServerApiLowLevelClient(httpClient, configuration);
//        numVisited = 0;
//        submittedHtmlPageResponses = Flux.empty();
////        Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.INFO, "Using run id {0}", runId);
//
//    }
//
//    @Override
//    public void onBeforeExit() {
//        submittedHtmlPageResponses.blockLast();
//    }
//
//    /**
//     *
//     * @param referringPage
//     * @param url
//     * @return
//     */
//    @Override
//    public boolean shouldVisit(Page referringPage, WebURL url) {
//        String urlString = url.getURL().toLowerCase();
//        return !EXCLUSIONS.matcher(urlString).matches()
//                && urlString.startsWith("https://www.allrecipes.com/recipe");
//    }
//
//    /**
//     *
//     * @param page
//     */
//    @Override
//    public void visit(Page page) {
//        String url = page.getWebURL().getURL();
//        if (url.startsWith("https://www.allrecipes.com/recipe/") && !url.endsWith("/?printview")) {
//            ParseData parseData = page.getParseData();
//            if (parseData instanceof HtmlParseData) {
//                HtmlParseData hpd = (HtmlParseData) (parseData);
//                // Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.INFO, "HtmlParseData matched for {0}", url);
//                Mono<String> responseMono = recipeClient.postRecipeHtml(
//                        new RecipeHtml(null, runId, hpd.getHtml(), null, url, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()))
//                )
//                        // .onErrorContinue((ex, a) -> Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex.fillInStackTrace()))
//                        //.retry(10)
//                        .onErrorContinue((ex, a) -> {
//                            // numErrors++;
//                            Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex.fillInStackTrace());
//                        })
//                        .retry(5)
//                        .onErrorStop();
//
//                submittedHtmlPageResponses = submittedHtmlPageResponses.mergeWith(responseMono);
//                final int buffSize = 2;
//                if ((numVisited % buffSize) == 0) {
////                        List<String> serverResponses = 
//                    submittedHtmlPageResponses.buffer(buffSize).blockLast();
//                    Runtime.getRuntime().gc();
////                        if (serverResponses != null) {
////                            serverResponses
////                                    .stream()
////                                    .forEach(text -> Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.INFO, "Server respones: {0}", text));
////
////                        }
//                }
//
//            } else {
//                Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.INFO, "HtmlParseData not matched for {0}", url);
//                //numErrors++;
//            }
//            numVisited++;
//        }
////            switch (parseData) {
////                case HtmlParseData hpd -> {
////                    // Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.INFO, "HtmlParseData matched for {0}", url);
////                    Mono<String> responseMono = recipeClient.postRecipeHtml(
////                            new RecipeHtml(null, runId, hpd.getHtml(), null, url, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()))
////                    )
////                            // .onErrorContinue((ex, a) -> Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex.fillInStackTrace()))
////                            //.retry(10)
////                            .onErrorContinue((ex, a) -> {
////                                numErrors++;
////                                Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex.fillInStackTrace());
////                                    })
////                            .retry(5)
////                            .onErrorStop();
////
////                    submittedHtmlPageResponses = submittedHtmlPageResponses.mergeWith(responseMono);
////                    final int buffSize = 2;
////                    if ((numVisited % buffSize) == 0) {
//////                        List<String> serverResponses = 
////                        submittedHtmlPageResponses.buffer(buffSize).blockLast();
////                        Runtime.getRuntime().gc();
//////                        if (serverResponses != null) {
//////                            serverResponses
//////                                    .stream()
//////                                    .forEach(text -> Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.INFO, "Server respones: {0}", text));
//////
//////                        }
////                    }
////                }
////                default ->
////                    Logger.getLogger(RecipeWebCrawler.class.getName()).log(Level.INFO, "HtmlParseData not matched for {0}", url);
////            };
////            numVisited++;
////        }
//    }
}
