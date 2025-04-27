package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class FileExplorer extends JavOSWindow {
    private ListView<HBox> fileListView;
    private final DesktopController desktop;
    private final File rootDir = new File(System.getProperty("user.home"), "JavOSFiles");

    public FileExplorer(DesktopController desktop) {
        super("File Explorer", desktop);
        this.desktop = desktop;
        setPrefWidth(700);
        setPrefHeight(500);

        HBox root = new HBox();
        root.setSpacing(0);
        root.setStyle("-fx-background-color: #f5f6fa;");

        // Sidebar
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 10, 30, 20));
        sidebar.setStyle("-fx-background-color: #2d3436; -fx-min-width: 160px; -fx-background-radius: 0 12 12 0;");
        sidebar.setAlignment(Pos.TOP_LEFT);
        Label home = new Label("Home");
        home.setStyle("-fx-text-fill: #fff; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label docs = new Label("Documents");
        docs.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 15px;");
        Label downloads = new Label("Downloads");
        downloads.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 15px;");
        sidebar.getChildren().addAll(home, docs, downloads);

        // Main content
        VBox content = new VBox(15);
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 12;");
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("JavOS File Explorer");
        title.setFont(Font.font("Consolas", 22));
        title.setStyle("-fx-text-fill: #2d3436; -fx-font-weight: bold;");
        content.getChildren().add(title);

        fileListView = new ListView<>();
        fileListView.setStyle("-fx-background-color: #fff; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
        fileListView.setPrefHeight(350);
        content.getChildren().add(fileListView);
        VBox.setVgrow(fileListView, Priority.ALWAYS);

        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshFileList());
        buttonBar.getChildren().add(refreshBtn);
        content.getChildren().add(buttonBar);

        root.getChildren().addAll(sidebar, content);
        getChildren().clear();
        getChildren().add(root);
        refreshFileList();
    }

    private void refreshFileList() {
        fileListView.getItems().clear();
        if (!rootDir.exists()) rootDir.mkdirs();
        File[] files = rootDir.listFiles((dir, name) -> !new File(dir, name).isDirectory());
        if (files == null || files.length == 0) {
            HBox emptyRow = new HBox();
            Label empty = new Label("No files in ~/JavOSFiles.");
            empty.setStyle("-fx-text-fill: #636e72; -fx-font-size: 15px;");
            emptyRow.getChildren().add(empty);
            fileListView.getItems().add(emptyRow);
            return;
        }
        for (File file : files) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            // File icon with fallback
            InputStream iconStream = getClass().getResourceAsStream("/edu/nyu/cs9053/javos/icons/file-icon.png");
            ImageView icon;
            if (iconStream != null) {
                icon = new ImageView(new Image(iconStream));
                icon.setFitWidth(24);
                icon.setFitHeight(24);
            } else {
                Label fallbackIcon = new Label("📄");
                fallbackIcon.setStyle("-fx-font-size: 20px;");
                icon = new ImageView();
                row.getChildren().add(fallbackIcon);
            }
            // File name
            Label name = new Label(file.getName());
            name.setStyle("-fx-text-fill: #2d3436; -fx-font-size: 16px; -fx-font-family: 'Consolas';");
            long sizeBytes = file.length();
            Label size = new Label("(" + sizeBytes + " bytes)");
            size.setStyle("-fx-text-fill: #636e72; -fx-font-size: 13px;");
            Button openBtn = new Button("Open");
            openBtn.setOnAction(e -> openInNotepad(file.getAbsolutePath()));
            Button delBtn = new Button("Delete");
            delBtn.setOnAction(e -> { file.delete(); refreshFileList(); });
            if (iconStream != null) {
                row.getChildren().addAll(icon, name, size, openBtn, delBtn);
            } else {
                row.getChildren().addAll(name, size, openBtn, delBtn);
            }
            fileListView.getItems().add(row);
        }
    }

    private void openInNotepad(String filePath) {
        // Launch Notepad and let user open the file manually
        desktop.launchApp("Notepad");
        // Optionally, you could pass the file path to Notepad if you add such support
    }
} 