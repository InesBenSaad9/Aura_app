package tn.esprit.aura.controllers;

import tn.esprit.aura.dao.TaskDAO;
import tn.esprit.aura.entities.Task;
import tn.esprit.aura.services.PredictiveTaskEngine;
import tn.esprit.aura.services.PredictiveTaskEngine.RapportPredictif;
import tn.esprit.aura.services.PredictiveTaskEngine.PredictionTache;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class TasksController {

    // ═══ FXML IDs ═══
    @FXML private VBox todoList, inProgressList, doneList;
    @FXML private Label todoCount, inProgressCount, doneCount;
    @FXML private StackPane popupOverlay;
    @FXML private StackPane viewContainer;
    @FXML private HBox kanbanView;
    @FXML private Button btnToutes, btnHaute, btnMoyenne, btnBasse;
    @FXML private Button btnKanban, btnTable, btnCalendar, btnStats;
    @FXML private Button todoMenuBtn, inProgressMenuBtn, doneMenuBtn;
    @FXML private Label todoTitle, inProgressTitle, doneTitle;

    // ═══ Predictive Intelligence FXML ═══
    @FXML private VBox        statsView;
    @FXML private Label       predResume;
    @FXML private Label       predScoreLabel;
    @FXML private ProgressBar predScoreBar;
    @FXML private Label       predCritiqueLabel;
    @FXML private Label       predRisqueLabel;
    @FXML private Label       predOkLabel;
    @FXML private Label       predHeureLabel;
    @FXML private Label       predRecommandationLabel;
    @FXML private VBox        predTaskList;

    // ═══ Services ═══
    private final TaskDAO taskDAO = new TaskDAO();
    private final PredictiveTaskEngine predictiveEngine = new PredictiveTaskEngine();
    private List<Task> allTasks;
    private Task draggedTask = null;
    private String currentPriorityFilter = null;
    private int currentUserId = tn.esprit.aura.utils.SessionManager.isLoggedIn() ? tn.esprit.aura.utils.SessionManager.getCurrentUser().getId() : 1;

    @FXML
    public void initialize() {
        loadTasks();
        setupDropZone(todoList, "à faire");
        setupDropZone(inProgressList, "en cours");
        setupDropZone(doneList, "terminé");
        setActiveView(btnKanban);
    }

    // ═══════════════════════════════
    // CHARGEMENT TÂCHES
    // ═══════════════════════════════

    private void loadTasks() {
        allTasks = taskDAO.getAll(currentUserId);
        List<Task> filtered = currentPriorityFilter != null
                ? allTasks.stream().filter(t -> t.getPriority().equalsIgnoreCase(currentPriorityFilter)).toList()
                : allTasks;
        displayByStatus(filtered);
    }

    private void displayByStatus(List<Task> tasks) {
        todoList.getChildren().clear();
        inProgressList.getChildren().clear();
        doneList.getChildren().clear();
        int todo = 0, inProgress = 0, done = 0;

        for (Task t : tasks) {
            VBox card = createTaskCard(t);
            String status = t.getStatus() != null ? t.getStatus().toLowerCase().trim() : "";
            switch (status) {
                case "à faire", "a faire" -> { todoList.getChildren().add(card); todo++; }
                case "en cours"           -> { inProgressList.getChildren().add(card); inProgress++; }
                case "terminé", "termine" -> { doneList.getChildren().add(card); done++; }
                default                   -> { todoList.getChildren().add(card); todo++; }
            }
        }
        if (todoCount != null)      todoCount.setText(String.valueOf(todo));
        if (inProgressCount != null) inProgressCount.setText(String.valueOf(inProgress));
        if (doneCount != null)      doneCount.setText(String.valueOf(done));
    }

    // ═══════════════════════════════
    // CARTE TÂCHE
    // ═══════════════════════════════

    private VBox createTaskCard(Task t) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1E2329; -fx-background-radius: 12; -fx-padding: 14;");

        Rectangle bar = new Rectangle(40, 4);
        bar.setFill(Color.web(getPriorityColor(t.getPriority())));
        bar.setArcWidth(4); bar.setArcHeight(4);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(t.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #1BBFA8; -fx-text-fill: black; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> { e.consume(); showTaskFormPopup(t); });

        Button deleteBtn = new Button("Del");
        deleteBtn.setStyle("-fx-background-color: #E85D3A; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> { e.consume(); showDeletePopup(t); });

        header.getChildren().addAll(title, editBtn, deleteBtn);

        HBox badges = new HBox(6);
        Label priorityBadge = new Label(t.getPriority());
        priorityBadge.setStyle("-fx-background-color: " + getPriorityColor(t.getPriority()) + "33; -fx-text-fill: " + getPriorityColor(t.getPriority()) + "; -fx-background-radius: 6; -fx-padding: 2 8; -fx-font-size: 11; -fx-font-weight: bold;");
        String dateText = t.getScheduledAt() != null ? t.getScheduledAt().substring(0, Math.min(10, t.getScheduledAt().length())) : "";
        Label dateBadge = new Label("📅 " + dateText);
        dateBadge.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 11;");
        badges.getChildren().addAll(priorityBadge, dateBadge);

        card.getChildren().addAll(bar, header, badges);

        // Drag & Drop
        card.setOnDragDetected(event -> {
            draggedTask = t;
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(String.valueOf(t.getId()));
            db.setContent(cc);
            card.setOpacity(0.4);
            event.consume();
        });
        card.setOnDragDone(event -> {
            card.setOpacity(1.0);
            draggedTask = null;
            event.consume();
        });
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252B35; -fx-background-radius: 12; -fx-padding: 14;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2329; -fx-background-radius: 12; -fx-padding: 14;"));

        return card;
    }

    // ═══════════════════════════════
    // DRAG & DROP
    // ═══════════════════════════════

    private void setupDropZone(VBox column, String newStatus) {
        column.setOnDragOver(event -> {
            if (event.getDragboard().hasString() && draggedTask != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                column.setStyle("-fx-background-color: rgba(27,191,168,0.08); -fx-background-radius: 8; -fx-padding: 4; -fx-border-color: #1BBFA8; -fx-border-radius: 8; -fx-border-width: 2;");
            }
            event.consume();
        });
        column.setOnDragExited(event -> {
            column.setStyle("-fx-padding: 4;");
            event.consume();
        });
        column.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasString() && draggedTask != null) {
                taskDAO.update(draggedTask.getId(), newStatus);
                success = true;
                loadTasks();
            }
            column.setStyle("-fx-padding: 4;");
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ═══════════════════════════════
    // VUES
    // ═══════════════════════════════

    @FXML
    private void showKanban() {
        setActiveView(btnKanban);
        // Remet le kanban visible si statsView l'avait caché
        if (statsView != null) { statsView.setVisible(false); statsView.setManaged(false); }
        viewContainer.getChildren().setAll(kanbanView);
        kanbanView.setVisible(true);
        kanbanView.setManaged(true);
        loadTasks();
    }

    @FXML
    private void showTable() {
        setActiveView(btnTable);
        if (statsView != null) { statsView.setVisible(false); statsView.setManaged(false); }

        VBox tableView = new VBox(0);
        tableView.setStyle("-fx-background-color: #111418; -fx-background-radius: 12;");

        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1E2329; -fx-padding: 12 16; -fx-background-radius: 12 12 0 0;");
        for (String[] col : new String[][]{{"Titre","300"},{"Priorité","140"},{"Statut","140"},{"Date","140"},{"Actions","160"}}) {
            Label lbl = new Label(col[0]);
            lbl.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12; -fx-font-weight: bold;");
            lbl.setPrefWidth(Double.parseDouble(col[1]));
            header.getChildren().add(lbl);
        }
        tableView.getChildren().add(header);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        VBox rows = new VBox(0);

        List<Task> tasks = taskDAO.getAll(currentUserId);
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            HBox row = new HBox();
            row.setStyle("-fx-background-color: " + (i % 2 == 0 ? "#111418" : "#13171C") + "; -fx-padding: 12 16;");
            row.setAlignment(Pos.CENTER_LEFT);

            Label tLbl = new Label(t.getTitle()); tLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13;"); tLbl.setPrefWidth(300);
            Label pLbl = new Label(t.getPriority()); pLbl.setStyle("-fx-background-color: " + getPriorityColor(t.getPriority()) + "33; -fx-text-fill: " + getPriorityColor(t.getPriority()) + "; -fx-background-radius: 6; -fx-padding: 2 8; -fx-font-size: 11;"); pLbl.setPrefWidth(140);
            Label sLbl = new Label(t.getStatus()); sLbl.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12;"); sLbl.setPrefWidth(140);
            Label dLbl = new Label(t.getScheduledAt() != null ? t.getScheduledAt().substring(0, Math.min(10, t.getScheduledAt().length())) : "—"); dLbl.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12;"); dLbl.setPrefWidth(140);

            HBox actions = new HBox(6);
            actions.setPrefWidth(160);
            Button e1 = new Button("Modifier");
            e1.setStyle("-fx-background-color: #1E2329; -fx-text-fill: #1BBFA8; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
            e1.setOnAction(e -> showTaskFormPopup(t));
            Button d1 = new Button("Supprimer");
            d1.setStyle("-fx-background-color: #1E2329; -fx-text-fill: #E85D3A; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11; -fx-font-weight: bold;");
            d1.setOnAction(e -> showDeletePopup(t));
            actions.getChildren().addAll(e1, d1);

            row.getChildren().addAll(tLbl, pLbl, sLbl, dLbl, actions);
            rows.getChildren().add(row);
        }
        scroll.setContent(rows);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        tableView.getChildren().add(scroll);
        viewContainer.getChildren().setAll(tableView);
    }

    @FXML
    private void showCalendar() {
        setActiveView(btnCalendar);
        if (statsView != null) { statsView.setVisible(false); statsView.setManaged(false); }

        VBox calView = new VBox(16);
        calView.setStyle("-fx-background-color: #111418; -fx-background-radius: 12; -fx-padding: 20;");

        java.time.LocalDate today = java.time.LocalDate.now();
        Label monthLbl = new Label(today.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH) + " " + today.getYear());
        monthLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(4); grid.setVgap(4);

        String[] dayNames = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
        for (int i = 0; i < 7; i++) {
            Label d = new Label(dayNames[i]);
            d.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12;");
            d.setPrefWidth(100);
            grid.add(d, i, 0);
        }

        int startCol = today.withDayOfMonth(1).getDayOfWeek().getValue() - 1;
        int daysInMonth = today.lengthOfMonth();
        List<Task> tasks = taskDAO.getAll(currentUserId);

        for (int day = 1; day <= daysInMonth; day++) {
            int col = (startCol + day - 1) % 7;
            int rowIdx = (startCol + day - 1) / 7 + 1;
            VBox cell = new VBox(2);
            cell.setPrefSize(100, 80);
            boolean isToday = day == today.getDayOfMonth();
            cell.setStyle(isToday
                    ? "-fx-background-color: rgba(27,191,168,0.15); -fx-background-radius: 8; -fx-border-color: #1BBFA8; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 4;"
                    : "-fx-background-color: #1E2329; -fx-background-radius: 8; -fx-padding: 4;");
            Label dayLbl = new Label(String.valueOf(day));
            dayLbl.setStyle(isToday ? "-fx-text-fill: #1BBFA8; -fx-font-size: 12; -fx-font-weight: bold;" : "-fx-text-fill: #A8B4C0; -fx-font-size: 12;");
            cell.getChildren().add(dayLbl);
            String dateStr = String.format("%d-%02d-%02d", today.getYear(), today.getMonthValue(), day);
            for (Task t : tasks) {
                if (t.getScheduledAt() != null && t.getScheduledAt().startsWith(dateStr)) {
                    Label dot = new Label("● " + t.getTitle());
                    dot.setStyle("-fx-text-fill: " + getPriorityColor(t.getPriority()) + "; -fx-font-size: 9;");
                    dot.setMaxWidth(92);
                    cell.getChildren().add(dot);
                }
            }
            grid.add(cell, col, rowIdx);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        calView.getChildren().addAll(monthLbl, scroll);
        viewContainer.getChildren().setAll(calView);
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTIER AVANCÉ #2 — Predictive Task Intelligence
    // ═══════════════════════════════════════════════════════════

    @FXML
    private void showStats() {
        setActiveView(btnStats);

        // Affiche la statsView du FXML dans le viewContainer
        if (statsView != null) {
            viewContainer.getChildren().setAll(statsView);
            statsView.setVisible(true);
            statsView.setManaged(true);
        }

        // Lance l'analyse automatiquement
        onAnalyserTaches();
    }

    @FXML
    private void onAnalyserTaches() {
        if (predResume != null) predResume.setText("Analyse en cours...");
        if (predTaskList != null) predTaskList.getChildren().clear();

        new Thread(() -> {
            RapportPredictif rapport = predictiveEngine.analyser(currentUserId);

            javafx.application.Platform.runLater(() -> {
                // KPIs
                predScoreLabel.setText(rapport.scoreChargeTravail + "/100");
                predScoreBar.setProgress(rapport.scoreChargeTravail / 100.0);

                String couleurScore = rapport.scoreChargeTravail >= 75 ? "#E85D3A"
                        : rapport.scoreChargeTravail >= 50 ? "#EF9F27" : "#1BBFA8";
                predScoreLabel.setStyle("-fx-text-fill: " + couleurScore + "; -fx-font-size: 32; -fx-font-weight: bold;");

                predCritiqueLabel.setText(String.valueOf(rapport.tachesEnRetard));
                predRisqueLabel.setText(String.valueOf(rapport.tachesARisque));
                predOkLabel.setText(String.valueOf(rapport.tachesOK));
                predHeureLabel.setText(rapport.heureOptimale);
                predResume.setText(rapport.resumeGlobal);
                predRecommandationLabel.setText(rapport.recommandationGlobale);

                predTaskList.getChildren().clear();
                for (PredictionTache pred : rapport.predictions) {
                    predTaskList.getChildren().add(creerCartePrediction(pred));
                }
            });
        }).start();
    }

    private javafx.scene.Node creerCartePrediction(PredictionTache pred) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1E2329; -fx-background-radius: 12; -fx-padding: 14;");

        Rectangle barre = new Rectangle(4, 40);
        barre.setFill(Color.web(pred.couleurRisque));
        barre.setArcWidth(4); barre.setArcHeight(4);

        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label(pred.tache.getTitle());
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;");

        Label badge = new Label(pred.niveauRisque);
        badge.setStyle("-fx-background-color: " + pred.couleurRisque + "33; -fx-text-fill: " + pred.couleurRisque
                + "; -fx-background-radius: 8; -fx-padding: 2 8; -fx-font-size: 11; -fx-font-weight: bold;");

        Label priorite = new Label(pred.tache.getPriority().toUpperCase());
        priorite.setStyle("-fx-background-color: #111418; -fx-text-fill: #A8B4C0; -fx-background-radius: 8; -fx-padding: 2 8; -fx-font-size: 11;");

        headerRow.getChildren().addAll(titre, badge, priorite);

        Label conseil = new Label(pred.conseil);
        conseil.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12;");
        conseil.setWrapText(true);

        infos.getChildren().addAll(headerRow, conseil);

        VBox pct = new VBox(2);
        pct.setAlignment(Pos.CENTER);
        Label pctLabel = new Label(pred.risqueRetard + "%");
        pctLabel.setStyle("-fx-text-fill: " + pred.couleurRisque + "; -fx-font-size: 18; -fx-font-weight: bold;");
        Label pctSub = new Label("risque");
        pctSub.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 10;");
        pct.getChildren().addAll(pctLabel, pctSub);

        row.getChildren().addAll(barre, infos, pct);
        return row;
    }

    // ═══════════════════════════════
    // MENUS COLONNES
    // ═══════════════════════════════

    @FXML private void onTodoMenu()       { showColumnMenu(todoMenuBtn, todoTitle.getText(), "à faire", todoTitle); }
    @FXML private void onInProgressMenu() { showColumnMenu(inProgressMenuBtn, inProgressTitle.getText(), "en cours", inProgressTitle); }
    @FXML private void onDoneMenu()       { showColumnMenu(doneMenuBtn, doneTitle.getText(), "terminé", doneTitle); }

    private void showColumnMenu(Button sourceBtn, String columnName, String status, Label columnLabel) {
        ContextMenu menu = new ContextMenu();
        MenuItem addCol    = new MenuItem("Ajouter une colonne");
        addCol.setOnAction(e -> showAddColumnPopup());
        MenuItem renameCol = new MenuItem("Renommer \"" + columnName + "\"");
        renameCol.setOnAction(e -> showRenamePopup(columnName, status, columnLabel));
        SeparatorMenuItem sep = new SeparatorMenuItem();
        MenuItem deleteAll = new MenuItem("Supprimer toutes les cartes");
        deleteAll.setOnAction(e -> showDeleteAllPopup(status, columnName));
        MenuItem deleteCol = new MenuItem("Supprimer la colonne");
        deleteCol.setOnAction(e -> showDeleteColumnPopup(columnName, status));
        menu.getItems().addAll(addCol, renameCol, sep, deleteAll, deleteCol);
        menu.show(sourceBtn, javafx.geometry.Side.BOTTOM, 0, 4);
    }

    private void showAddColumnPopup() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");
        VBox modal = new VBox(14);
        modal.setMaxWidth(400);
        modal.setStyle("-fx-background-color: #1A1E24; -fx-background-radius: 16; -fx-padding: 28;");
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("Ajouter une colonne");
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 17; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A8B4C0; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> popupOverlay.getChildren().clear());
        titleRow.getChildren().addAll(titleLbl, sp, closeBtn);
        Label h = hint("Nom de la colonne *");
        TextField nameField = new TextField();
        nameField.setPromptText("Ex: En révision");
        styleField(nameField, false);
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #E85D3A; -fx-font-size: 12;");
        HBox btnRow = new HBox(10);
        Button saveBtn = new Button("Ajouter");
        saveBtn.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(saveBtn, Priority.ALWAYS);
        saveBtn.setStyle("-fx-background-color: #1BBFA8; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(cancelBtn, Priority.ALWAYS);
        cancelBtn.setStyle("-fx-background-color: #2A2F38; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> popupOverlay.getChildren().clear());
        saveBtn.setOnAction(e -> {
            if (nameField.getText().trim().isEmpty()) { styleField(nameField, true); errorLabel.setText("⚠️ Le nom est obligatoire !"); return; }
            addNewColumn(nameField.getText().trim());
            popupOverlay.getChildren().clear();
        });
        btnRow.getChildren().addAll(saveBtn, cancelBtn);
        modal.getChildren().addAll(titleRow, h, nameField, errorLabel, btnRow);
        overlay.getChildren().add(modal);
        popupOverlay.getChildren().setAll(overlay);
    }

    private void addNewColumn(String name) {
        VBox newCol = new VBox(12);
        newCol.setStyle("-fx-background-color: #111418; -fx-background-radius: 16; -fx-padding: 20; -fx-border-color: rgba(168,180,192,0.2); -fx-border-radius: 16; -fx-border-width: 1;");
        HBox.setHgrow(newCol, Priority.ALWAYS);
        HBox colHeader = new HBox(10);
        colHeader.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(6);
        dot.setFill(Color.web("#A8B4C0"));
        Label colName = new Label(name);
        colName.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label count = new Label("0");
        count.setStyle("-fx-background-color: #1E2329; -fx-text-fill: #A8B4C0; -fx-background-radius: 10; -fx-padding: 2 8;");
        Button menuBtn = new Button("⋯");
        menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A8B4C0; -fx-font-size: 18; -fx-cursor: hand; -fx-padding: 0 6;");
        menuBtn.setOnAction(e -> showColumnMenu(menuBtn, name, name.toLowerCase(), colName));
        colHeader.getChildren().addAll(dot, colName, sp, count, menuBtn);
        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        VBox cardList = new VBox(10);
        cardList.setStyle("-fx-padding: 4;");
        scroll.setContent(cardList);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        setupDropZone(cardList, name.toLowerCase());
        Button addBtn = new Button("+ Ajouter une carte");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> onNewTask());
        addBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A8B4C0; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10; -fx-border-width: 1;");
        newCol.getChildren().addAll(colHeader, scroll, addBtn);
        kanbanView.getChildren().add(newCol);
    }

    private void showDeleteColumnPopup(String columnName, String status) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");
        VBox modal = new VBox(16); modal.setMaxWidth(400); modal.setAlignment(Pos.CENTER);
        modal.setStyle("-fx-background-color: #1A1E24; -fx-background-radius: 16; -fx-padding: 36;");
        Label icon = new Label("⚠️"); icon.setStyle("-fx-font-size: 44;");
        Label title = new Label("Supprimer la colonne ?"); title.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        Label subtitle = new Label("\"" + columnName + "\" et toutes ses cartes seront supprimées."); subtitle.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 13;"); subtitle.setWrapText(true);
        Label warning = new Label("Cette action est irréversible."); warning.setStyle("-fx-text-fill: #E85D3A; -fx-font-size: 12;");
        HBox btnRow = new HBox(12); btnRow.setAlignment(Pos.CENTER);
        Button del = new Button("Supprimer"); del.setPrefWidth(150);
        del.setStyle("-fx-background-color: #E85D3A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        del.setOnAction(e -> { allTasks.stream().filter(t -> t.getStatus().toLowerCase().contains(status)).forEach(t -> taskDAO.delete(t.getId())); popupOverlay.getChildren().clear(); loadTasks(); });
        Button can = new Button("Annuler"); can.setPrefWidth(150);
        can.setStyle("-fx-background-color: #2A2F38; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        can.setOnAction(e -> popupOverlay.getChildren().clear());
        btnRow.getChildren().addAll(del, can);
        modal.getChildren().addAll(icon, title, subtitle, warning, btnRow);
        overlay.getChildren().add(modal);
        popupOverlay.getChildren().setAll(overlay);
    }

    private void showRenamePopup(String currentName, String status, Label columnLabel) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");
        VBox modal = new VBox(14); modal.setMaxWidth(380);
        modal.setStyle("-fx-background-color: #1A1E24; -fx-background-radius: 16; -fx-padding: 28;");
        HBox titleRow = new HBox(); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("Renommer la colonne"); titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 17; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button closeBtn = new Button("X"); closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A8B4C0; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> popupOverlay.getChildren().clear());
        titleRow.getChildren().addAll(titleLbl, sp, closeBtn);
        TextField nameField = new TextField(currentName);
        styleField(nameField, false);
        Label errorLabel = new Label(""); errorLabel.setStyle("-fx-text-fill: #E85D3A; -fx-font-size: 12;");
        HBox btnRow = new HBox(10);
        Button saveBtn = new Button("Renommer"); saveBtn.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(saveBtn, Priority.ALWAYS);
        saveBtn.setStyle("-fx-background-color: #1BBFA8; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        Button cancelBtn = new Button("Annuler"); cancelBtn.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(cancelBtn, Priority.ALWAYS);
        cancelBtn.setStyle("-fx-background-color: #2A2F38; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> popupOverlay.getChildren().clear());
        saveBtn.setOnAction(e -> {
            if (nameField.getText().trim().isEmpty()) { styleField(nameField, true); errorLabel.setText("Le nom est obligatoire !"); return; }
            columnLabel.setText(nameField.getText().trim());
            popupOverlay.getChildren().clear();
        });
        btnRow.getChildren().addAll(saveBtn, cancelBtn);
        modal.getChildren().addAll(titleRow, hint("Nouveau nom"), nameField, errorLabel, btnRow);
        overlay.getChildren().add(modal);
        popupOverlay.getChildren().setAll(overlay);
    }

    private void showDeleteAllPopup(String status, String columnName) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");
        VBox modal = new VBox(16); modal.setMaxWidth(400); modal.setAlignment(Pos.CENTER);
        modal.setStyle("-fx-background-color: #1A1E24; -fx-background-radius: 16; -fx-padding: 36;");
        Label icon = new Label("⚠️"); icon.setStyle("-fx-font-size: 44;");
        Label title = new Label("Supprimer toutes les cartes ?"); title.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;"); title.setWrapText(true);
        Label subtitle = new Label("Toutes les tâches de \"" + columnName + "\" seront supprimées."); subtitle.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 13;"); subtitle.setWrapText(true);
        Label warning = new Label("Cette action est irréversible."); warning.setStyle("-fx-text-fill: #E85D3A; -fx-font-size: 12;");
        HBox btnRow = new HBox(12); btnRow.setAlignment(Pos.CENTER);
        Button deleteBtn = new Button("Tout supprimer"); deleteBtn.setPrefWidth(160);
        deleteBtn.setStyle("-fx-background-color: #E85D3A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> { allTasks.stream().filter(t -> t.getStatus().toLowerCase().contains(status.replace("à", "a"))).forEach(t -> taskDAO.delete(t.getId())); popupOverlay.getChildren().clear(); loadTasks(); });
        Button cancelBtn = new Button("Annuler"); cancelBtn.setPrefWidth(160);
        cancelBtn.setStyle("-fx-background-color: #2A2F38; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> popupOverlay.getChildren().clear());
        btnRow.getChildren().addAll(deleteBtn, cancelBtn);
        modal.getChildren().addAll(icon, title, subtitle, warning, btnRow);
        overlay.getChildren().add(modal);
        popupOverlay.getChildren().setAll(overlay);
    }

    // ═══════════════════════════════
    // POPUP AJOUTER / MODIFIER
    // ═══════════════════════════════

    @FXML private void onNewTask() { showTaskFormPopup(null); }

    private void showTaskFormPopup(Task taskToEdit) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");
        VBox modal = new VBox(14); modal.setMaxWidth(460);
        modal.setStyle("-fx-background-color: #1A1E24; -fx-background-radius: 16; -fx-padding: 30;");
        HBox titleRow = new HBox(); titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(taskToEdit == null ? "Nouvelle tâche" : "Modifier la tâche");
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 19; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button closeBtn = new Button("✕"); closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A8B4C0; -fx-font-size: 16; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> popupOverlay.getChildren().clear());
        titleRow.getChildren().addAll(titleLbl, sp, closeBtn);
        TextField titleField = new TextField(taskToEdit != null ? taskToEdit.getTitle() : "");
        titleField.setPromptText("Ex: Réviser le cours Java"); styleField(titleField, false);
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("haute", "moyenne", "basse");
        priorityBox.setPromptText("-- Choisir une priorité --");
        priorityBox.setValue(taskToEdit != null ? taskToEdit.getPriority() : null);
        priorityBox.setMaxWidth(Double.MAX_VALUE); styleCombo(priorityBox, false);
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("à faire", "en cours", "terminé");
        statusBox.setPromptText("-- Choisir un statut --");
        statusBox.setValue(taskToEdit != null ? taskToEdit.getStatus() : null);
        statusBox.setMaxWidth(Double.MAX_VALUE); styleCombo(statusBox, false);
        TextField dateField = new TextField(taskToEdit != null ? taskToEdit.getScheduledAt() : "");
        dateField.setPromptText("2026-06-01"); styleField(dateField, false);
        Label errorLabel = new Label(""); errorLabel.setWrapText(true); errorLabel.setMinHeight(18);
        errorLabel.setStyle("-fx-text-fill: #E85D3A; -fx-font-size: 12;");
        HBox btnRow = new HBox(12);
        Button saveBtn = new Button(taskToEdit == null ? "Ajouter" : "Sauvegarder");
        saveBtn.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(saveBtn, Priority.ALWAYS);
        saveBtn.setStyle("-fx-background-color: #1BBFA8; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 13; -fx-font-size: 14; -fx-cursor: hand;");
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(cancelBtn, Priority.ALWAYS);
        cancelBtn.setStyle("-fx-background-color: #2A2F38; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 13; -fx-font-size: 14; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> popupOverlay.getChildren().clear());
        saveBtn.setOnAction(e -> {
            styleField(titleField, false); styleField(dateField, false);
            styleCombo(priorityBox, false); styleCombo(statusBox, false); errorLabel.setText("");
            if (titleField.getText().trim().isEmpty()) { styleField(titleField, true); errorLabel.setText("⚠️ Le titre est obligatoire !"); return; }
            if (titleField.getText().trim().length() < 3) { styleField(titleField, true); errorLabel.setText("⚠️ Le titre doit contenir au moins 3 caractères !"); return; }
            if (priorityBox.getValue() == null) { styleCombo(priorityBox, true); errorLabel.setText("⚠️ Veuillez choisir une priorité !"); return; }
            if (statusBox.getValue() == null) { styleCombo(statusBox, true); errorLabel.setText("⚠️ Veuillez choisir un statut !"); return; }
            String date = dateField.getText().trim();
            if (!date.isEmpty() && !date.matches("\\d{4}-\\d{2}-\\d{2}")) { styleField(dateField, true); errorLabel.setText("⚠️ Format de date invalide ! Exemple : 2026-06-01"); return; }
            if (date.isEmpty()) date = "2026-12-31";
            if (taskToEdit != null) taskDAO.delete(taskToEdit.getId());
            taskDAO.add(new Task(0, currentUserId, titleField.getText().trim(), priorityBox.getValue(), statusBox.getValue(), date));
            popupOverlay.getChildren().clear();
            loadTasks();
        });
        btnRow.getChildren().addAll(saveBtn, cancelBtn);
        modal.getChildren().addAll(titleRow, hint("Titre *"), titleField, hint("Priorité *"), priorityBox, hint("Statut *"), statusBox, hint("Date (optionnel — format: 2026-06-01)"), dateField, errorLabel, btnRow);
        overlay.getChildren().add(modal);
        popupOverlay.getChildren().setAll(overlay);
    }

    // ═══════════════════════════════
    // POPUP SUPPRESSION
    // ═══════════════════════════════

    private void showDeletePopup(Task t) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");
        VBox modal = new VBox(16); modal.setMaxWidth(400); modal.setAlignment(Pos.CENTER);
        modal.setStyle("-fx-background-color: #1A1E24; -fx-background-radius: 16; -fx-padding: 36;");
        Label icon = new Label("⚠️"); icon.setStyle("-fx-font-size: 44;");
        Label title = new Label("Êtes-vous sûr ?"); title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
        Label taskName = new Label("\"" + t.getTitle() + "\""); taskName.setStyle("-fx-text-fill: #1BBFA8; -fx-font-size: 14; -fx-font-style: italic;"); taskName.setWrapText(true);
        Label warning = new Label("Cette action est irréversible."); warning.setStyle("-fx-text-fill: #E85D3A; -fx-font-size: 12;");
        HBox btnRow = new HBox(12); btnRow.setAlignment(Pos.CENTER);
        Button del = new Button("Supprimer"); del.setPrefWidth(150);
        del.setStyle("-fx-background-color: #E85D3A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        del.setOnAction(e -> { taskDAO.delete(t.getId()); popupOverlay.getChildren().clear(); loadTasks(); });
        Button can = new Button("Annuler"); can.setPrefWidth(150);
        can.setStyle("-fx-background-color: #2A2F38; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        can.setOnAction(e -> popupOverlay.getChildren().clear());
        btnRow.getChildren().addAll(del, can);
        modal.getChildren().addAll(icon, title, taskName, warning, btnRow);
        overlay.getChildren().add(modal);
        popupOverlay.getChildren().setAll(overlay);
    }

    // ═══════════════════════════════
    // FILTRES
    // ═══════════════════════════════

    @FXML private void filterToutes()  { currentPriorityFilter = null;      loadTasks(); setActiveFilter(btnToutes,  "#6B5FD4"); }
    @FXML private void filterHaute()   { currentPriorityFilter = "haute";   loadTasks(); setActiveFilter(btnHaute,   "#E85D3A"); }
    @FXML private void filterMoyenne() { currentPriorityFilter = "moyenne"; loadTasks(); setActiveFilter(btnMoyenne, "#EF9F27"); }
    @FXML private void filterBasse()   { currentPriorityFilter = "basse";   loadTasks(); setActiveFilter(btnBasse,   "#1BBFA8"); }

    private void setActiveFilter(Button active, String color) {
        for (Button b : new Button[]{btnToutes, btnHaute, btnMoyenne, btnBasse}) {
            if (b == null) continue;
            b.setStyle("-fx-background-color: #1E2329; -fx-text-fill: #A8B4C0; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand;");
        }
        if (active != null)
            active.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand; -fx-font-weight: bold;");
    }

    private void setActiveView(Button active) {
        for (Button b : new Button[]{btnKanban, btnTable, btnCalendar, btnStats}) {
            if (b == null) continue;
            b.setStyle("-fx-background-color: #1E2329; -fx-text-fill: #A8B4C0; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand;");
        }
        if (active != null)
            active.setStyle("-fx-background-color: #1BBFA8; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 16; -fx-cursor: hand;");
    }

    // ═══════════════════════════════
    // HELPERS
    // ═══════════════════════════════

    private Label hint(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #A8B4C0; -fx-font-size: 12;");
        return l;
    }

    private void styleField(TextField field, boolean isError) {
        field.setStyle(isError
                ? "-fx-background-color: #111418; -fx-text-fill: white; -fx-prompt-text-fill: #A8B4C0; -fx-background-radius: 10; -fx-padding: 12; -fx-font-size: 13; -fx-border-color: #E85D3A; -fx-border-width: 2; -fx-border-radius: 10;"
                : "-fx-background-color: #111418; -fx-text-fill: white; -fx-prompt-text-fill: #A8B4C0; -fx-background-radius: 10; -fx-padding: 12; -fx-font-size: 13; -fx-border-width: 0;");
    }

    private void styleCombo(ComboBox<?> box, boolean isError) {
        box.setStyle(isError
                ? "-fx-background-color: #111418; -fx-background-radius: 10; -fx-border-color: #E85D3A; -fx-border-width: 2; -fx-border-radius: 10;"
                : "-fx-background-color: #111418; -fx-background-radius: 10; -fx-border-width: 0;");
    }

    private String getPriorityColor(String priority) {
        if (priority == null) return "#A8B4C0";
        return switch (priority.toLowerCase()) {
            case "haute"   -> "#E85D3A";
            case "moyenne" -> "#EF9F27";
            default        -> "#1BBFA8";
        };
    }
}