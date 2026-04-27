package org.example;

import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import java.io.*;


@RestController
@CrossOrigin
public class CrawlerController {

    // START API
    @GetMapping("/start")
    public String start(@RequestParam String url, @RequestParam(defaultValue = "50") int max) {
//    public String start(@RequestParam String url, @RequestParam int max) {

        new Thread(() -> {
            try {
                PrintWriter writer = new PrintWriter("seo_report.csv");
                Main.startCrawler(url, max, writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return "Crawler Started 🚀";
    }
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile() throws Exception {

        File file = new File("seo_report.csv");

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=seo_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }


    // STATUS API
    @GetMapping("/status")
    public Map<String, Object> status() {

        Map<String, Object> res = new HashMap<>();
        res.put("running", Main.isRunning());
        res.put("logs", Main.getLogs());
        res.put("pagesCrawled", Main.pagesCrawled.get());

        return res;
    }
}