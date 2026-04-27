package org.example;

import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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