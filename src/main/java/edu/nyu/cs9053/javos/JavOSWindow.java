package edu.nyu.cs9053.javos;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class JavOSWindow extends VBox {
    private final String title;
    private final DesktopController desktop;
    private Point2D dragDelta;
    private Runnable onCloseHandler;
    private boolean isMaximized = false;
    private double savedX, savedY, savedWidth, savedHeight;
    private Label titleLabel;
    
    public JavOSWindow(String title, DesktopController desktop) {
        this.title = title;
        this.desktop = desktop;
        this.titleLabel = new Label(title);
        
        getStyleClass().add("javos-window");
        setupWindow();
    }
    
    private void setupWindow() {
        // Window title bar
        HBox titleBar = new HBox(8); // 8px spacing between elements
        titleBar.getStyleClass().add("window-title-bar");
        
        // Window controls (macOS style)
        HBox windowControls = new HBox(8);
        windowControls.getStyleClass().add("window-controls");
        
        Button closeBtn = new Button();
        closeBtn.getStyleClass().addAll("window-button", "close-button");
        closeBtn.setOnAction(e -> close());
        
        Button minimizeBtn = new Button();
        minimizeBtn.getStyleClass().addAll("window-button", "minimize-button");
        minimizeBtn.setOnAction(e -> minimize());
        
        Button maximizeBtn = new Button();
        maximizeBtn.getStyleClass().addAll("window-button", "maximize-button");
        maximizeBtn.setOnAction(e -> toggleMaximize());
        
        windowControls.getChildren().addAll(closeBtn, minimizeBtn, maximizeBtn);
        
        titleLabel.getStyleClass().add("window-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        titleBar.getChildren().addAll(windowControls, titleLabel);
        
        // Make window draggable
        titleBar.setOnMousePressed(e -> {
            if (!isMaximized) {
                dragDelta = new Point2D(e.getSceneX() - getLayoutX(), e.getSceneY() - getLayoutY());
            }
            e.consume();
        });
        
        titleBar.setOnMouseDragged(e -> {
            if (!isMaximized) {
                setLayoutX(e.getSceneX() - dragDelta.getX());
                setLayoutY(e.getSceneY() - dragDelta.getY());
            }
            e.consume();
        });
        
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleMaximize();
            }
        });
        
        // Content area
        Region content = createContent();
        content.getStyleClass().add("window-content");
        VBox.setVgrow(content, Priority.ALWAYS);
        
        getChildren().addAll(titleBar, content);
        
        // Get screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Save initial position for restore
        savedX = 50;
        savedY = 50;
        savedWidth = 600;
        savedHeight = 400;
        
        // Start maximized
        setLayoutX(0);
        setLayoutY(0);
        setPrefWidth(screenBounds.getWidth());
        setPrefHeight(screenBounds.getHeight() - 40); // Leave space for taskbar
        isMaximized = true;
    }
    
    private void toggleMaximize() {
        if (!isMaximized) {
            // Save current size and position
            savedX = getLayoutX();
            savedY = getLayoutY();
            savedWidth = getWidth();
            savedHeight = getHeight();
            
            // Get screen bounds
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Maximize
            setLayoutX(0);
            setLayoutY(0);
            setPrefWidth(screenBounds.getWidth());
            setPrefHeight(screenBounds.getHeight() - 40); // Leave space for taskbar
            
            isMaximized = true;
        } else {
            // Restore
            setLayoutX(savedX);
            setLayoutY(savedY);
            setPrefWidth(savedWidth);
            setPrefHeight(savedHeight);
            
            isMaximized = false;
        }
    }
    
    protected Region createContent() {
        // This will be overridden by specific application windows
        return new Region();
    }
    
    public void minimize() {
        setVisible(false);
    }
    
    public void focus() {
        toFront();
        setVisible(true);
    }
    
    public void close() {
        if (onCloseHandler != null) {
            onCloseHandler.run();
        }
    }
    
    public void setOnClose(Runnable handler) {
        this.onCloseHandler = handler;
    }

    protected void setTitle(String newTitle) {
        titleLabel.setText(newTitle);
    }

    public boolean isOpen() {
        return getScene() != null && isVisible();
    }
} 