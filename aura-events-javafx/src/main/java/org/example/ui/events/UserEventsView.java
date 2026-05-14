package org.example.ui.events;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import org.example.aura.entities.Event;
import org.example.services.EventService;
import org.example.ui.MainLayout;

import java.util.List;

public class UserEventsView extends BorderPane {

    private final EventService service = new EventService();
    private final int currentUserId = MainLayout.CURRENT_USER_ID;

    private VBox eventsList;
    private String activeFilter = "all";
    private TextField searchField;

    private static final String[][] MOOD_FILTERS = {
            {"all", "Tous"}, {"focus", "Focus"}, {"energy", "Energy"},
            {"relax", "Relax"}, {"calme", "Calme"}
    };

    public UserEventsView() {
        setStyle("-fx-background-color: #0A0C0F;");
        buildUI();
        loadEvents();
    }

    private void buildUI() {
        //  TOP HEADER 
        VBox header = new VBox(20);
        header.setPadding(new Insets(36, 36, 20, 36));
        header.setStyle("-fx-background-color: #0A0C0F;");

        // Title row
        VBox titleBlock = new VBox(4);
        Label title = new Label("Decouvrir des evenements");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitle = new Label("Trouvez des evenements compatibles avec votre humeur du moment");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A6070;");
        titleBlock.getChildren().addAll(title, subtitle);

        // Search bar
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Rechercher un evenement...");
        searchField.setStyle("-fx-background-color: #1C2028; -fx-text-fill: #F0F2F5; -fx-prompt-text-fill: #5A6070; -fx-border-color: #252830; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 16; -fx-font-size: 13px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, o, n) -> filterAndLoad());
        searchRow.getChildren().add(searchField);

        // Mood filter pills
        HBox pills = new HBox(8);
        pills.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup tg = new ToggleGroup();
        for (String[] mood : MOOD_FILTERS) {
            ToggleButton pill = new ToggleButton(mood[1]);
            pill.setToggleGroup(tg);
            styleMoodPill(pill, false);
            if (mood[0].equals("all")) {
                pill.setSelected(true);
                styleMoodPill(pill, true);
            }
            String key = mood[0];
            pill.setOnAction(e -> {
                activeFilter = key;
                pills.getChildren().forEach(n -> styleMoodPill((ToggleButton) n, false));
                styleMoodPill(pill, true);
                filterAndLoad();
            });
            pills.getChildren().add(pill);
        }

        header.getChildren().addAll(titleBlock, searchRow, pills);

        //  EVENTS GRID 
        eventsList = new VBox(14);
        eventsList.setPadding(new Insets(8, 36, 36, 36));

        ScrollPane scroll = new ScrollPane(eventsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0A0C0F; -fx-background-color: #0A0C0F; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setTop(header);
        setCenter(scroll);
        BorderPane.setMargin(scroll, new Insets(12, 0, 0, 0));
    }


    private void styleMoodPill(ToggleButton pill, boolean active) {
        if (active) {
            pill.setStyle("-fx-background-color: #1BBFA822; -fx-border-color: #1BBFA8; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #1BBFA8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        } else {
            pill.setStyle("-fx-background-color: #1C2028; -fx-border-color: #252830; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #8A95A3; -fx-font-size: 12px; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        }
    }

    private String cardStyle(boolean hover) {
        if (hover) {
            return "-fx-background-color: #1C2028; -fx-background-radius: 12; -fx-border-color: #1BBFA8; -fx-border-radius: 12; -fx-border-width: 1; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(27,191,168,0.15), 16, 0, 0, 4);";
        }
        return "-fx-background-color: #1C2028; -fx-background-radius: 12; -fx-border-color: #252830; -fx-border-radius: 12; -fx-border-width: 1; -fx-cursor: hand;";
    }
    private void filterAndLoad() {
        List<Event> events;
        try {
            List<Event> all = "all".equals(activeFilter)
                    ? service.getUpcomingEvents()
                    : service.getEventsByMood(activeFilter);
            String search = searchField.getText().trim().toLowerCase();
            if (!search.isEmpty()) {
                events = all.stream()
                        .filter(e -> e.getTitle().toLowerCase().contains(search)
                                || (e.getDescription() != null && e.getDescription().toLowerCase().contains(search))
                                || (e.getLocation() != null && e.getLocation().toLowerCase().contains(search)))
                        .toList();
            } else {
                events = all;
            }
        } catch (Exception ex) {
            showError("Erreur de chargement : " + ex.getMessage());
            return;
        }
        render(events);
    }

    public void loadEvents() {
        filterAndLoad();
    }

    private void render(List<Event> events) {
        eventsList.getChildren().clear();
        if (events.isEmpty()) {
            eventsList.getChildren().add(buildEmptyState());
            return;
        }
        for (Event ev : events) {
            eventsList.getChildren().add(buildEventCard(ev));
        }
    }

    //  EVENT CARD 
    private HBox buildEventCard(Event ev) {
        HBox card = new HBox(0);
        card.setStyle(cardStyle(false));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Left accent bar (mood color)
        VBox colorBar = new VBox();
        colorBar.setPrefWidth(5);
        colorBar.setMinHeight(90);
        String mColor = EventsViewController.moodColor(ev.getRecommendedMood());
        colorBar.setStyle("-fx-background-color: " + mColor + "; -fx-background-radius: 12 0 0 12;");

        StackPane imagePane = buildEventImage(ev, 190, 120);

        // Main content
        VBox content = new VBox(8);
        content.setPadding(new Insets(18, 16, 18, 18));
        HBox.setHgrow(content, Priority.ALWAYS);

        // Title + badge
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(ev.getTitle());
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #F0F2F5;");
        Label moodBadge = new Label(EventsViewController.moodLabel(ev.getRecommendedMood()));
        moodBadge.setStyle(
                "-fx-background-color:" + mColor + "28;" +
                        "-fx-text-fill:" + mColor + ";" +
                        "-fx-border-color:" + mColor + "66;" +
                        "-fx-font-size:11px; -fx-padding:2 10 2 10;" +
                        "-fx-background-radius:20; -fx-border-radius:20; -fx-font-weight:bold;");
        Label priceBadge = new Label(priceBadgeText(ev));
        priceBadge.setStyle("-fx-background-color: #F59E0B22; -fx-border-color: #F59E0B66;"
                + "-fx-text-fill: #FBBF24; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-padding: 2 10 2 10; -fx-background-radius: 20; -fx-border-radius: 20;");
        titleRow.getChildren().addAll(titleLbl, moodBadge, priceBadge);

        // Description
        Label descLbl = new Label(ev.getDescription() != null && !ev.getDescription().isBlank()
                ? ev.getDescription() : "Aucune description");
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8A95A3;");
        descLbl.setMaxWidth(500);

        // Meta row: date, time, location, spots
        HBox meta = new HBox(20);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().addAll(
                metaChip(ev.getEventDate() != null ? ev.getEventDate().toString() : ""),
                metaChip( ev.getEventTime() != null ? ev.getEventTime() : ""),
                metaChip( ev.getLocation() != null ? ev.getLocation() : "")
        );

        content.getChildren().addAll(titleRow, descLbl, meta);

        // Right: spots + join button
        VBox rightCol = new VBox(12);
        rightCol.setAlignment(Pos.CENTER);
        rightCol.setPadding(new Insets(16, 20, 16, 12));
        rightCol.setMinWidth(120);

        EventService.EventAvailability availability = service.getEventAvailability(ev.getIdEvent());
        int spots = availability.availableSpots();
        boolean joined = ev.getParticipants().contains(currentUserId);
        boolean full = availability.full();

        // Spots indicator
        VBox spotsBox = new VBox(3);
        spotsBox.setAlignment(Pos.CENTER);
        Label spotsNum = new Label(spots > 0 ? String.valueOf(spots) : "0");
        spotsNum.setStyle("-fx-font-size:22px; -fx-font-weight:900; -fx-text-fill:" +
                (full ? "#E85D3A" : "#1BBFA8") + ";");
        Label spotsLbl2 = new Label(full ? "Complet" : "places libres");
        spotsLbl2.setStyle("-fx-font-size:10px; -fx-text-fill:#5A6070;");
        spotsBox.getChildren().addAll(spotsNum, spotsLbl2);

        // Action button
        Button actionBtn;
        if (joined) {
            actionBtn = new Button("Inscrit");
            actionBtn.setStyle(
                    "-fx-background-color: #1BBFA822; -fx-text-fill: #1BBFA8;" +
                            "-fx-border-color: #1BBFA8; -fx-border-radius: 8; -fx-background-radius: 8;" +
                            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 16;");
        } else if (full) {
            actionBtn = new Button("Complet");
            actionBtn.setStyle(
                    "-fx-background-color: #1C2028; -fx-text-fill: #5A6070;" +
                            "-fx-border-color: #252830; -fx-border-radius: 8; -fx-background-radius: 8;" +
                            "-fx-font-size: 12px; -fx-padding: 8 16;");
            actionBtn.setDisable(true);
        } else {
            actionBtn = new Button("Participer");
            actionBtn.setStyle(
                    "-fx-background-color: #1BBFA8; -fx-text-fill: white;" +
                            "-fx-background-radius: 8; -fx-font-size: 12px;" +
                            "-fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
            actionBtn.setOnAction(e -> {
                e.consume();
                openDetailView(ev);
            });
        }

        rightCol.getChildren().addAll(spotsBox, actionBtn);

        card.getChildren().addAll(colorBar, imagePane, content, rightCol);

        // Click on card  open detail
        card.setOnMouseClicked(e -> openDetailView(ev));
        card.setOnMouseEntered(e -> card.setStyle(cardStyle(true)));
        card.setOnMouseExited(e -> card.setStyle(cardStyle(false)));

        return card;
    }

    private StackPane buildEventImage(Event ev, double width, double height) {
        StackPane imagePane = new StackPane();
        imagePane.setMinSize(width, height);
        imagePane.setPrefSize(width, height);
        imagePane.setMaxSize(width, height);
        imagePane.setStyle("-fx-background-color: #12151A;");

        if (ev.getImageUrl() != null && !ev.getImageUrl().isBlank()) {
            ImageView imageView = new ImageView(new Image(ev.getImageUrl(), width, height, false, true, true));
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(false);
            imagePane.getChildren().add(imageView);
            return imagePane;
        }

        Label placeholder = new Label("No image");
        placeholder.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A6070;");
        imagePane.getChildren().add(placeholder);
        return imagePane;
    }

    private String priceBadgeText(Event ev) {
        if (!ev.isPaidEvent()) {
            return "Gratuit";
        }
        if (ev.getDiscountPercent() > 0) {
            return String.format(java.util.Locale.US, "%.2f DT (-%d%%)",
                    ev.getDiscountedPrice(), ev.getDiscountPercent());
        }
        return String.format(java.util.Locale.US, "%.2f DT", ev.getPrice());
    }

        private HBox metaChip(String text) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        Label textLbl = new Label(text);
        textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8A95A3;");
        chip.getChildren().add(textLbl);
        return chip;
    }

    private void openDetailView(Event ev) {
        // Reload fresh event from DB
        service.getEventById(ev.getIdEvent()).ifPresent(fresh -> {
            UserEventDetailView detail = new UserEventDetailView(fresh, service, currentUserId, this::loadEvents);
            // Swap center content with detail view
            getScene().getRoot().lookup(".content-area");
            StackPane contentArea = (StackPane) getScene().getRoot().lookup(".content-area");
            if (contentArea != null) {
                contentArea.getChildren().setAll(detail);
            }
        });
    }

    private VBox buildEmptyState() {
        VBox empty = new VBox(14);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80));
        Label msg = new Label("Aucun evenement disponible");
        msg.setStyle("-fx-font-size: 18px; -fx-text-fill: #F0F2F5; -fx-font-weight: bold;");
        Label hint = new Label("Revenez plus tard ou changez votre filtre d'humeur.");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A6070;");
        empty.getChildren().addAll(msg, hint);
        return empty;
    }

    private void showError(String msg) {
        eventsList.getChildren().clear();
        Label err = new Label("Erreur : " + msg);
        err.setStyle("-fx-text-fill: #E85D3A; -fx-font-size: 13px; -fx-padding: 20;");
        eventsList.getChildren().add(err);
    }
}
