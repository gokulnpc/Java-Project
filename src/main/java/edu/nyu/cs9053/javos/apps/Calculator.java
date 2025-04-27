package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.effect.BoxBlur;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.scene.layout.Region;

public class Calculator extends JavOSWindow {
    private TextField display;
    private StringBuilder expression = new StringBuilder();
    private boolean errorState = false;
    private double currentValue = 0;
    private String currentInput = "";
    private String lastOperator = "";
    private double lastOperand = 0;
    private boolean startNewInput = true;

    public Calculator(DesktopController desktop) {
        super("Calculator", desktop);
        setPrefWidth(480);
        setPrefHeight(650);
    }

    @Override
    protected Region createContent() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(10, 40, 40, 40));
        content.setStyle("-fx-background-color: rgba(44, 62, 80, 0.55); -fx-background-radius: 32; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.18), 40, 0.2, 0, 8);");
        content.setAlignment(Pos.TOP_CENTER);
        VBox.setMargin(content, new Insets(40, 0, 0, 0));

        display = new TextField();
        display.setFont(Font.font("Consolas", 36));
        display.setEditable(false);
        display.setFocusTraversable(false);
        display.setStyle("-fx-background-color: rgba(30,34,44,0.7); -fx-text-fill: #fff; -fx-border-color: #00e6d0; -fx-border-width: 3; -fx-background-radius: 18; -fx-border-radius: 18; -fx-padding: 18; -fx-alignment: center-right; -fx-effect: dropshadow(gaussian, #00e6d0, 18, 0.2, 0, 0);");
        display.setPrefHeight(60);
        VBox.setVgrow(display, Priority.NEVER);

        // Scientific row
        GridPane sciGrid = new GridPane();
        sciGrid.setHgap(18);
        sciGrid.setVgap(18);
        sciGrid.setAlignment(Pos.CENTER);
        String[][] sciButtons = {
            {"sin", "cos", "tan", "ln", "log"},
            {"√", "x²", "xʸ", "π", "e"},
            {"1/x", "%", "!", "+/-", "C"}
        };
        for (int row = 0; row < sciButtons.length; row++) {
            for (int col = 0; col < sciButtons[row].length; col++) {
                String text = sciButtons[row][col];
                Button btn = createButton(text, true);
                sciGrid.add(btn, col, row);
            }
        }

        // Standard row
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setAlignment(Pos.CENTER);
        String[][] buttons = {
            {"7", "8", "9", "/"},
            {"4", "5", "6", "*"},
            {"1", "2", "3", "-"},
            {"0", ".", "=", "+"}
        };
        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String text = buttons[row][col];
                Button btn = createButton(text, false);
                grid.add(btn, col, row);
            }
        }

        content.getChildren().addAll(display, sciGrid, grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
        resetAll();
        return content;
    }

    private Button createButton(String text, boolean scientific) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Consolas", 22));
        btn.setPrefSize(70, 70);
        btn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.10);
            -fx-text-fill: #fff;
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-width: 2;
            -fx-border-color: transparent;
            -fx-effect: dropshadow(gaussian, #00e6d0, 8, 0.15, 0, 2);
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);
        btn.setOnAction(e -> handleButton(text));
        btn.setOnMouseEntered(ev -> btn.setStyle("""
            -fx-background-color: rgba(0,230,208,0.18);
            -fx-text-fill: #fff;
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-width: 2;
            -fx-border-color: #00e6d0;
            -fx-effect: dropshadow(gaussian, #00e6d0, 16, 0.25, 0, 2);
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """));
        btn.setOnMouseExited(ev -> btn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.10);
            -fx-text-fill: #fff;
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-width: 2;
            -fx-border-color: transparent;
            -fx-effect: dropshadow(gaussian, #00e6d0, 8, 0.15, 0, 2);
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """));
        btn.setOnMousePressed(ev -> btn.setStyle("""
            -fx-background-color: #00e6d0;
            -fx-text-fill: #222;
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-width: 2;
            -fx-border-color: #00e6d0;
            -fx-effect: dropshadow(gaussian, #00e6d0, 24, 0.35, 0, 2);
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """));
        btn.setOnMouseReleased(ev -> btn.setStyle("""
            -fx-background-color: rgba(0,230,208,0.18);
            -fx-text-fill: #fff;
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-width: 2;
            -fx-border-color: #00e6d0;
            -fx-effect: dropshadow(gaussian, #00e6d0, 16, 0.25, 0, 2);
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """));
        if (text.equals("C")) {
            btn.setStyle(btn.getStyle() + "-fx-background-color: rgba(231,76,60,0.18); -fx-border-color: #e74c3c;");
            btn.setOnMouseEntered(ev -> btn.setStyle(btn.getStyle() + "-fx-background-color: #e74c3c; -fx-text-fill: #fff;"));
            btn.setOnMouseExited(ev -> btn.setStyle("-fx-background-color: rgba(231,76,60,0.18); -fx-text-fill: #fff; -fx-background-radius: 22; -fx-border-radius: 22; -fx-border-width: 2; -fx-border-color: #e74c3c; -fx-effect: dropshadow(gaussian, #e74c3c, 8, 0.15, 0, 2); -fx-font-weight: bold; -fx-cursor: hand;"));
        }
        return btn;
    }

    public void handleButton(String text) {
        if (errorState) {
            resetAll();
        }
        if (isScientific(text)) {
            handleScientific(text);
            return;
        }
        switch (text) {
            case "C":
                resetAll();
                break;
            case "+": case "-": case "*": case "/":
                handleOperator(text);
                break;
            case "=":
                handleEquals();
                break;
            case ".":
                if (!currentInput.contains(".")) {
                    if (currentInput.isEmpty()) currentInput = "0";
                    currentInput += ".";
                    display.setText(currentInput);
                }
                break;
            case "+/-":
                if (!currentInput.isEmpty()) {
                    if (currentInput.startsWith("-"))
                        currentInput = currentInput.substring(1);
                    else
                        currentInput = "-" + currentInput;
                    display.setText(currentInput);
                }
                break;
            default: // numbers
                if (startNewInput) {
                    currentInput = text;
                    startNewInput = false;
                } else {
                    currentInput += text;
                }
                display.setText(currentInput);
                break;
        }
    }

    private void handleOperator(String op) {
        if (!currentInput.isEmpty()) {
            if (!lastOperator.isEmpty()) {
                currentValue = calculate(currentValue, Double.parseDouble(currentInput), lastOperator);
            } else {
                currentValue = Double.parseDouble(currentInput);
            }
            lastOperand = Double.parseDouble(currentInput);
            display.setText(formatResult(currentValue));
            currentInput = "";
        }
        lastOperator = op;
        startNewInput = true;
    }

    private void handleEquals() {
        if (!lastOperator.isEmpty()) {
            double operand = !currentInput.isEmpty() ? Double.parseDouble(currentInput) : lastOperand;
            currentValue = calculate(currentValue, operand, lastOperator);
            display.setText(formatResult(currentValue));
            lastOperand = operand;
            currentInput = "";
            startNewInput = true;
        }
    }

    private void handleScientific(String text) {
        try {
            double value = currentInput.isEmpty() ? currentValue : Double.parseDouble(currentInput);
            double result = switch (text) {
                case "sin" -> Math.sin(Math.toRadians(value));
                case "cos" -> Math.cos(Math.toRadians(value));
                case "tan" -> Math.tan(Math.toRadians(value));
                case "ln" -> Math.log(value);
                case "log" -> Math.log10(value);
                case "√" -> Math.sqrt(value);
                case "x²" -> Math.pow(value, 2);
                case "xʸ" -> { lastOperator = "^"; lastOperand = value; currentInput = ""; startNewInput = true; yield value; }
                case "π" -> Math.PI;
                case "e" -> Math.E;
                case "1/x" -> 1.0 / value;
                case "%" -> value / 100.0;
                case "!" -> factorial(value);
                default -> value;
            };
            if (text.equals("xʸ")) {
                // Wait for next input and operator
                return;
            }
            currentValue = result;
            display.setText(formatResult(result));
            currentInput = formatResult(result);
            startNewInput = true;
        } catch (Exception e) {
            display.setText("Error");
            errorState = true;
        }
    }

    private boolean isScientific(String text) {
        return List.of("sin", "cos", "tan", "ln", "log", "√", "x²", "xʸ", "π", "e", "1/x", "%", "!", "+/-").contains(text);
    }

    private void resetAll() {
        currentValue = 0;
        currentInput = "";
        lastOperator = "";
        lastOperand = 0;
        startNewInput = true;
        errorState = false;
        display.setText("0");
    }

    private double calculate(double a, double b, String op) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : Double.NaN;
            case "^" -> Math.pow(a, b);
            default -> b;
        };
    }

    private String formatResult(double result) {
        if (Double.isNaN(result) || Double.isInfinite(result)) return "Error";
        BigDecimal bd = new BigDecimal(result).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString();
    }

    private double factorial(double n) {
        if (n < 0 || n != Math.floor(n)) throw new ArithmeticException();
        double res = 1;
        for (int i = 2; i <= (int) n; i++) res *= i;
        return res;
    }
} 