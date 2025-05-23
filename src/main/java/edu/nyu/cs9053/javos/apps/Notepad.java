package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.file.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.LinkedList;
import javafx.scene.control.TextInputDialog;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Notepad extends JavOSWindow {
    private TextArea textArea;
    private String currentFile = null;
    private boolean isModified = false;
    private final LinkedList<String> recentFiles = new LinkedList<>();
    private Timeline autoSaveTimeline;
    private Menu recentFilesMenu;

    public Notepad(DesktopController desktop) {
        super("Notepad", desktop);
        setupNotepad();
        setupAutoSave();
    }

    private void setupNotepad() {
        // Create menu bar
        MenuBar menuBar = createMenuBar();

        // Create text area
        textArea = new TextArea();
        textArea.setStyle("""
            -fx-control-inner-background: #2f3640;
            -fx-text-fill: #ecf0f1;
            -fx-highlight-fill: #3498db;
            -fx-highlight-text-fill: white;
            -fx-font-family: 'Monaco', 'Consolas', 'Courier New', monospace;
            -fx-font-size: 14px;
            """);
        
        textArea.setWrapText(true);
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!isModified) {
                isModified = true;
                updateTitle();
            }
        });

        // Layout
        VBox content = new VBox(menuBar, textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        getChildren().add(content);

        // Set initial size
        setPrefWidth(600);
        setPrefHeight(400);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #2f3640;");

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newFile = new MenuItem("New");
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        newFile.setOnAction(e -> newFile());

        MenuItem open = new MenuItem("Open");
        open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        open.setOnAction(e -> openFile());

        MenuItem save = new MenuItem("Save");
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        save.setOnAction(e -> saveFile(false));

        MenuItem saveAs = new MenuItem("Save As");
        saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        saveAs.setOnAction(e -> saveFile(true));

        // Recent Files submenu
        recentFilesMenu = new Menu("Recent Files");
        updateRecentFilesMenu();

        fileMenu.getItems().addAll(newFile, open, save, saveAs, new SeparatorMenuItem(), recentFilesMenu);

        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem cut = new MenuItem("Cut");
        cut.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
        cut.setOnAction(e -> textArea.cut());

        MenuItem copy = new MenuItem("Copy");
        copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copy.setOnAction(e -> textArea.copy());

        MenuItem paste = new MenuItem("Paste");
        paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
        paste.setOnAction(e -> textArea.paste());

        MenuItem selectAll = new MenuItem("Select All");
        selectAll.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN));
        selectAll.setOnAction(e -> textArea.selectAll());

        editMenu.getItems().addAll(cut, copy, paste, new SeparatorMenuItem(), selectAll);

        menuBar.getMenus().addAll(fileMenu, editMenu);
        return menuBar;
    }

    private void updateRecentFilesMenu() {
        recentFilesMenu.getItems().clear();
        if (recentFiles.isEmpty()) {
            MenuItem none = new MenuItem("(No recent files)");
            none.setDisable(true);
            recentFilesMenu.getItems().add(none);
        } else {
            for (String path : recentFiles) {
                MenuItem item = new MenuItem(path);
                item.setOnAction(e -> openRecentFile(path));
                recentFilesMenu.getItems().add(item);
            }
        }
    }

    private void addRecentFile(String path) {
        recentFiles.remove(path);
        recentFiles.addFirst(path);
        while (recentFiles.size() > 5) recentFiles.removeLast();
        updateRecentFilesMenu();
    }

    private void openRecentFile(String path) {
        if (checkSave()) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    textArea.setText(content);
                    currentFile = path;
                    isModified = false;
                    updateTitle();
                } catch (IOException e) {
                    showError("Failed to open file: " + e.getMessage());
                }
            } else {
                showError("File not found: " + path);
            }
        }
    }

    private void setupAutoSave() {
        autoSaveTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (isModified && currentFile != null) {
                try {
                    Files.write(Paths.get(currentFile), textArea.getText().getBytes());
                    isModified = false;
                    updateTitle();
                } catch (Exception ex) {
                    showError("Auto-save failed: " + ex.getMessage());
                }
            }
        }));
        autoSaveTimeline.setCycleCount(Timeline.INDEFINITE);
        autoSaveTimeline.play();
    }

    private void newFile() {
        if (checkSave()) {
            textArea.clear();
            currentFile = null;
            isModified = false;
            updateTitle();
        }
    }

    private void openFile() {
        if (checkSave()) {
            File dir = new File(System.getProperty("user.home"), "JavOSFiles");
            if (!dir.exists()) dir.mkdirs();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            fileChooser.setInitialDirectory(dir);
            File file = fileChooser.showOpenDialog(getScene().getWindow());
            if (file != null && file.exists()) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    textArea.setText(content);
                    currentFile = file.getAbsolutePath();
                    isModified = false;
                    updateTitle();
                    addRecentFile(currentFile);
                } catch (IOException e) {
                    showError("Failed to open file: " + e.getMessage());
                }
            }
        }
    }

    private void saveFile(boolean saveAs) {
        File dir = new File(System.getProperty("user.home"), "JavOSFiles");
        if (!dir.exists()) dir.mkdirs();
        if (currentFile == null || saveAs || !currentFile.startsWith(dir.getAbsolutePath())) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            fileChooser.setInitialDirectory(dir);
            fileChooser.setInitialFileName(currentFile != null ? new File(currentFile).getName() : "Untitled.txt");
            File file = fileChooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                // Force save in JavOSFiles
                if (!file.getParentFile().getAbsolutePath().equals(dir.getAbsolutePath())) {
                    showError("You must save files in ~/JavOSFiles");
                    return;
                }
                currentFile = file.getAbsolutePath();
            } else {
                return;
            }
        }
        try {
            Files.write(Paths.get(currentFile), textArea.getText().getBytes());
            isModified = false;
            updateTitle();
            addRecentFile(currentFile);
        } catch (IOException e) {
            showError("Error saving file: " + e.getMessage());
        }
    }

    private boolean checkSave() {
        if (isModified) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes");
            alert.setHeaderText("Do you want to save changes?");
            alert.setContentText("Your changes will be lost if you don't save them.");
            alert.getButtonTypes().setAll(
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL
            );

            ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
            if (result == ButtonType.YES) {
                saveFile(false);
                return !isModified; // Return true if save was successful
            }
            return result == ButtonType.NO;
        }
        return true;
    }

    private void updateTitle() {
        String fileName = currentFile != null ? 
            Path.of(currentFile).getFileName().toString() : 
            "Untitled";
        setTitle("Notepad - " + fileName + (isModified ? "*" : ""));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void close() {
        if (checkSave()) {
            super.close();
        }
    }
} 