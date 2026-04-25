package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.PrintWriter;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    // ===== THREAD-SAFE STRUCTURES =====
    static BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    static Set<String> visited = ConcurrentHashMap.newKeySet();

    static AtomicInteger activeTasks = new AtomicInteger(0);
    static AtomicInteger pagesCrawled = new AtomicInteger(0);

    static List<String> logs = Collections.synchronizedList(new ArrayList<>());

    static ExecutorService executor;

    static int maxPages = 100;
    static int THREADS = 5;
    static String domain;
    static PrintWriter writer;

    static volatile boolean isRunning = false;

    // ===== START CRAWLER =====
    public static void startCrawler(String startUrl, int max, PrintWriter w) {
        try {
            isRunning = true;

            domain = new URI(startUrl).getHost();

            writer = w;
            maxPages = max;

            // RESET STATE
            queue.clear();
            visited.clear();
            logs.clear();
            activeTasks.set(0);
            pagesCrawled.set(0);

            // ✅ IMPORTANT: ADD START URL
            queue.offer(startUrl);

            executor = Executors.newFixedThreadPool(THREADS);

            for (int i = 0; i < THREADS; i++) {
                executor.submit(Main::worker);
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);

            isRunning = false;

            writer.flush();
            writer.close();

            log("✅ Crawl Completed!");
            log("Total pages: " + pagesCrawled.get());

        } catch (Exception e) {
            log("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== WORKER THREAD =====
    public static void worker() {

        while (true) {
            String url = null;

            try {
                url = queue.poll(3, TimeUnit.SECONDS);

                // Stop condition
                if (url == null) {
                    if (activeTasks.get() == 0) return;
                    else continue;
                }

                // LIMIT + DUPLICATE CHECK
                if (visited.size() >= maxPages) return;
                if (!visited.add(url)) continue;

                activeTasks.incrementAndGet();

                log("Crawling: " + url);

                // ===== FETCH PAGE =====
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
                        .ignoreHttpErrors(true)
                        .ignoreContentType(true)
                        .followRedirects(true)
                        .get();

                // ===== SEO METRICS =====
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

                // ===== SEO SCORE =====
                int score = 0;
                if (titleLen >= 50 && titleLen <= 60) score += 20;
                if (descLen >= 140 && descLen <= 160) score += 20;
                if (h1Count == 1) score += 15;
                if (totalImages > 0 && missingAlt == 0) score += 15;
                if (schemaCount > 0) score += 10;
                if (doc.selectFirst("link[rel=canonical]") != null) score += 10;
                if (doc.select("meta[property^=og:]").size() > 0) score += 10;

                // ===== WRITE CSV =====
                synchronized (writer) {
                    writer.println(url + "," + titleLen + "," + descLen + "," +
                            h1Count + "," + totalImages + "," + missingAlt + "," +
                            schemaCount + "," + score);
                }

                int count = pagesCrawled.incrementAndGet();
                log("📄 Pages crawled: " + count);

                // ===== EXTRACT LINKS =====
                Elements links = doc.select("a[href]");

                for (Element link : links) {
                    String nextUrl = normalizeUrl(link.absUrl("href"));

                    if (isValidUrl(nextUrl) &&
                            isSameDomain(nextUrl) &&
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

    // ===== LOGGING =====
    public static void log(String msg) {
        logs.add(msg);
        System.out.println(msg);
    }

    public static List<String> getLogs() {
        return logs;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    // ===== URL HELPERS =====
    public static String normalizeUrl(String url) {
        try {
            int hash = url.indexOf("#");
            if (hash != -1) url = url.substring(0, hash);

            int q = url.indexOf("?");
            if (q != -1) url = url.substring(0, q);

        } catch (Exception ignored) {}

        return url;
    }

    public static boolean isValidUrl(String url) {
        return url.startsWith("http") &&
                !url.contains("facebook") &&
                !url.contains("twitter") &&
                !url.contains("linkedin") &&
                !url.contains("mailto:");
    }

    public static boolean isSameDomain(String url) {
        try {
            String host = new URI(url).getHost();
            return host != null && host.contains(domain);
        } catch (Exception e) {
            return false;
        }
    }
}