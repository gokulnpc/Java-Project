package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Weather extends JavOSWindow {
    private static final String API_KEY = "5bd6eb836e971518e002c7f23c742128";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String DEFAULT_CITY = "New York";
    
    private VBox contentBox;
    private VBox weatherBox;
    private Label statusLabel;
    private TextField cityInput;
    private Button searchButton;
    
    public Weather(DesktopController desktop) {
        super("Weather", desktop);
        setPrefWidth(400);
        setPrefHeight(600);
    }
    
    @Override
    protected Region createContent() {
        contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-background-color: #2f3640;");
        
        // Search Box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        cityInput = new TextField(DEFAULT_CITY);
        cityInput.setPromptText("Enter city name");
        cityInput.setPrefWidth(250);
        cityInput.setStyle("""
            -fx-background-color: #353b48;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #7f8c8d;
            -fx-padding: 8;
            -fx-background-radius: 4;
            """);
        
        searchButton = new Button("Get Weather");
        searchButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 8 15;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        searchButton.setOnAction(e -> fetchWeather());
        
        // Add hover effect
        searchButton.setOnMouseEntered(e -> 
            searchButton.setStyle("""
                -fx-background-color: #2980b9;
                -fx-text-fill: white;
                -fx-padding: 8 15;
                -fx-background-radius: 4;
                -fx-cursor: hand;
                """)
        );
        
        searchButton.setOnMouseExited(e -> 
            searchButton.setStyle("""
                -fx-background-color: #3498db;
                -fx-text-fill: white;
                -fx-padding: 8 15;
                -fx-background-radius: 4;
                -fx-cursor: hand;
                """)
        );
        
        searchBox.getChildren().addAll(cityInput, searchButton);
        
        // Weather Info Box
        weatherBox = new VBox(15);
        weatherBox.setAlignment(Pos.CENTER);
        weatherBox.setStyle("""
            -fx-background-color: rgba(53, 59, 72, 0.5);
            -fx-background-radius: 8;
            -fx-padding: 20;
            """);
        
        statusLabel = new Label("Enter a city name to get weather information");
        statusLabel.setStyle("-fx-text-fill: #bdc3c7;");
        weatherBox.getChildren().add(statusLabel);
        
        contentBox.getChildren().addAll(searchBox, weatherBox);
        
        // Handle enter key in city input
        cityInput.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                fetchWeather();
            }
        });
        
        // Initial weather fetch
        Platform.runLater(this::fetchWeather);
        
        return contentBox;
    }
    
    private void fetchWeather() {
        String city = cityInput.getText().isEmpty() ? DEFAULT_CITY : cityInput.getText();
        
        // Update UI to show loading state
        searchButton.setDisable(true);
        statusLabel.setText("Fetching weather data...");
        weatherBox.getChildren().clear();
        weatherBox.getChildren().add(statusLabel);
        
        Thread thread = new Thread(() -> {
            try {
                String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
                String url = String.format("%s?q=%s&appid=%s&units=metric", 
                    WEATHER_API_URL, encodedCity, API_KEY);
                
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
                
                HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                JSONObject json = new JSONObject(response.body());
                
                if (response.statusCode() == 200) {
                    JSONObject main = json.getJSONObject("main");
                    double temp = main.getDouble("temp");
                    double feelsLike = main.getDouble("feels_like");
                    int humidity = main.getInt("humidity");
                    int pressure = main.getInt("pressure");
                    
                    JSONObject wind = json.getJSONObject("wind");
                    double windSpeed = wind.getDouble("speed");
                    int windDeg = wind.getInt("deg");
                    
                    JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
                    String description = weather.getString("description");
                    
                    int visibility = json.getInt("visibility");
                    
                    String countryCode = json.getJSONObject("sys").getString("country");
                    double minTemp = main.getDouble("temp_min");
                    double maxTemp = main.getDouble("temp_max");
                    
                    Platform.runLater(() -> updateWeatherUI(
                        city, countryCode, temp, feelsLike, description, 
                        humidity, pressure, windSpeed, windDeg, visibility, 
                        0.0, minTemp, maxTemp // UV index not available in free API
                    ));
                } else {
                    String message = json.has("message") ? 
                        json.getString("message") : 
                        "City not found. Please check the spelling and try again.";
                    Platform.runLater(() -> showError(message));
                }
                
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error fetching weather data: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> searchButton.setDisable(false));
            }
        });
        thread.start();
    }
    
    private void updateWeatherUI(String city, String country, double temp, double feelsLike, 
                               String description, int humidity, int pressure, double windSpeed,
                               int windDeg, int visibility, double uvi, double minTemp, double maxTemp) {
        weatherBox.getChildren().clear();
        
        // Location
        Label locationLabel = new Label(city + ", " + country);
        locationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Temperature
        Label tempLabel = new Label(String.format("%.1f°C", temp));
        tempLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 48px; -fx-font-weight: bold;");
        
        // Min/Max Temperature
        Label minMaxLabel = new Label(String.format("Min: %.1f°C / Max: %.1f°C", minTemp, maxTemp));
        minMaxLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");
        
        // Description
        Label descLabel = new Label(description.substring(0, 1).toUpperCase() + description.substring(1));
        descLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 18px;");
        
        // Feels like
        Label feelsLabel = new Label(String.format("Feels like: %.1f°C", feelsLike));
        feelsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");
        
        // Wind
        String windDirection = getWindDirection(windDeg);
        Label windLabel = new Label(String.format("Wind: %.1f m/s %s", windSpeed, windDirection));
        windLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");
        
        // Other details
        Label humidityLabel = new Label(String.format("Humidity: %d%%", humidity));
        humidityLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");
        
        Label pressureLabel = new Label(String.format("Pressure: %d hPa", pressure));
        pressureLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");
        
        Label visibilityLabel = new Label(String.format("Visibility: %.1f km", visibility / 1000.0));
        visibilityLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");
        
        weatherBox.getChildren().addAll(
            locationLabel,
            tempLabel,
            minMaxLabel,
            descLabel,
            new Separator(),
            feelsLabel,
            windLabel,
            humidityLabel,
            pressureLabel,
            visibilityLabel
        );
    }
    
    private String getWindDirection(int degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                             "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int)((degrees + 11.25) / 22.5) % 16;
        return directions[index];
    }
    
    private void showError(String message) {
        weatherBox.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);
        weatherBox.getChildren().add(errorLabel);
    }
} 