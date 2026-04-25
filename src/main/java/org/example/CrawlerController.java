package org.example;

import org.springframework.web.bind.annotation.*;
import java.io.PrintWriter;

@RestController
@CrossOrigin
public class CrawlerController {

    @GetMapping("/")
    public String home() {
        return "SEO Crawler is running 🚀";
    }

    @GetMapping("/crawl")
    public String crawl(@RequestParam String url,
                        @RequestParam(defaultValue = "50") int max) {

        try {
            PrintWriter writer = new PrintWriter("seo_report.csv");

            Main.startCrawler(url, max, writer);

            return "✅ Crawl completed! CSV generated.";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}

/*package org.example;

import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;


@RestController
 class CrawlController {
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws Exception {

        File file = new File("seo_report.csv");

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=seo_report.csv");

        java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @GetMapping("/crawl")
    public String startCrawl(@RequestParam String url) {

        new Thread(() -> {
            try {
                PrintWriter writer = new PrintWriter("seo_report.csv");
                writer.println("URL,Title Length,Meta Desc Length,H1 Count,Images,Missing Alt,Schema Count,SEO Score");

                Main.startCrawler(url, 100, writer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return "✅ Crawling started for: " + url;
    }

    @GetMapping("/status")
    public String status() {
        return "📄 Pages crawled: " + Main.pagesCrawled.get();
    }
}*/
