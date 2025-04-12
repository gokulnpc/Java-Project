package edu.nyu.cs9053.javos;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DesktopController {
    @FXML private StackPane desktopPane;
    @FXML private HBox taskbarItems;
    @FXML private Label clockLabel;
    
    private final Map<String, JavOSWindow> runningApps = new HashMap<>();
    private Popup startMenu;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @FXML
    public void initialize() {
        setupClock();
        setupStartMenu();
    }
    
    private void setupClock() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime currentTime = LocalTime.now();
            clockLabel.setText(currentTime.format(timeFormatter));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
    private void setupStartMenu() {
        startMenu = new Popup();
        VBox menuContent = new VBox(5);
        menuContent.getStyleClass().add("start-menu");
        
        // Add application launchers
        String[] apps = {"Terminal", "Notepad", "Calculator"};
        for (String app : apps) {
            Button appButton = new Button(app);
            appButton.setOnAction(e -> launchApp(app));
            appButton.getStyleClass().add("start-menu-item");
            menuContent.getChildren().add(appButton);
        }
        
        startMenu.getContent().add(menuContent);
    }
    
    @FXML
    private void showStartMenu() {
        if (!startMenu.isShowing()) {
            startMenu.show(desktopPane.getScene().getWindow());
        } else {
            startMenu.hide();
        }
    }
    
    public void launchApp(String appName) {
        if (runningApps.containsKey(appName)) {
            runningApps.get(appName).focus();
            return;
        }
        
        try {
            JavOSWindow window = new JavOSWindow(appName, this);
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
            
        } catch (Exception e) {
            showError("Failed to launch " + appName);
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 