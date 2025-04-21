package edu.nyu.cs9053.javos;

import edu.nyu.cs9053.javos.apps.Terminal;
import edu.nyu.cs9053.javos.apps.Notepad;
import edu.nyu.cs9053.javos.apps.Weather;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.KeyCode;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class DesktopController {
    @FXML private StackPane desktopPane;
    @FXML private HBox taskbarItems;
    @FXML private Label clockLabel;
    @FXML private TextField searchField;
    
    private final Map<String, JavOSWindow> runningApps = new HashMap<>();
    private Popup searchResults;
    private ListView<String> searchResultsList;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ObservableList<String> allApps = FXCollections.observableArrayList(
        "Terminal", "Notepad", "Calculator", "Weather"
    );
    
    @FXML
    public void initialize() {
        setupClock();
        setupSearch();
        
        // Add click handler to the desktop pane to close search popup
        desktopPane.setOnMouseClicked(event -> {
            if (searchResults != null && searchResults.isShowing()) {
                // Get the click coordinates relative to the search field
                double clickX = event.getSceneX();
                double clickY = event.getSceneY();
                
                // Get the bounds of the search field in scene coordinates
                double searchFieldMinX = searchField.localToScene(searchField.getBoundsInLocal()).getMinX();
                double searchFieldMaxX = searchField.localToScene(searchField.getBoundsInLocal()).getMaxX();
                double searchFieldMinY = searchField.localToScene(searchField.getBoundsInLocal()).getMinY();
                double searchFieldMaxY = searchField.localToScene(searchField.getBoundsInLocal()).getMaxY();
                
                // Check if click is outside search field and search results
                if (!(clickX >= searchFieldMinX && clickX <= searchFieldMaxX &&
                    clickY >= searchFieldMinY && clickY <= searchFieldMaxY)) {
                    searchField.clear();
                    searchResults.hide();
                }
            }
        });
    }
    
    private void setupClock() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime currentTime = LocalTime.now();
            clockLabel.setText(currentTime.format(timeFormatter));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
    private void setupSearch() {
        // Initialize search results popup
        searchResults = new Popup();
        searchResults.setAutoHide(false);
        searchResultsList = new ListView<>();
        searchResultsList.setPrefHeight(300); // Reduced height for better positioning
        searchResultsList.getStyleClass().add("search-results-list");
        
        // Create a container for the popup content
        VBox popupContent = new VBox(searchResultsList);
        popupContent.getStyleClass().add("search-results-container");
        searchResults.getContent().add(popupContent);
        
        // Bind the width of the popup content to the search field width
        searchResultsList.prefWidthProperty().bind(searchField.widthProperty());
        popupContent.prefWidthProperty().bind(searchField.widthProperty());
        
        // Setup filtered list
        FilteredList<String> filteredApps = new FilteredList<>(allApps, p -> true);
        searchResultsList.setItems(filteredApps);
        
        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredApps.setPredicate(app -> {
                if (newValue == null || newValue.isEmpty()) {
                    searchResults.hide();
                    return false;
                }
                return app.toLowerCase().contains(newValue.toLowerCase());
            });
            
            if (!newValue.isEmpty() && !filteredApps.isEmpty()) {
                if (!searchResults.isShowing()) {
                    // Calculate position relative to search field
                    Bounds searchFieldBounds = searchField.localToScreen(searchField.getBoundsInLocal());
                    
                    // Get screen bounds
                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                    
                    // Calculate X position (aligned with search field)
                    double x = searchFieldBounds.getMinX();
                    
                    // Calculate Y position (below search field with small gap)
                    double y = searchFieldBounds.getMaxY() + 2;
                    
                    // Check if popup would go off bottom of screen
                    if (y + searchResultsList.getPrefHeight() > screenBounds.getMaxY()) {
                        // If it would go off screen, show above search field instead
                        y = searchFieldBounds.getMinY() - searchResultsList.getPrefHeight() - 2;
                    }
                    
                    // Position popup
                    searchResults.setX(x-12);
                    searchResults.setY(y-33);
                    
                    searchResults.show(searchField.getScene().getWindow());
                }
            } else {
                searchResults.hide();
            }
        });

        // Prevent clicks in the search results from bubbling up
        searchResultsList.setOnMouseClicked(event -> {
            String selectedApp = searchResultsList.getSelectionModel().getSelectedItem();
            if (selectedApp != null) {
                launchApp(selectedApp);
                searchField.clear();
                searchResults.hide();
            }
            event.consume();
        });
        
        // Handle keyboard navigation
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN && searchResults.isShowing()) {
                searchResultsList.requestFocus();
                searchResultsList.getSelectionModel().selectFirst();
            } else if (event.getCode() == KeyCode.ENTER && searchResults.isShowing()) {
                String selectedApp = searchResultsList.getSelectionModel().getSelectedItem();
                if (selectedApp != null) {
                    launchApp(selectedApp);
                    searchField.clear();
                    searchResults.hide();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
                searchResults.hide();
            }
        });
        
        searchResultsList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String selectedApp = searchResultsList.getSelectionModel().getSelectedItem();
                if (selectedApp != null) {
                    launchApp(selectedApp);
                    searchField.clear();
                    searchResults.hide();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
                searchResults.hide();
            }
            event.consume();
        });
    }
    
    @FXML
    private void showStartMenu() {
        // We'll keep this simple since search is now in taskbar
        // You can customize this for other start menu features
    }
    
    public void launchApp(String appName) {
        if (runningApps.containsKey(appName)) {
            runningApps.get(appName).focus();
            return;
        }
        
        try {
            JavOSWindow window = createApp(appName);
            if (window != null) {
                runningApps.put(appName, window);
                desktopPane.getChildren().add(window);
                
                // Add to taskbar
                Button taskButton = new Button(appName);
                taskButton.getStyleClass().add("taskbar-item");
                taskButton.setOnAction(e -> window.focus());
                taskbarItems.getChildren().add(taskButton);
                
                window.setOnClose(() -> {
                    desktopPane.getChildren().remove(window);
                    taskbarItems.getChildren().remove(taskButton);
                    runningApps.remove(appName);
                });
            }
        } catch (Exception e) {
            showError("Failed to launch " + appName);
        }
    }
    
    private JavOSWindow createApp(String appName) {
        return switch (appName) {
            case "Terminal" -> new Terminal(this);
            case "Notepad" -> new Notepad(this);
            case "Weather" -> new Weather(this);
            // Add other apps here as they are implemented
            default -> null;
        };
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 