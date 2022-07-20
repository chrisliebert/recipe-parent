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
package com.clieb.kitchen.crawler;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.http.client.HttpClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import reactor.core.publisher.Mono;

/**
 *
 * @author chris
 */
public class CrawlerApp {

    private static boolean shouldVisit(final String url) {
        return url.startsWith("https://www.allrecipes.com/recipe");
    }

    private final static Pattern IGNORE_PATTERN = Pattern.compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");

    private static boolean isRecipe(final String url) {
        return url.startsWith("https://www.allrecipes.com/recipe/") && !IGNORE_PATTERN.matcher(url).find();
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
            Logger.getLogger(CrawlerApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Base64.getEncoder().encodeToString(compressedOutputStream.toByteArray());
    }

    public static String decompressString(String compressed) {

        String decompressedHtml = null;
        try ( InputStream recipeHtmlStream = new ByteArrayInputStream(Base64.getDecoder()
                .decode(compressed));  GZIPInputStream gis = new GZIPInputStream(recipeHtmlStream)) {
            decompressedHtml = new String(gis.readAllBytes());
        } catch (IOException ex) {
            Logger.getLogger(CrawlerApp.class.getName()).log(Level.SEVERE, null, ex);
            // throw ex;
        }
        return decompressedHtml;
    }

    static class ConTask implements Runnable {

        UrlBuffer buffer;
        HttpClient httpClient;

        ConTask(UrlBuffer buffer, HttpClient httpClient) {
            this.buffer = buffer;
            this.httpClient = httpClient;
        }

        private void processUrl(String url) {
            Mono<String> htmlMono = Mono.from(httpClient.retrieve(url))
                    .doOnError(onError -> {
                        buffer.exceptionQueue.add(onError);
                    })
                    .retry(10).onErrorStop();
            String html = htmlMono.block();
            if (html != null && !html.isEmpty()) {
                Document doc = Jsoup.parse(html);
                Elements linkElements = doc.getElementsByTag("a");
                HashSet<String> linksToVisit = new HashSet<>();
                for (Element link : linkElements) {
                    String href = link.attr("href");
                    if (shouldVisit(href) && !href.equalsIgnoreCase(url)) {
                        linksToVisit.add(href);
                    }
                }
                buffer.putURLs(linksToVisit);
                final Long htmlRunId = 1l;//TODO: get from server
                if (isRecipe(url)) {
                    RecipeServerConfiguration configuration = new RecipeServerConfiguration();
                    RecipeServerApiLowLevelClient recipeClient = new RecipeServerApiLowLevelClient(httpClient, configuration);
                    String compressedHtml = compressString(html);
                    recipeClient.processRecipeURL(htmlRunId, url, compressedHtml)
                            .doOnError(onError -> {
                                buffer.exceptionQueue.add(onError);
                            }).retry(10).onErrorStop().subscribe((a) -> {
                        //System.out.println("received " + a.body() + " -> " + url);
                        buffer.putRecipeId(url, a.body());
                    });
                } else {
                    // We want to track this url so we don't re-processed by another thread
                    // but it is not a recipe id so it will be -1 here
                    buffer.putRecipeId(url, -1L);
                }
            }
        }

        @Override
        public void run() {
            try {
                // delay to make sure producer starts first
                // Thread.sleep(20);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String url = buffer.get();
            if (url != null) {
                try {
                    processUrl(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!buffer.isEmpty()) {
                run();
            }
        }
    }
    
    //Shared class used by threads
    static class UrlBuffer {

        // String s;
        Queue<String> clQueue = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<String, Long> visitedUrls;
        Queue<Throwable> exceptionQueue = new ConcurrentLinkedQueue<>();

        public UrlBuffer() {
            visitedUrls = new ConcurrentHashMap<>();
        }

        //Retrieving from the queue 
        public String get() {
            String a = clQueue.poll();
            return a == null ? null : !visitedUrls.contains(a) ? a : null;
        }

        public void putURL(final String s) {
            // this.s = s;
            if (!clQueue.contains(s) && !visitedUrls.contains(s)) {
                clQueue.add(s);
            }
        }

        public void putURLs(Collection<? extends String> buff) {
            buff.forEach(s -> this.putURL(s));
        }

        public void putRecipeId(final String url, final Long recipeId) {
            visitedUrls.put(url, recipeId);
        }

        public boolean isEmpty() {
            return clQueue.isEmpty();
        }
    }

    static int lastSize = 0;
    static int numTicks = 0;

    private static HashMap<String, Long> getVistedUrls() {
        HashMap<String, Long> urls = new HashMap<>();
        ApplicationContextBuilder acb = ApplicationContext.builder();
        try ( HttpClient httpClient = acb.run(HttpClient.class)) {
            RecipeServerConfiguration configuration = new RecipeServerConfiguration();
            RecipeServerApiLowLevelClient recipeClient = new RecipeServerApiLowLevelClient(httpClient, configuration);
            List<RecipeServerApiLowLevelClient.CompressedUrlIdPair> compressedUrls = recipeClient.getVisitedURLs().block();
            if (compressedUrls != null) {
                compressedUrls.forEach(compressedUrl -> {
                    urls.put(decompressString(compressedUrl.compressedUrl()), compressedUrl.recipeId());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urls;
    }
    //public static record RecipeSiteConfig(String )

    public static void main(String[] args) {
        try {
            UrlBuffer buffer = new UrlBuffer();
            buffer.putURL("https://www.allrecipes.com/recipes/92/meat-and-poultry/");          
            
            HashMap<String, Long> downloadedVisitedURLs = getVistedUrls();
            for (String url : downloadedVisitedURLs.keySet()) {
                buffer.putRecipeId(url, downloadedVisitedURLs.get(url));
            }
            System.out.println("Downloaded " + Integer.toString(buffer.visitedUrls.size()) + " urls");
            int numThreads = 64;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            for (int i = 0; i < numThreads; i++) {
                ApplicationContextBuilder acb = ApplicationContext.builder();
                try ( HttpClient httpClient = acb.run(HttpClient.class
                )) {
                    executor.execute(
                            new ConTask(buffer, httpClient));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // stat monitor thread
            executor.execute(() -> {
                final int initialSize = buffer.visitedUrls.size();
                lastSize = initialSize;
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {

                        while (!buffer.exceptionQueue.isEmpty()) {
                            Throwable t = buffer.exceptionQueue.remove();
                            if (t != null) {
                                System.out.println(t.getMessage());
                            }
                        }

                        int buffSize = buffer.visitedUrls.size();
                        int addedSinceLastInterval = buffSize - lastSize;
                        lastSize = buffSize;
                        numTicks++;
                        System.out.println(Integer.toString(buffSize) + " total\t new: " + addedSinceLastInterval + '\t' + " rate:" + (buffSize / numTicks)
                                + '\t' + " queue size:" + buffer.clQueue.size()
                                + '\t' + " error count:" + buffer.exceptionQueue.size()
                        );
                    }
                };
                Timer timer = new Timer("MyTimer");//create a new timer
                int interval = 10000; // display stats every 10 seconds
                timer.scheduleAtFixedRate(timerTask, interval, interval);//start timer in 30ms to increment  counter            }
            });
            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}