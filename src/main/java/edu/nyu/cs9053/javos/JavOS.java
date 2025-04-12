package edu.nyu.cs9053.javos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class JavOS extends Application {
    private static final String LOGIN_FXML = "/edu/nyu/cs9053/javos/login.fxml";
    private static final String DESKTOP_FXML = "/edu/nyu/cs9053/javos/desktop.fxml";
    private static final String APP_TITLE = "JavOS";
    private static final double DESKTOP_WIDTH = 1024;
    private static final double DESKTOP_HEIGHT = 768;
    
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
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
        
        Scene desktopScene = new Scene(root, DESKTOP_WIDTH, DESKTOP_HEIGHT);
        desktopScene.getStylesheets().add(getClass().getResource("/edu/nyu/cs9053/javos/styles.css").toExternalForm());
        
        primaryStage.setScene(desktopScene);
        primaryStage.setMaximized(true);
        primaryStage.initStyle(StageStyle.DECORATED);
    }

    public static void main(String[] args) {
        launch(args);
    }
} 