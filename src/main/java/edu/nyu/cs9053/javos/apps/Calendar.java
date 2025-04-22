package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.layout.Priority;

public class Calendar extends JavOSWindow {
    private VBox contentBox;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private YearMonth currentYearMonth;
    private Map<LocalDate, List<CalendarEvent>> events;
    private LocalDate selectedDate;
    private VBox eventListView;
    
    public Calendar(DesktopController desktop) {
        super("Calendar", desktop);
        setPrefWidth(800);
        setPrefHeight(600);
    }
    
    @Override
    protected Region createContent() {
        // Initialize all fields first
        this.currentYearMonth = YearMonth.now();
        this.events = new HashMap<>();
        this.selectedDate = LocalDate.now();
        this.monthYearLabel = new Label();
        
        // Create the main container
        contentBox = new VBox(20);
        contentBox.setPadding(new Insets(25));
        contentBox.setStyle("-fx-background-color: #2f3640;");
        
        // Initialize monthYearLabel first
        monthYearLabel = new Label();
        monthYearLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        updateMonthYearLabel();
        
        // Create header with navigation
        HBox header = createHeader();
        
        // Initialize calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(15);
        calendarGrid.setVgap(15);
        calendarGrid.setStyle("""
            -fx-background-color: rgba(53, 59, 72, 0.5);
            -fx-background-radius: 15;
            -fx-padding: 20;
            """);
        
        // Initialize event list view
        eventListView = new VBox(10);
        eventListView.setStyle("""
            -fx-background-color: rgba(53, 59, 72, 0.5);
            -fx-background-radius: 15;
            -fx-padding: 20;
            """);
        eventListView.setFillWidth(true);  // Ensure VBox stretches to fill width
        
        // Create add event button with modern styling
        Button addEventButton = new Button("Add Event");
        addEventButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 20;
            -fx-cursor: hand;
            """);
        
        addEventButton.setOnMouseEntered(e -> 
            addEventButton.setStyle("""
                -fx-background-color: #2980b9;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 10 20;
                -fx-background-radius: 20;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);
                """)
        );
        
        addEventButton.setOnMouseExited(e -> 
            addEventButton.setStyle("""
                -fx-background-color: #3498db;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 10 20;
                -fx-background-radius: 20;
                -fx-cursor: hand;
                """)
        );
        
        addEventButton.setOnAction(e -> showAddEventDialog());
        
        // Split view for calendar and events
        HBox mainContent = new HBox(20);
        VBox calendarContent = new VBox(20);
        calendarContent.getChildren().addAll(calendarGrid);
        calendarContent.setPrefWidth(600);
        
        VBox eventContent = new VBox(15);
        Label eventsLabel = new Label("Events");
        eventsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Wrap eventListView in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(eventListView);
        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-border-color: transparent;
            """);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        eventContent.getChildren().addAll(eventsLabel, scrollPane, addEventButton);
        eventContent.setPrefWidth(300);
        
        mainContent.getChildren().addAll(calendarContent, eventContent);
        contentBox.getChildren().addAll(header, mainContent);
        
        // Initialize views
        populateCalendar();
        updateEventList();
        
        return contentBox;
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        Button prevMonth = new Button("←");
        Button nextMonth = new Button("→");
        
        String buttonStyle = """
            -fx-background-color: rgba(52, 152, 219, 0.1);
            -fx-text-fill: #3498db;
            -fx-font-size: 18px;
            -fx-min-width: 40px;
            -fx-min-height: 40px;
            -fx-background-radius: 20;
            -fx-cursor: hand;
            """;
        
        String buttonHoverStyle = """
            -fx-background-color: rgba(52, 152, 219, 0.2);
            -fx-text-fill: #3498db;
            -fx-font-size: 18px;
            -fx-min-width: 40px;
            -fx-min-height: 40px;
            -fx-background-radius: 20;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.3), 10, 0, 0, 0);
            """;
        
        prevMonth.setStyle(buttonStyle);
        nextMonth.setStyle(buttonStyle);
        
        prevMonth.setOnMouseEntered(e -> prevMonth.setStyle(buttonHoverStyle));
        prevMonth.setOnMouseExited(e -> prevMonth.setStyle(buttonStyle));
        nextMonth.setOnMouseEntered(e -> nextMonth.setStyle(buttonHoverStyle));
        nextMonth.setOnMouseExited(e -> nextMonth.setStyle(buttonStyle));
        
        prevMonth.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateMonthYearLabel();
            populateCalendar();
        });
        
        nextMonth.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateMonthYearLabel();
            populateCalendar();
        });
        
        header.getChildren().addAll(monthYearLabel, prevMonth, nextMonth);
        return header;
    }
    
    private void styleNavigationButton(Button button) {
        button.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-min-width: 30px;
            -fx-min-height: 30px;
            -fx-background-radius: 15;
            -fx-cursor: hand;
            """);
    }
    
    private void updateMonthYearLabel() {
        // Add verification
        if (currentYearMonth == null) {
            System.err.println("Error: currentYearMonth is null in updateMonthYearLabel");
            return;
        }
        if (monthYearLabel == null) {
            System.err.println("Error: monthYearLabel is null in updateMonthYearLabel");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentYearMonth.format(formatter));
    }
    
    private void populateCalendar() {
        calendarGrid.getChildren().clear();
        
        // Add day of week headers
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(65);
            calendarGrid.add(dayLabel, i, 0);
        }
        
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1;
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                int dayIndex = (i * 7 + j) - dayOfWeek;
                LocalDate date = firstOfMonth.plusDays(dayIndex);
                
                if (date.getMonth() == currentYearMonth.getMonth()) {
                    VBox dayBox = createDayBox(date);
                    calendarGrid.add(dayBox, j, i + 1);
                }
            }
        }
    }
    
    private VBox createDayBox(LocalDate date) {
        VBox dayBox = new VBox(5);
        dayBox.setPrefSize(70, 70);
        dayBox.setPadding(new Insets(5));
        dayBox.setAlignment(Pos.TOP_CENTER);
        
        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        
        String baseStyle = """
            -fx-background-radius: 10;
            -fx-padding: 5;
            -fx-cursor: hand;
            -fx-alignment: center;
            """;
            
        if (isSelected) {
            dayBox.setStyle(baseStyle + "-fx-background-color: rgba(52, 152, 219, 0.3);");
        } else if (isToday) {
            dayBox.setStyle(baseStyle + "-fx-background-color: rgba(46, 204, 113, 0.2);");
        } else {
            dayBox.setStyle(baseStyle + "-fx-background-color: rgba(53, 59, 72, 0.5);");
        }
        
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        
        if (events.containsKey(date)) {
            Circle eventDot = new Circle(3, Color.DODGERBLUE);
            dayBox.getChildren().addAll(dateLabel, eventDot);
        } else {
            dayBox.getChildren().add(dateLabel);
        }
        
        // Add hover effect
        dayBox.setOnMouseEntered(e -> {
            if (!isSelected) {
                dayBox.setStyle(baseStyle + "-fx-background-color: rgba(52, 152, 219, 0.2);");
            }
        });
        
        dayBox.setOnMouseExited(e -> {
            if (!isSelected) {
                if (isToday) {
                    dayBox.setStyle(baseStyle + "-fx-background-color: rgba(46, 204, 113, 0.2);");
                } else {
                    dayBox.setStyle(baseStyle + "-fx-background-color: rgba(53, 59, 72, 0.5);");
                }
            }
        });
        
        dayBox.setOnMouseClicked(e -> {
            selectedDate = date;
            updateEventList();
            populateCalendar();
        });
        
        return dayBox;
    }
    
    private void updateEventList() {
        eventListView.getChildren().clear();
        
        Label dateLabel = new Label(selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        eventListView.getChildren().add(dateLabel);
        
        List<CalendarEvent> dateEvents = events.getOrDefault(selectedDate, new ArrayList<>());
        if (dateEvents.isEmpty()) {
            Label noEventsLabel = new Label("No events for this date");
            noEventsLabel.setStyle("-fx-text-fill: #95a5a6;");
            eventListView.getChildren().add(noEventsLabel);
        } else {
            dateEvents.sort(Comparator.comparing(CalendarEvent::getTime));
            for (CalendarEvent event : dateEvents) {
                eventListView.getChildren().add(createEventView(event));
            }
        }
    }
    
    private HBox createEventView(CalendarEvent event) {
        HBox eventBox = new HBox(10);
        eventBox.setStyle("""
            -fx-background-color: rgba(52, 152, 219, 0.15);
            -fx-padding: 15;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: rgba(52, 152, 219, 0.3);
            -fx-border-width: 1;
            """);
        
        VBox eventDetails = new VBox(5);
        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label timeLabel = new Label(event.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");
        
        eventDetails.getChildren().addAll(titleLabel, timeLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button deleteButton = new Button("×");
        deleteButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #e74c3c;
            -fx-font-size: 18px;
            -fx-cursor: hand;
            -fx-padding: 0 5;
            """);
        
        deleteButton.setOnMouseEntered(e -> 
            deleteButton.setStyle("""
                -fx-background-color: rgba(231, 76, 60, 0.1);
                -fx-text-fill: #e74c3c;
                -fx-font-size: 18px;
                -fx-cursor: hand;
                -fx-padding: 0 5;
                -fx-background-radius: 5;
                """)
        );
        
        deleteButton.setOnMouseExited(e -> 
            deleteButton.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #e74c3c;
                -fx-font-size: 18px;
                -fx-cursor: hand;
                -fx-padding: 0 5;
                """)
        );
        
        deleteButton.setOnAction(e -> {
            events.get(selectedDate).remove(event);
            if (events.get(selectedDate).isEmpty()) {
                events.remove(selectedDate);
            }
            updateEventList();
            populateCalendar();
        });
        
        eventBox.getChildren().addAll(eventDetails, spacer, deleteButton);
        return eventBox;
    }
    
    private void showAddEventDialog() {
        // Create overlay panel that will cover the entire window
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        
        // Set the overlay to fill the ENTIRE window
        modalOverlay.setPrefWidth(getWidth());
        modalOverlay.setPrefHeight(getHeight());
        modalOverlay.setLayoutX(0);
        modalOverlay.setLayoutY(0);
        
        // Create dialog container with REDUCED HEIGHT
        VBox dialogContainer = new VBox(10); // Reduced spacing between elements
        dialogContainer.setPadding(new Insets(25));
        dialogContainer.setStyle("""
            -fx-background-color: #2f3640;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 25, 0, 0, 5);
            -fx-border-color: rgba(52, 152, 219, 0.8);
            -fx-border-width: 1.5;
            -fx-border-radius: 15;
            """);
        dialogContainer.setMaxWidth(420);
        dialogContainer.setMaxHeight(500); // Set maximum height
        
        // Set up animation initial state
        dialogContainer.setScaleX(0.9);
        dialogContainer.setScaleY(0.9);
        dialogContainer.setOpacity(0.0);
        
        // Dialog content - MORE COMPACT
        Label titleLabel = new Label("Add New Event");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");
        
        Label dateLabel = new Label(selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dateLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 16px;");
        
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setStyle("-fx-background-color: rgba(52, 152, 219, 0.4);");
        VBox.setMargin(separator, new Insets(8, 0, 15, 0)); // Reduced margin
        
        TextField titleField = new TextField();
        titleField.setPromptText("Event Title");
        titleField.setStyle("""
            -fx-background-color: rgba(53, 59, 72, 0.8);
            -fx-text-fill: white;
            -fx-prompt-text-fill: #7f8c8d;
            -fx-padding: 12; // Reduced padding
            -fx-background-radius: 8;
            -fx-font-size: 15px;
            -fx-focus-color: #3498db;
            -fx-faint-focus-color: transparent;
            """);
        VBox.setMargin(titleField, new Insets(0, 0, 15, 0)); // Reduced margin
        
        HBox timeBox = new HBox(15);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label timeLabel = new Label("Time:");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        
        HBox spinnerContainer = new HBox(2);
        spinnerContainer.setStyle("""
            -fx-background-color: rgba(53, 59, 72, 0.8);
            -fx-background-radius: 8;
            -fx-padding: 8 15;
            """);
        spinnerContainer.setAlignment(Pos.CENTER);
        
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(60);
        styleSpinner(hourSpinner);
        
        Label timeSeparator = new Label(":");
        timeSeparator.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, LocalTime.now().getMinute());
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(60);
        styleSpinner(minuteSpinner);
        
        spinnerContainer.getChildren().addAll(hourSpinner, timeSeparator, minuteSpinner);
        timeBox.getChildren().addAll(timeLabel, spinnerContainer);
        VBox.setMargin(timeBox, new Insets(0, 0, 15, 0)); // Reduced margin
        
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        errorLabel.setVisible(false);
        errorLabel.setMinHeight(20);
        VBox.setMargin(errorLabel, new Insets(0, 0, 5, 0)); // Reduced margin
        
        // No spacer to reduce vertical space
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0)); // Reduced padding
        
        Button cancelButton = new Button("Cancel");
        styleButton(cancelButton, "-fx-background-color: rgba(231, 76, 60, 0.8);");
        cancelButton.setPrefWidth(130);
        cancelButton.setPrefHeight(40); // Reduced height
        
        Button addButton = new Button("Add Event");
        styleButton(addButton, "-fx-background-color: rgba(52, 152, 219, 0.8);");
        addButton.setPrefWidth(130);
        addButton.setPrefHeight(40); // Reduced height
        
        buttonBox.getChildren().addAll(cancelButton, addButton);
        
        dialogContainer.getChildren().addAll(
            titleLabel,
            dateLabel,
            separator,
            titleField,
            timeBox,
            errorLabel,
            buttonBox // No spacer before buttons
        );
        
        // Add dialog to overlay and center it
        modalOverlay.getChildren().add(dialogContainer);
        StackPane.setAlignment(dialogContainer, Pos.CENTER);
        
        // Critical: Add the overlay directly to this window (not contentBox)
        getChildren().add(modalOverlay);
        modalOverlay.toFront();
        
        // Animation to show the dialog
        Timeline timeline = new Timeline(
            new KeyFrame(
                Duration.millis(150),
                new KeyValue(dialogContainer.scaleXProperty(), 1.0),
                new KeyValue(dialogContainer.scaleYProperty(), 1.0),
                new KeyValue(dialogContainer.opacityProperty(), 1.0)
            )
        );
        timeline.play();
        
        // Event handlers remain the same as before
        cancelButton.setOnAction(e -> {
            Timeline closeTimeline = new Timeline(
                new KeyFrame(
                    Duration.millis(100),
                    new KeyValue(dialogContainer.opacityProperty(), 0.0),
                    new KeyValue(dialogContainer.scaleXProperty(), 0.9),
                    new KeyValue(dialogContainer.scaleYProperty(), 0.9)
                )
            );
            closeTimeline.setOnFinished(event -> getChildren().remove(modalOverlay));
            closeTimeline.play();
        });
        
        addButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            if (!title.isEmpty()) {
                LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
                CalendarEvent event = new CalendarEvent(title, time);
                
                // Add the event
                if (!events.containsKey(selectedDate)) {
                    events.put(selectedDate, new ArrayList<>());
                }
                events.get(selectedDate).add(event);
                
                // Animation before removing
                Timeline closeTimeline = new Timeline(
                    new KeyFrame(
                        Duration.millis(100),
                        new KeyValue(dialogContainer.opacityProperty(), 0.0),
                        new KeyValue(dialogContainer.scaleXProperty(), 0.9),
                        new KeyValue(dialogContainer.scaleYProperty(), 0.9)
                    )
                );
                closeTimeline.setOnFinished(event2 -> {
                    getChildren().remove(modalOverlay);
                    updateEventList();
                    populateCalendar();
                });
                closeTimeline.play();
            } else {
                // Show error message with animation
                errorLabel.setText("Please enter an event title");
                errorLabel.setVisible(true);
                errorLabel.setOpacity(0);
                
                Timeline errorTimeline = new Timeline(
                    new KeyFrame(
                        Duration.millis(200),
                        new KeyValue(errorLabel.opacityProperty(), 1.0)
                    )
                );
                errorTimeline.play();
                
                // Shake animation for error feedback
                TranslateTransition shake = new TranslateTransition(Duration.millis(50), titleField);
                shake.setFromX(0);
                shake.setByX(10);
                shake.setCycleCount(6);
                shake.setAutoReverse(true);
                shake.play();
            }
        });
        
        // Close when clicking outside
        modalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == modalOverlay) {
                Timeline closeTimeline = new Timeline(
                    new KeyFrame(
                        Duration.millis(100),
                        new KeyValue(dialogContainer.opacityProperty(), 0.0),
                        new KeyValue(dialogContainer.scaleXProperty(), 0.9),
                        new KeyValue(dialogContainer.scaleYProperty(), 0.9)
                    )
                );
                closeTimeline.setOnFinished(event -> getChildren().remove(modalOverlay));
                closeTimeline.play();
            }
        });
        
        // Focus on title field
        titleField.requestFocus();
    }

    private void styleSpinner(Spinner<?> spinner) {
        spinner.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            """);
        
        spinner.getEditor().setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-alignment: center;
            -fx-font-size: 15px;
            """);
        
        // Find and style the increment/decrement buttons
        spinner.lookupAll(".increment-arrow-button, .decrement-arrow-button").forEach(node -> {
            node.setStyle("""
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                -fx-padding: 0 5;
                """);
        });
        
        spinner.lookupAll(".increment-arrow, .decrement-arrow").forEach(node -> {
            node.setStyle("-fx-background-color: #3498db;");
        });
    }
    
    private void styleButton(Button button, String backgroundColor) {
        button.setStyle(backgroundColor + """
            ;
            -fx-text-fill: white;
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            -fx-background-radius: 8;
            """);
        
        button.setOnMouseEntered(e -> 
            button.setStyle(backgroundColor + """
                ;
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-background-radius: 8;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0);
                """)
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(backgroundColor + """
                ;
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-background-radius: 8;
                """)
        );
        
        // Add a pressed effect
        button.setOnMousePressed(e -> 
            button.setStyle(backgroundColor + """
                ;
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-background-radius: 8;
                -fx-translate-y: 1px;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 0);
                """)
        );
        
        button.setOnMouseReleased(e -> 
            button.setStyle(backgroundColor + """
                ;
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-background-radius: 8;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0);
                """)
        );
    }
    
    private static class CalendarEvent {
        private final String title;
        private final LocalTime time;
        
        public CalendarEvent(String title, LocalTime time) {
            this.title = title;
            this.time = time;
        }
        
        public String getTitle() {
            return title;
        }
        
        public LocalTime getTime() {
            return time;
        }
    }
    
    private static class Circle extends javafx.scene.shape.Circle {
        public Circle(double radius, Color color) {
            super(radius, color);
        }
    }
} 