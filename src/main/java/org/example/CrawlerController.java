package org.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class CrawlerController {

    private static final String FILE_NAME = "seo_report.csv";

    // ===== START CRAWL =====
    @PostMapping("/start")
    public ResponseEntity<String> startCrawl(
            @RequestParam String url,
            @RequestParam(defaultValue = "50") int maxPages) {

        if (Main.isRunning()) {
            return ResponseEntity.ok("Crawler already running...");
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME));
            writer.println("URL,TitleLength,DescriptionLength,H1Count,TotalImages,MissingAlt,SchemaCount,SEOScore");

            // Run crawler in new thread (non-blocking)
            new Thread(() -> Main.startCrawler(url, maxPages, writer)).start();

            return ResponseEntity.ok("Crawl started!");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error starting crawl");
        }
    }

    // ===== STATUS API =====
    @GetMapping("/status")
    public Map<String, Object> getStatus() {

        Map<String, Object> response = new HashMap<>();
        response.put("running", Main.isRunning());
        response.put("pagesCrawled", Main.pagesCrawled.get());
        response.put("logs", Main.getLogs());

        return response;
    }

    // ===== DOWNLOAD CSV =====
    @GetMapping("/download")
    public ResponseEntity<File> downloadCSV() {

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(file);
    }
}
