package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import org.json.JSONObject;
import javafx.application.Platform;

public class Weather extends JavOSWindow {
    private static final String API_KEY = "5bd6eb836e971518e002c7f23c742128"; // User needs to replace this with their API key
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String DEFAULT_CITY = "New York";
    
    private VBox contentBox;
    private Label weatherInfo;
    private TextField cityInput;
    
    public Weather(DesktopController desktop) {
        super("Weather", desktop);
    }
    
    @Override
    protected Region createContent() {
        contentBox = new VBox(10);
        contentBox.setPadding(new Insets(15));
        
        HBox searchBox = new HBox(10);
        cityInput = new TextField(DEFAULT_CITY);
        cityInput.setPromptText("Enter city name");
        Button searchButton = new Button("Get Weather");
        searchButton.setOnAction(e -> fetchWeather());
        searchBox.getChildren().addAll(cityInput, searchButton);
        
        weatherInfo = new Label("Fetching weather for New York...");
        
        contentBox.getChildren().addAll(searchBox, weatherInfo);
        
        // Fetch New York weather when the window opens
        Platform.runLater(this::fetchWeather);
        
        return contentBox;
    }
    
    private void fetchWeather() {
        String city = cityInput.getText().isEmpty() ? DEFAULT_CITY : cityInput.getText();
        fetchWeatherForCity(city);
    }
    
    private void fetchWeatherForCity(String city) {
        Thread thread = new Thread(() -> {
            try {
                String url = String.format("%s?q=%s&appid=%s&units=metric", API_URL, city, API_KEY);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject json = new JSONObject(response.body());
                
                String temp = json.getJSONObject("main").getDouble("temp") + "°C";
                String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
                String humidity = json.getJSONObject("main").getInt("humidity") + "%";
                
                Platform.runLater(() -> {
                    weatherInfo.setText(String.format("""
                        Temperature: %s
                        Description: %s
                        Humidity: %s
                        """, temp, description, humidity));
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    weatherInfo.setText("Error fetching weather data: " + e.getMessage());
                });
            }
        });
        thread.start();
    }
} 