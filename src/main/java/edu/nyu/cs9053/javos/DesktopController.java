package edu.nyu.cs9053.javos;

import edu.nyu.cs9053.javos.apps.Terminal;
import edu.nyu.cs9053.javos.apps.Notepad;
import edu.nyu.cs9053.javos.apps.Weather;
import edu.nyu.cs9053.javos.apps.Calendar;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.KeyCode;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.effect.GaussianBlur;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import edu.nyu.cs9053.javos.apps.ProcessManager;
import edu.nyu.cs9053.javos.widgets.NewsTicker;

public class DesktopController {
    @FXML private StackPane desktopPane;
    @FXML private HBox taskbarItems;
    @FXML private Label clockLabel;
    @FXML private TextField searchField;
    @FXML private Button startButton;
    
    private final Map<String, JavOSWindow> runningApps = new HashMap<>();
    private Popup searchResults;
    private ListView<String> searchResultsList;
    private VBox launcherMenu;
    private boolean isLauncherVisible = false;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ObservableList<String> allApps = FXCollections.observableArrayList(
        "Terminal", "Notepad", "Calculator", "Weather", "Calendar"
    );
    
    private final ObservableList<AppInfo> availableApps = FXCollections.observableArrayList(
        new AppInfo("Terminal", "terminal-icon.png"),
        new AppInfo("Notepad", "notepad-icon.png"),
        new AppInfo("Calculator", "calculator-icon.png"),
        new AppInfo("Weather", "weather-icon.png"),
        new AppInfo("Calendar", "calendar-icon.png")
    );

    private static class AppInfo {
        private final String name;
        private final String iconPath;

        public AppInfo(String name, String iconPath) {
            this.name = name;
            this.iconPath = iconPath;
        }

        public String getName() { return name; }
        public String getIconPath() { return iconPath; }
    }
    
    @FXML
    public void initialize() {
        setupClock();
        setupSearch();
        setupLauncherMenu();
        setupHomeButton();
        
        // Add the news ticker to the taskbar
        NewsTicker newsTicker = new NewsTicker();
        taskbarItems.getChildren().add(newsTicker);
        
        // Add click handler to the desktop pane to close menus
        desktopPane.setOnMouseClicked(event -> {
            // Only close if click is directly on the desktop pane
            if (event.getTarget() == desktopPane) {
                if (searchResults != null && searchResults.isShowing()) {
                    searchField.clear();
                    searchResults.hide();
                }
                if (isLauncherVisible) {
                    hideLauncher();
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
    
    private void setupLauncherMenu() {
        launcherMenu = new VBox();
        launcherMenu.setAlignment(Pos.CENTER);
        launcherMenu.setSpacing(20);
        launcherMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 40;");
        
        // Create grid for apps
        GridPane appGrid = new GridPane();
        appGrid.setHgap(40);
        appGrid.setVgap(40);
        appGrid.setAlignment(Pos.CENTER);
        
        int col = 0;
        int row = 0;
        final int COLS = 4;
        
        for (AppInfo app : availableApps) {
            VBox appBox = createAppIcon(app);
            appGrid.add(appBox, col, row);
            
            col++;
            if (col >= COLS) {
                col = 0;
                row++;
            }
        }
        
        launcherMenu.getChildren().add(appGrid);
        
        // Initially hide the launcher
        launcherMenu.setVisible(false);
        launcherMenu.setMouseTransparent(false);
        
        // Prevent click events from reaching the desktop pane
        launcherMenu.setOnMouseClicked(event -> {
            event.consume();
        });
    }
    
    private VBox createAppIcon(AppInfo app) {
        VBox appBox = new VBox(10);
        appBox.setAlignment(Pos.CENTER);
        appBox.setPrefWidth(120);
        appBox.setPrefHeight(120);
        
        // Create app icon
        StackPane iconContainer = new StackPane();
        Rectangle iconBg = new Rectangle(80, 80);
        iconBg.setArcWidth(20);
        iconBg.setArcHeight(20);
        
        // Set different colors for different apps
        switch (app.getName()) {
            case "Terminal":
                iconBg.setStyle("-fx-fill: #34495e;");
                break;
            case "Notepad":
                iconBg.setStyle("-fx-fill: #2ecc71;");
                break;
            case "Calculator":
                iconBg.setStyle("-fx-fill: #e74c3c;");
                break;
            case "Weather":
                iconBg.setStyle("-fx-fill: #3498db;");
                break;
            case "Calendar":
                iconBg.setStyle("-fx-fill: #95a5a6;");
                break;
            default:
                iconBg.setStyle("-fx-fill: #95a5a6;");
        }
        
        // Add icon text (first letter of app name)
        Label iconLabel = new Label(app.getName().substring(0, 1).toUpperCase());
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        
        iconContainer.getChildren().addAll(iconBg, iconLabel);
        
        // App name label
        Label nameLabel = new Label(app.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        appBox.getChildren().addAll(iconContainer, nameLabel);
        
        // Add hover effect with transition
        appBox.setOnMouseEntered(e -> {
            iconContainer.setScaleX(1.1);
            iconContainer.setScaleY(1.1);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, #3498db, 10, 0, 0, 0);");
        });
        
        appBox.setOnMouseExited(e -> {
            iconContainer.setScaleX(1.0);
            iconContainer.setScaleY(1.0);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        });
        
        // Add click handler with visual feedback
        appBox.setOnMouseClicked(event -> {
            event.consume(); // Prevent event from reaching desktop pane
            iconContainer.setScaleX(0.9);
            iconContainer.setScaleY(0.9);
            
            // Use a small delay to show the click animation
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                iconContainer.setScaleX(1.0);
                iconContainer.setScaleY(1.0);
                launchApp(app.getName());
                hideLauncher();
            }));
            timeline.play();
        });
        
        return appBox;
    }
    
    private void showLauncher() {
        // Remove launcher if it's already in the scene
        desktopPane.getChildren().remove(launcherMenu);
        
        // Add blur effect to desktop background
        desktopPane.getChildren().stream()
            .filter(node -> node != launcherMenu)
            .forEach(node -> node.setEffect(new GaussianBlur(10)));
        
        // Add launcher on top
        desktopPane.getChildren().add(launcherMenu);
        launcherMenu.setVisible(true);
        launcherMenu.setMouseTransparent(false);
        launcherMenu.toFront();
        isLauncherVisible = true;
    }
    
    private void hideLauncher() {
        // Remove blur effect
        desktopPane.getChildren().forEach(node -> node.setEffect(null));
        
        launcherMenu.setVisible(false);
        isLauncherVisible = false;
        
        // Remove launcher from scene
        desktopPane.getChildren().remove(launcherMenu);
    }
    
    @FXML
    private void showStartMenu() {
        if (isLauncherVisible) {
            hideLauncher();
        } else {
            showLauncher();
        }
    }
    
    public void launchApp(String appName) {
        try {
            // Launch each app in its own thread
            Runnable appTask = () -> {
                JavOSWindow[] windowRef = new JavOSWindow[1];
                javafx.application.Platform.runLater(() -> {
                    JavOSWindow window = createApp(appName);
                    windowRef[0] = window;
                    if (window != null) {
                        desktopPane.getChildren().add(window);
                        window.toFront();
                        // Add to taskbar
                        Button taskButton = new Button(appName);
                        taskButton.getStyleClass().add("taskbar-item");
                        taskButton.setOnAction(e -> {
                            if (!window.isVisible()) {
                                window.setVisible(true);
                                window.toFront();
                                window.focus();
                            } else if (window.isFocused()) {
                                window.setVisible(false);
                            } else {
                                window.toFront();
                                window.focus();
                            }
                        });
                        window.focusedProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal) {
                                taskButton.getStyleClass().add("taskbar-item-focused");
                            } else {
                                taskButton.getStyleClass().remove("taskbar-item-focused");
                            }
                        });
                        window.visibleProperty().addListener((obs, oldVal, newVal) -> {
                            if (!newVal) {
                                taskButton.getStyleClass().add("taskbar-item-minimized");
                            } else {
                                taskButton.getStyleClass().remove("taskbar-item-minimized");
                            }
                        });
                        taskbarItems.getChildren().add(taskButton);
                        window.setOnClose(() -> {
                            desktopPane.getChildren().remove(window);
                            taskbarItems.getChildren().remove(taskButton);
                        });
                    }
                });
                // Wait for window to be created
                while (windowRef[0] == null) {
                    try { Thread.sleep(10); } catch (InterruptedException e) { return; }
                }
                // Register with ProcessManager
                int pid = ProcessManager.getInstance().startProcess(appName, Thread.currentThread(), windowRef[0]);
                // Keep thread alive until window is closed or interrupted
                while (windowRef[0].isOpen() && !Thread.currentThread().isInterrupted()) {
                    try { Thread.sleep(100); } catch (InterruptedException e) { break; }
                }
                // Cleanup: close window if not already closed
                if (windowRef[0].isOpen()) {
                    javafx.application.Platform.runLater(windowRef[0]::close);
                }
            };
            Thread appThread = new Thread(appTask, appName + "-Thread");
            appThread.setDaemon(false);
            appThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to launch " + appName + ": " + e.getMessage());
        }
    }
    
    private JavOSWindow createApp(String appName) {
        try {
            System.out.println("Attempting to create app: " + appName);
            JavOSWindow window = switch (appName) {
                case "Terminal" -> new Terminal(this);
                case "Notepad" -> new Notepad(this);
                case "Weather" -> new Weather(this);
                case "Calendar" -> new Calendar(this);
                case "Calculator" -> new edu.nyu.cs9053.javos.apps.Calculator(this);
                default -> null;
            };
            System.out.println("App created: " + appName + " => " + (window != null));
            return window;
        } catch (Exception e) {
            System.out.println("Exception while creating app: " + appName);
            e.printStackTrace();
            showError("Failed to create " + appName + ": " + e.getMessage());
            return null;
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupHomeButton() {
        Button homeButton = new Button("\u2302"); // House symbol
        homeButton.getStyleClass().add("taskbar-home");
        homeButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-font-size: 20px;
            -fx-padding: 5 15;
            -fx-cursor: hand;
            """);
        
        // Add hover effect
        homeButton.setOnMouseEntered(e -> 
            homeButton.setStyle("""
                -fx-background-color: rgba(52, 152, 219, 0.3);
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-padding: 5 15;
                -fx-cursor: hand;
                """)
        );
        
        homeButton.setOnMouseExited(e -> 
            homeButton.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-padding: 5 15;
                -fx-cursor: hand;
                """)
        );
        
        // Add click handler to minimize all windows
        homeButton.setOnAction(e -> minimizeAllWindows());
        
        // Add the home button at the beginning of the taskbar items
        taskbarItems.getChildren().add(0, homeButton);
    }
    
    private void minimizeAllWindows() {
        runningApps.values().forEach(window -> {
            if (window.isVisible()) {
                window.minimize();
            }
        });
    }
} 