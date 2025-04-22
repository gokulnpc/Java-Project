package edu.nyu.cs9053.javos.apps;

import edu.nyu.cs9053.javos.JavOSWindow;
import edu.nyu.cs9053.javos.DesktopController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
        contentBox = new VBox(10);
        contentBox.setPadding(new Insets(15));
        contentBox.setStyle("-fx-background-color: #2f3640;");
        
        // Initialize monthYearLabel first
        monthYearLabel = new Label();
        monthYearLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        updateMonthYearLabel();
        
        // Create header with navigation
        HBox header = createHeader();
        
        // Initialize calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setStyle("-fx-background-color: rgba(53, 59, 72, 0.5); -fx-background-radius: 8;");
        calendarGrid.setPadding(new Insets(10));
        
        // Initialize event list view
        eventListView = new VBox(10);
        eventListView.setStyle("-fx-background-color: rgba(53, 59, 72, 0.5); -fx-background-radius: 8;");
        eventListView.setPadding(new Insets(10));
        
        // Create add event button
        Button addEventButton = new Button("Add Event");
        addEventButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-padding: 8 15;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        addEventButton.setOnAction(e -> showAddEventDialog());
        
        // Create split view for calendar and events
        HBox mainContent = new HBox(10);
        VBox calendarContent = new VBox(10);
        calendarContent.getChildren().addAll(calendarGrid);
        calendarContent.setPrefWidth(500);
        
        VBox eventContent = new VBox(10);
        Label eventsLabel = new Label("Events");
        eventsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        eventContent.getChildren().addAll(eventsLabel, eventListView, addEventButton);
        eventContent.setPrefWidth(280);
        
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
        
        Button prevMonth = new Button("←");
        Button nextMonth = new Button("→");
        
        styleNavigationButton(prevMonth);
        styleNavigationButton(nextMonth);
        
        monthYearLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        updateMonthYearLabel();
        
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
        VBox dayBox = new VBox(2);
        dayBox.setPrefSize(65, 65);
        dayBox.setPadding(new Insets(5));
        dayBox.setAlignment(Pos.TOP_CENTER);
        
        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        
        String baseStyle = "-fx-background-radius: 8; -fx-padding: 5; -fx-cursor: hand;";
        if (isSelected) {
            dayBox.setStyle(baseStyle + "-fx-background-color: rgba(52, 152, 219, 0.3);");
        } else if (isToday) {
            dayBox.setStyle(baseStyle + "-fx-background-color: rgba(46, 204, 113, 0.2);");
        } else {
            dayBox.setStyle(baseStyle + "-fx-background-color: rgba(53, 59, 72, 0.5);");
        }
        
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.setStyle("-fx-text-fill: white;");
        
        if (events.containsKey(date)) {
            Circle eventDot = new Circle(3, Color.DODGERBLUE);
            dayBox.getChildren().addAll(dateLabel, eventDot);
        } else {
            dayBox.getChildren().add(dateLabel);
        }
        
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
            -fx-background-color: rgba(52, 152, 219, 0.2);
            -fx-padding: 10;
            -fx-background-radius: 5;
            """);
        
        VBox eventDetails = new VBox(5);
        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label timeLabel = new Label(event.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #3498db;");
        
        eventDetails.getChildren().addAll(titleLabel, timeLabel);
        
        Button deleteButton = new Button("×");
        deleteButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #e74c3c;
            -fx-font-size: 16px;
            -fx-cursor: hand;
            """);
        deleteButton.setOnAction(e -> {
            events.get(selectedDate).remove(event);
            if (events.get(selectedDate).isEmpty()) {
                events.remove(selectedDate);
            }
            updateEventList();
            populateCalendar();
        });
        
        eventBox.getChildren().addAll(eventDetails, deleteButton);
        return eventBox;
    }
    
    private void showAddEventDialog() {
        Dialog<CalendarEvent> dialog = new Dialog<>();
        dialog.setTitle("Add Event");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2f3640;");
        
        // Create the form fields
        TextField titleField = new TextField();
        titleField.setPromptText("Event Title");
        titleField.setStyle("""
            -fx-background-color: #353b48;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #7f8c8d;
            """);
        
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, LocalTime.now().getMinute());
        
        HBox timeBox = new HBox(10);
        timeBox.getChildren().addAll(
            new Label("Time:"),
            hourSpinner,
            new Label(":"),
            minuteSpinner
        );
        timeBox.setStyle("-fx-text-fill: white;");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(titleField, timeBox);
        dialogPane.setContent(content);
        
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    LocalTime time = LocalTime.of(
                        hourSpinner.getValue(),
                        minuteSpinner.getValue()
                    );
                    return new CalendarEvent(title, time);
                }
            }
            return null;
        });
        
        Optional<CalendarEvent> result = dialog.showAndWait();
        result.ifPresent(event -> {
            events.computeIfAbsent(selectedDate, k -> new ArrayList<>()).add(event);
            updateEventList();
            populateCalendar();
        });
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