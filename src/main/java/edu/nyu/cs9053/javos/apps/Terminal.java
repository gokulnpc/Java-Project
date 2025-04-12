package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.application.Platform;

public class Terminal extends JavOSWindow {
    private final TextArea consoleArea;
    private final StringBuilder currentCommand;
    private final CommandInterpreter interpreter;
    private String prompt = "javos$ ";
    private int commandStartPosition = 0;

    public Terminal(DesktopController desktop) {
        super("Terminal", desktop);
        this.currentCommand = new StringBuilder();
        this.interpreter = new CommandInterpreter(this);
        this.consoleArea = createConsoleArea();
        
        VBox content = new VBox(consoleArea);
        VBox.setVgrow(consoleArea, Priority.ALWAYS);
        getChildren().add(content);
        
        displayWelcomeMessage();
        showPrompt();
    }
    
    private TextArea createConsoleArea() {
        TextArea area = new TextArea();
        area.setStyle("-fx-control-inner-background: #2f3640;" +
                     "-fx-text-fill: #ecf0f1;" +
                     "-fx-highlight-fill: #3498db;" +
                     "-fx-highlight-text-fill: white;" +
                     "-fx-font-family: 'Monaco', 'Consolas', 'Courier New', monospace;" +
                     "-fx-font-size: 14px;");
        
        area.setWrapText(true);
        area.setEditable(false);
        
        // Handle key events
        area.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        
        return area;
    }
    
    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                executeCommand();
                event.consume();
                break;
            case BACK_SPACE:
                handleBackspace();
                event.consume();
                break;
            default:
                if (event.getText().matches("\\S")) {
                    appendToCommand(event.getText());
                    event.consume();
                }
                break;
        }
    }
    
    private void handleBackspace() {
        if (currentCommand.length() > 0) {
            currentCommand.setLength(currentCommand.length() - 1);
            String fullText = consoleArea.getText();
            consoleArea.setText(fullText.substring(0, fullText.length() - 1));
        }
    }
    
    private void appendToCommand(String text) {
        currentCommand.append(text);
        appendToConsole(text);
    }
    
    private void executeCommand() {
        String command = currentCommand.toString().trim();
        appendToConsole("\n");
        
        if (!command.isEmpty()) {
            interpreter.executeCommand(command);
        }
        
        currentCommand.setLength(0);
        showPrompt();
    }
    
    public void appendToConsole(String text) {
        Platform.runLater(() -> {
            consoleArea.appendText(text);
            consoleArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    public void appendToConsoleWithColor(String text, String color) {
        Platform.runLater(() -> {
            consoleArea.appendText(text);
            consoleArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private void showPrompt() {
        appendToConsole("\n" + prompt);
        commandStartPosition = consoleArea.getText().length();
    }
    
    private void displayWelcomeMessage() {
        String welcome = """
                        JavOS Terminal v1.0
                        Type 'help' for a list of available commands.
                        
                        """;
        appendToConsole(welcome);
    }
    
    public void clearConsole() {
        Platform.runLater(() -> {
            consoleArea.clear();
            showPrompt();
        });
    }
    
    public String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
} 