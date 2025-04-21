package edu.nyu.cs9053.javos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class JavOS extends Application {
    private static final String LOGIN_FXML = "/edu/nyu/cs9053/javos/login.fxml";
    private static final String DESKTOP_FXML = "/edu/nyu/cs9053/javos/desktop.fxml";
    private static final String APP_TITLE = "JavOS";
    
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        // Set the stage to use the full screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
        
        showLoginScreen();
    }
    
    private void showLoginScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
        Parent root = loader.load();
        
        LoginController loginController = loader.getController();
        loginController.setOnLoginSuccess(() -> {
            try {
                showDesktop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // Center the login screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene loginScene = new Scene(root, 400, 300);
        loginScene.getStylesheets().add(getClass().getResource("/edu/nyu/cs9053/javos/styles.css").toExternalForm());
        
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(loginScene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }
    
    private void showDesktop() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(DESKTOP_FXML));
        Parent root = loader.load();
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene desktopScene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        desktopScene.getStylesheets().add(getClass().getResource("/edu/nyu/cs9053/javos/styles.css").toExternalForm());
        
        primaryStage.setScene(desktopScene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.initStyle(StageStyle.UNDECORATED);
    }

    public static void main(String[] args) {
        launch(args);
    }
} 