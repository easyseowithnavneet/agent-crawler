/*package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.awt.Desktop;


public class CrawlerUI {

    private JTextField urlField;
    private JTextField maxPagesField;
    private JTextArea outputArea;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrawlerUI().createUI());
    }

    public void createUI() {
        JFrame frame = new JFrame("SEO Crawler Tool");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());


        // ===== TOP INPUT PANEL =====
        JPanel topPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        topPanel.add(new JLabel("Start URL:"));
        urlField = new JTextField("https://example.com/");
        topPanel.add(urlField);

        topPanel.add(new JLabel("Max Pages:"));
        maxPagesField = new JTextField("20");
        topPanel.add(maxPagesField);

        JButton startBtn = new JButton("Start Crawl");
        topPanel.add(startBtn);
        JButton openBtn = new JButton("Open Report");
        topPanel.add(openBtn);
        openBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new java.io.File("seo_report.csv"));
            } catch (Exception ex) {
                appendOutput("❌ Cannot open file");
            }
        });



        panel.add(topPanel, BorderLayout.NORTH);

        // ===== OUTPUT AREA =====
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);

        // ===== BUTTON ACTION =====
        startBtn.addActionListener((ActionEvent e) -> startCrawling());
    }

    private void startCrawling() {
        String url = urlField.getText();
        int maxPages = Integer.parseInt(maxPagesField.getText());

        outputArea.setText("Starting crawl...\n");

        new Thread(() -> {
            try {
                PrintWriter writer = new PrintWriter(new FileWriter("seo_report.csv"));
//                writer.println("URL,Title Length,Score");
                writer.println("URL,Title Length,Meta Desc Length,H1 Count,Images,Missing Alt,Schema Count,SEO Score");


                // call your crawler
                Main.startCrawler(url, maxPages, writer, outputArea);

//                writer.close();

//                appendOutput("\n✅ Crawl finished. Report saved!");
//                appendOutput("\n✅ Crawl finished. Opening report...");

//                Desktop.getDesktop().open(new java.io.File("seo_report.csv"));


            } catch (Exception ex) {
                appendOutput("Error: " + ex.getMessage());
            }
        }).start();
    }

    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> outputArea.append(text + "\n"));
    }
}*/
