package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;


public class Main {

    static BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    static AtomicInteger activeTasks = new AtomicInteger(0);
    static Set<String> visited = ConcurrentHashMap.newKeySet();

    static int maxPages = 100;
    static int THREADS = 5;

    static ExecutorService executor;

    static PrintWriter writer;
    static String domain;

    public static AtomicInteger pagesCrawled = new AtomicInteger(0);

    // ===== START CRAWLER =====
    public static void startCrawler(String startUrl, int max, PrintWriter w) {

        try {
            // ✅ FIX deprecated URL
            domain = new URI(startUrl).toURL().getHost();

            writer = w;
            maxPages = max;

            queue.clear();
            visited.clear();
            activeTasks.set(0);
            pagesCrawled.set(0);
            queue.add(startUrl);

            executor = Executors.newFixedThreadPool(THREADS);

            for (int i = 0; i < THREADS; i++) {
                executor.submit(Main::worker);
            }

            // ✅ BETTER WAIT (no busy loop)
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);

            writer.flush();
            writer.close();

            log("✅ Crawl Completed!");
            log("Total pages: " + pagesCrawled.get());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== WORKER =====
    public static void worker() {

        while (true) {
            String url = null;

            try {
                url = queue.poll(3, TimeUnit.SECONDS);

                if (url == null) {
                    if (activeTasks.get() == 0) return;
                    else continue;
                }

                synchronized (visited) {
                    if (visited.size() >= maxPages) return;
                    if (!visited.add(url)) continue;
                }

                activeTasks.incrementAndGet();

                log("Crawling: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                String title = doc.title();
                int titleLen = title.length();

                Element desc = doc.selectFirst("meta[name=description]");
                int descLen = (desc != null) ? desc.attr("content").length() : 0;

                int h1Count = doc.select("h1").size();

                Elements images = doc.select("img");
                int totalImages = images.size();
                int missingAlt = 0;

                for (Element img : images) {
                    if (!img.hasAttr("alt") || img.attr("alt").isEmpty()) {
                        missingAlt++;
                    }
                }

                int schemaCount = doc.select("script[type=application/ld+json]").size();

                int score = 0;
                if (titleLen >= 50 && titleLen <= 60) score += 20;
                if (descLen >= 140 && descLen <= 160) score += 20;
                if (h1Count == 1) score += 15;
                if (totalImages > 0 && missingAlt == 0) score += 15;
                if (schemaCount > 0) score += 10;
                if (doc.selectFirst("link[rel=canonical]") != null) score += 10;
                if (doc.select("meta[property^=og:]").size() > 0) score += 10;

                synchronized (writer) {
                    writer.println(url + "," + titleLen + "," + descLen + "," + h1Count + "," +
                            totalImages + "," + missingAlt + "," + schemaCount + "," + score);
                }

                int count = pagesCrawled.incrementAndGet();
                log("📄 Pages crawled: " + count);

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String nextUrl = normalizeUrl(link.absUrl("href"));

                    if (isValidUrl(nextUrl) &&
                            nextUrl.startsWith("https://" + domain) &&
                            visited.size() < maxPages) {

                        queue.offer(nextUrl);
                    }
                }

            } catch (Exception e) {
                log("Failed: " + e.getMessage());
            } finally {
                if (url != null) {
                    activeTasks.decrementAndGet();
                }
            }
        }
    }

    public static void log(String msg) {
        System.out.println(msg);
    }

    public static String normalizeUrl(String url) {
        int hash = url.indexOf("#");
        if (hash != -1) url = url.substring(0, hash);

        int q = url.indexOf("?");
        if (q != -1) url = url.substring(0, q);

        return url;
    }

    public static boolean isValidUrl(String url) {
        return url.startsWith("http") &&
                !url.contains("facebook") &&
                !url.contains("twitter") &&
                !url.contains("linkedin") &&
                !url.contains("mailto:");
    }
}
