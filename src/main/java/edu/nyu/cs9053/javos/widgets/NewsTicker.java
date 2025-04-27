package edu.nyu.cs9053.javos.widgets;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NewsTicker extends HBox {
    private final Label newsLabel = new Label("Loading news...");
    private final List<String> headlines = new ArrayList<>();
    private int currentHeadline = 0;
    private Timeline tickerTimeline;

    // Replace with your NewsAPI key
    private static final String API_KEY = "98df55f89ea346228f95feaa7c63c179";
    // You can change the country code as needed
    private static final String COUNTRY = "us";

    public NewsTicker() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setStyle("-fx-background-color: transparent;");
        newsLabel.setFont(Font.font("Consolas", 14));
        newsLabel.setStyle("-fx-text-fill: #fff;");
        getChildren().add(newsLabel);

        fetchHeadlines();
    }

    private void fetchHeadlines() {
        new Thread(() -> {
            try {
                String urlString = String.format(
                    "https://newsapi.org/v2/top-headlines?country=%s&pageSize=10&apiKey=%s",
                    COUNTRY, API_KEY
                );
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    Platform.runLater(() -> newsLabel.setText("Failed to load news"));
                    return;
                }

                StringBuilder inline = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject data = new JSONObject(inline.toString());
                JSONArray articles = data.getJSONArray("articles");
                headlines.clear();
                for (int i = 0; i < articles.length(); i++) {
                    String title = articles.getJSONObject(i).getString("title");
                    headlines.add(title);
                }

                Platform.runLater(this::startTicker);

            } catch (Exception e) {
                Platform.runLater(() -> newsLabel.setText("Error loading news"));
            }
        }).start();
    }

    private void startTicker() {
        if (headlines.isEmpty()) {
            newsLabel.setText("No news available");
            return;
        }
        newsLabel.setText(headlines.get(0));
        if (tickerTimeline != null) {
            tickerTimeline.stop();
        }
        tickerTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            currentHeadline = (currentHeadline + 1) % headlines.size();
            newsLabel.setText(headlines.get(currentHeadline));
        }));
        tickerTimeline.setCycleCount(Timeline.INDEFINITE);
        tickerTimeline.play();
    }
} 