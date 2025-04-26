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

public class Calculator extends JavOSWindow {
    private TextField display;
    private StringBuilder expression = new StringBuilder();
    private boolean errorState = false;

    public Calculator(DesktopController desktop) {
        super("Calculator", desktop);
        setPrefWidth(380);
        setPrefHeight(540);
        VBox content = new VBox(30);
        content.setPadding(new Insets(40, 40, 40, 40));
        content.setStyle("-fx-background-color: rgba(44, 62, 80, 0.55); -fx-background-radius: 32; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.18), 40, 0.2, 0, 8);");
        content.setAlignment(Pos.TOP_CENTER);

        display = new TextField();
        display.setFont(Font.font("Consolas", 36));
        display.setEditable(false);
        display.setFocusTraversable(false);
        display.setStyle("-fx-background-color: rgba(30,34,44,0.7); -fx-text-fill: #fff; -fx-border-color: #00e6d0; -fx-border-width: 3; -fx-background-radius: 18; -fx-border-radius: 18; -fx-padding: 18; -fx-alignment: center-right; -fx-effect: dropshadow(gaussian, #00e6d0, 18, 0.2, 0, 0);");
        display.setPrefHeight(60);
        VBox.setVgrow(display, Priority.NEVER);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setAlignment(Pos.CENTER);

        String[][] buttons = {
            {"7", "8", "9", "/"},
            {"4", "5", "6", "*"},
            {"1", "2", "3", "-"},
            {"0", ".", "=", "+"},
            {"C", "", "", ""}
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String text = buttons[row][col];
                if (!text.isEmpty()) {
                    Button btn = new Button(text);
                    btn.setFont(Font.font("Consolas", 24));
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
                    grid.add(btn, col, row);
                }
            }
        }

        content.getChildren().addAll(display, grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
        getChildren().clear();
        getChildren().add(content);
    }

    public void handleButton(String text) {
        if (errorState) {
            // Reset on any input after error
            expression.setLength(0);
            display.setText("0");
            errorState = false;
        }
        switch (text) {
            case "C":
                display.setText("0");
                expression.setLength(0);
                break;
            case "=":
                if (expression.length() == 0) {
                    display.setText("0");
                    break;
                }
                try {
                    double result = evaluateExpression(expression.toString());
                    display.setText(formatResult(result));
                    expression = new StringBuilder(formatResult(result));
                } catch (Exception e) {
                    display.setText("Error");
                    errorState = true;
                }
                break;
            default:
                if (isValidInput(text)) {
                    expression.append(text);
                    display.setText(expression.toString());
                } else {
                    display.setText("Error");
                    errorState = true;
                }
                break;
        }
    }

    private boolean isValidInput(String input) {
        if (expression.length() == 0 && "+-*/.".contains(input)) {
            return false;
        }
        if ("+-*/".contains(input)) {
            // Prevent consecutive operators
            char last = expression.length() > 0 ? expression.charAt(expression.length() - 1) : ' ';
            if ("+-*/".indexOf(last) != -1) {
                return false;
            }
        }
        if (input.equals(".")) {
            // Prevent multiple decimals in a number
            int i = expression.length() - 1;
            while (i >= 0 && Character.isDigit(expression.charAt(i))) i--;
            if (i >= 0 && expression.charAt(i) == '.') return false;
        }
        return true;
    }

    // Shunting Yard Algorithm for BODMAS/PEMDAS
    private double evaluateExpression(String expr) throws Exception {
        List<String> rpn = toRPN(expr);
        Stack<Double> stack = new Stack<>();
        for (String token : rpn) {
            if (isNumber(token)) {
                stack.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if (stack.size() < 2) throw new Exception("Invalid expression");
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperator(a, b, token));
            }
        }
        if (stack.size() != 1) throw new Exception("Invalid expression");
        return stack.pop();
    }

    private List<String> toRPN(String expr) throws Exception {
        List<String> output = new ArrayList<>();
        Stack<String> ops = new Stack<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i));
                    i++;
                }
                output.add(num.toString());
                continue;
            } else if (isOperator(Character.toString(c))) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(Character.toString(c))) {
                    output.add(ops.pop());
                }
                ops.push(Character.toString(c));
            } else {
                throw new Exception("Invalid character");
            }
            i++;
        }
        while (!ops.isEmpty()) {
            output.add(ops.pop());
        }
        return output;
    }

    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/");
    }

    private int precedence(String op) {
        if (op.equals("+") || op.equals("-")) return 1;
        if (op.equals("*") || op.equals("/")) return 2;
        return 0;
    }

    private double applyOperator(double a, double b, String op) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : Double.NaN;
            default -> 0;
        };
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.format("%s", result);
        }
    }
} 