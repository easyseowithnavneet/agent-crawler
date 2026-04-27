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

    static BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    static AtomicInteger activeTasks = new AtomicInteger(0);
    static Set<String> visited = ConcurrentHashMap.newKeySet();

    public static List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public static volatile boolean running = false;


    static int maxPages = 100;
    static int THREADS = 5;

    static ExecutorService executor;
    static PrintWriter writer;
    static String domain;
    public static AtomicInteger pagesCrawled = new AtomicInteger(0);

    // ===== START CRAWLER =====
    public static void startCrawler(String startUrl, int max, PrintWriter w) {

        try {
            running = true;

            domain = new URI(startUrl).toURL().getHost();
            writer = w;
            writer.println("URL,Title Length,Meta Description Length,H1 Count,Total Images,Missing Alt,SEO Score");
            maxPages = max;

            queue.clear();
            visited.clear();
            logs.clear();
            pagesCrawled.set(0);

            queue.add(startUrl); // ✅ IMPORTANT

            executor = Executors.newFixedThreadPool(THREADS);

            for (int i = 0; i < THREADS; i++) {
                executor.submit(Main::worker);
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);

            log("✅ Crawl Completed!");
            log("Total pages: " + pagesCrawled.get());

        } catch (Exception e) {
            log("Error: " + e.getMessage());
        } finally {
            running = false;
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    // ===== WORKER =====
    public static void worker() {

        while (true) {
            try {
                String url = queue.poll(3, TimeUnit.SECONDS);

                if (url == null) return;

                if (!visited.add(url)) continue;
                if (visited.size() > maxPages) return;

                log("Crawling: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
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

                int score = 0;
                if (titleLen >= 50 && titleLen <= 60) score += 20;
                if (descLen >= 140 && descLen <= 160) score += 20;
                if (h1Count == 1) score += 15;
                if (totalImages > 0 && missingAlt == 0) score += 15;

                synchronized (writer) {
                    writer.println(url + "," + titleLen + "," + descLen + "," + h1Count + "," +
                            totalImages + "," + missingAlt + "," + score);
                }

                int count = pagesCrawled.incrementAndGet();
                log("📄 Pages crawled: " + count);

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String next = link.absUrl("href");

                    if (next.startsWith("http") &&
                            next.contains(domain) &&
                            visited.size() < maxPages) {

                        queue.offer(next);
                    }
                }

            } catch (Exception e) {
                log("Failed: " + e.getMessage());
            }
        }
    }

    public static void log(String msg) {
        logs.add(msg);
        System.out.println(msg);
    }

    public static List<String> getLogs() {
        return logs;
    }

    public static boolean isRunning() {
        return running;
    }
    public static int getPagesCrawled() {
        return pagesCrawled.get();
    }
}