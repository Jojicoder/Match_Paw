package com.matchpaw.view;

import com.matchpaw.ApiClient;
import com.matchpaw.model.*;
import com.matchpaw.service.AnimalService;
import com.matchpaw.service.ShelterService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainView {

    private static final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private final Runnable onLogout;
    private final ObservableList<Animal> animals = FXCollections.observableArrayList();
    private final ObservableList<Applicant> applicants = FXCollections.observableArrayList();
    private final ObservableList<AdoptionApplication> applications = FXCollections.observableArrayList();
    private final ObservableList<MedicalRecord> medicalRecords = FXCollections.observableArrayList();
    private final ObservableList<CareLog> careLogs = FXCollections.observableArrayList();
    private final ObservableList<StaffUser> staffUsers = FXCollections.observableArrayList();

    private Label statusLabel;
    private VBox dashboardContent;
    private VBox reportsContent;
    private TableView<Animal> animalTable;
    private TableView<AdoptionApplication> applicationTable;
    private TableView<MedicalRecord> medicalTable;
    private TableView<CareLog> careTable;

    private MainView(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public static Scene create(Runnable onLogout) {
        return new MainView(onLogout).createScene();
    }

    private Scene createScene() {
        var titleLabel = new Label("MatchPaw Shelter");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: white;");

        var roleLabel = new Label("Role: " + displayRole());
        roleLabel.setStyle("-fx-text-fill: #dfe6e9;");

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadAllData());

        var logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            ApiClient.setSession(null, 0, null);
            onLogout.run();
        });

        var topBar = new HBox(12, titleLabel, roleLabel, spacer, refreshButton, logoutButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setStyle("-fx-background-color: #263238;");

        var tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        if (canViewDashboard()) tabs.getTabs().add(new Tab("Dashboard", createDashboardTab()));
        if (canViewAnimals()) tabs.getTabs().add(new Tab("Animals", createAnimalTab()));
        if (canManageAnimals()) tabs.getTabs().add(new Tab("Archived Animals", createArchivedAnimalTab()));
        if (canViewApplications()) tabs.getTabs().add(new Tab("Applications", createApplicationTab()));
        if (canViewMedicalRecords()) tabs.getTabs().add(new Tab("Medical", createMedicalTab()));
        if (canViewCareLogs()) tabs.getTabs().add(new Tab("Care Logs", createCareTab()));
        if (canViewReports()) tabs.getTabs().add(new Tab("Reports", createReportsTab()));
        if (canManageUsers()) {
            tabs.getTabs().add(new Tab("Staff", createStaffTab()));
            tabs.getTabs().add(new Tab("Archived", createArchivedStaffTab()));
        }

        statusLabel = new Label("Loading data...");
        var statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");

        var root = new BorderPane(tabs, topBar, null, statusBar, null);
        loadAllData();
        return new Scene(root, 1180, 760);
    }

    private Node createDashboardTab() {
        dashboardContent = new VBox(18);
        dashboardContent.setPadding(new Insets(18));
        var scroll = new ScrollPane(dashboardContent);
        scroll.setFitToWidth(true);
        refreshDashboard();
        return scroll;
    }

    private Node createAnimalTab() {
        animalTable = new TableView<>();
        animalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        animalTable.setPlaceholder(new Label("No animals found."));

        var filtered = new FilteredList<>(animals, a -> !isArchivedAnimal(a));
        animalTable.setItems(filtered);

        var searchField = new TextField();
        searchField.setPromptText("Search name, species, breed");
        var speciesFilter = combo("All", "Dog", "Cat", "Rabbit", "Bird", "Other");
        var statusFilter = combo("All", "Available", "Pending", "Adopted");

        Runnable updateFilter = () -> filtered.setPredicate(animal -> {
            if (isArchivedAnimal(animal)) return false;
            var q = searchField.getText().trim().toLowerCase(Locale.ROOT);
            boolean queryMatches = q.isEmpty()
                    || valueOr(animal.getName(), "").toLowerCase(Locale.ROOT).contains(q)
                    || valueOr(animal.getSpecies(), "").toLowerCase(Locale.ROOT).contains(q)
                    || valueOr(animal.getBreed(), "").toLowerCase(Locale.ROOT).contains(q);
            boolean speciesMatches = "All".equals(speciesFilter.getValue()) || speciesFilter.getValue().equalsIgnoreCase(valueOr(animal.getSpecies(), ""));
            boolean statusMatches = "All".equals(statusFilter.getValue())
                    || animalStatusValue(statusFilter.getValue()).equalsIgnoreCase(valueOr(animal.getAdoptionStatus(), ""));
            return queryMatches && speciesMatches && statusMatches;
        });
        searchField.textProperty().addListener((obs, old, val) -> updateFilter.run());
        speciesFilter.valueProperty().addListener((obs, old, val) -> updateFilter.run());
        statusFilter.valueProperty().addListener((obs, old, val) -> updateFilter.run());

        var toolbar = new HBox(10, searchField, speciesFilter, statusFilter);
        if (canManageAnimals()) {
            var addButton = new Button("New Animal");
            addButton.setOnAction(e -> openAnimalDialog(null));
            var editButton = new Button("Edit");
            editButton.setOnAction(e -> {
                var selected = animalTable.getSelectionModel().getSelectedItem();
                if (selected != null) openAnimalDialog(selected);
            });
            var statusButton = new Button("Change Status");
            statusButton.setOnAction(e -> changeSelectedAnimalStatus());
            var archiveButton = new Button("Archive");
            archiveButton.setOnAction(e -> archiveSelectedAnimal());
            toolbar.getChildren().addAll(addButton, editButton, statusButton, archiveButton);
        }
        toolbar.setPadding(new Insets(12));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        addAnimalColumns(animalTable);
        return new BorderPane(animalTable, toolbar, null, null, null);
    }

    private Node createArchivedAnimalTab() {
        var table = new TableView<Animal>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No archived animals found."));

        var filtered = new FilteredList<>(animals, this::isArchivedAnimal);
        table.setItems(filtered);

        var searchField = new TextField();
        searchField.setPromptText("Search archived animals");
        searchField.textProperty().addListener((obs, old, val) -> filtered.setPredicate(animal -> {
            if (!isArchivedAnimal(animal)) return false;
            var q = searchField.getText().trim().toLowerCase(Locale.ROOT);
            return q.isEmpty()
                    || valueOr(animal.getName(), "").toLowerCase(Locale.ROOT).contains(q)
                    || valueOr(animal.getSpecies(), "").toLowerCase(Locale.ROOT).contains(q)
                    || valueOr(animal.getBreed(), "").toLowerCase(Locale.ROOT).contains(q);
        }));

        var editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openAnimalDialog(selected);
        });
        var restoreButton = new Button("Restore");
        restoreButton.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                var updated = copyAnimal(selected);
                updated.setAdoptionStatus("Available");
                saveAnimal(selected, updated);
            }
        });

        var toolbar = new HBox(10, searchField, editButton, restoreButton);
        toolbar.setPadding(new Insets(12));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        addAnimalColumns(table);
        return new BorderPane(table, toolbar, null, null, null);
    }

    private void addAnimalColumns(TableView<Animal> table) {
        table.getColumns().addAll(
                intAnimalCol("ID", Animal::getAnimalId, 55),
                textCol("Name", Animal::getName),
                textCol("Species", Animal::getSpecies),
                textCol("Breed", Animal::getBreed),
                textCol("Age", a -> a.getAge() == null ? "" : a.getAge().toString()),
                textCol("Sex", Animal::getSex),
                textCol("Status", a -> displayAnimalStatus(a.getAdoptionStatus())),
                textCol("Health", Animal::getHealthStatus),
                textCol("Created", Animal::getCreatedAt),
                textCol("Photo", a -> valueOr(a.getPhotoUrl(), "").isBlank() ? "" : "Uploaded")
        );
    }

    private Node createApplicationTab() {
        applicationTable = new TableView<>(applications);
        applicationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        applicationTable.getColumns().addAll(
                intCol("ID", AdoptionApplication::getApplicationId, 55),
                textCol("Applicant", a -> applicantName(a.getApplicantId())),
                textCol("Email", a -> applicantEmail(a.getApplicantId())),
                textCol("Animal", a -> animalName(a.getAnimalId())),
                textCol("Status", a -> displayStatus(a.getStatus())),
                textCol("Submitted", AdoptionApplication::getApplicationDate),
                textCol("Reason / Memo", AdoptionApplication::getReason)
        );

        var detailsButton = new Button("View Applicant");
        detailsButton.setOnAction(e -> showSelectedApplication());
        var toolbar = new HBox(10, detailsButton);
        if (canReviewApplications()) {
            var statusButton = new Button("Change Status");
            statusButton.setOnAction(e -> changeApplicationStatus());
            toolbar.getChildren().add(statusButton);
        }
        toolbar.setPadding(new Insets(12));
        return new BorderPane(applicationTable, toolbar, null, null, null);
    }

    private Node createMedicalTab() {
        medicalTable = new TableView<>(medicalRecords);
        medicalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        medicalTable.getColumns().addAll(
                intCol("ID", MedicalRecord::getMedicalRecordId, 55),
                textCol("Animal", r -> animalName(r.getAnimalId())),
                textCol("Type", MedicalRecord::getTreatmentType),
                textCol("Date", MedicalRecord::getRecordDate),
                textCol("Next Visit", MedicalRecord::getNextAppointment),
                textCol("Veterinarian", MedicalRecord::getVeterinarianName),
                textCol("Description", MedicalRecord::getDescription)
        );
        var addVaccine = new Button("Add Vaccine");
        addVaccine.setOnAction(e -> openMedicalDialog("Vaccine"));
        var addTreatment = new Button("Add Treatment");
        addTreatment.setOnAction(e -> openMedicalDialog("Treatment"));
        var toolbar = new HBox(10, addVaccine, addTreatment);
        toolbar.setPadding(new Insets(12));
        return new BorderPane(medicalTable, toolbar, null, null, null);
    }

    private Node createCareTab() {
        careTable = new TableView<>(careLogs);
        careTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        careTable.getColumns().addAll(
                intCol("ID", CareLog::getCareLogId, 55),
                textCol("Date", CareLog::getLogDate),
                textCol("Animal", c -> animalName(c.getAnimalId())),
                textCol("Staff", c -> staffName(c.getUserId())),
                textCol("Meal", CareLog::getFeedingNotes),
                textCol("Cleaning", CareLog::getCleaningNotes),
                textCol("Behavior", CareLog::getBehaviorNotes)
        );
        var addButton = new Button("Add Care Log");
        addButton.setOnAction(e -> openCareDialog());
        var todayButton = new Button("Today Only");
        todayButton.setOnAction(e -> careTable.setItems(careLogs.filtered(c -> LocalDate.now().toString().equals(c.getLogDate()))));
        var allButton = new Button("All Dates");
        allButton.setOnAction(e -> careTable.setItems(careLogs));
        var toolbar = new HBox(10, addButton, todayButton, allButton);
        toolbar.setPadding(new Insets(12));
        return new BorderPane(careTable, toolbar, null, null, null);
    }

    private Node createReportsTab() {
        reportsContent = new VBox(14);
        reportsContent.setPadding(new Insets(18));
        refreshReports();
        return new ScrollPane(reportsContent);
    }

    private Node createStaffTab() {
        var activeUsers = new FilteredList<>(staffUsers, StaffUser::isActive);
        var table = createStaffTable(activeUsers, "No active staff found.");

        var addButton = new Button("New Staff");
        addButton.setOnAction(e -> openStaffDialog(null, table));
        var editButton = new Button("Edit Staff");
        editButton.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openStaffDialog(selected, table);
        });
        var archiveButton = new Button("Archive");
        archiveButton.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                var updated = copyStaffUser(selected);
                updated.setActive(false);
                saveStaffUser(selected, updated, null, table);
            }
        });

        var toolbar = new HBox(10, addButton, editButton, archiveButton);
        var box = new VBox(10, new Label("Staff management is visible to Admin users."), toolbar, table);
        box.setPadding(new Insets(18));
        return box;
    }

    private Node createArchivedStaffTab() {
        var archivedUsers = new FilteredList<>(staffUsers, user -> !user.isActive());
        var table = createStaffTable(archivedUsers, "No archived staff found.");

        var editButton = new Button("Edit Staff");
        editButton.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openStaffDialog(selected, table);
        });
        var restoreButton = new Button("Restore");
        restoreButton.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                var updated = copyStaffUser(selected);
                updated.setActive(true);
                saveStaffUser(selected, updated, null, table);
            }
        });

        var toolbar = new HBox(10, editButton, restoreButton);
        var box = new VBox(10, new Label("Archived staff users are hidden from the Staff tab."), toolbar, table);
        box.setPadding(new Insets(18));
        return box;
    }

    private TableView<StaffUser> createStaffTable(ObservableList<StaffUser> users, String placeholder) {
        var table = new TableView<>(users);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(placeholder));
        table.getColumns().addAll(
                intCol("ID", StaffUser::getUserId, 55),
                textCol("Name", StaffUser::getFullName),
                textCol("Email", StaffUser::getEmail),
                textCol("Role", StaffUser::getRole),
                textCol("Status", u -> u.isActive() ? "Active" : "Archived"),
                textCol("Created", StaffUser::getCreatedAt)
        );
        return table;
    }

    private void loadAllData() {
        statusLabel.setText("Loading data...");
        executor.execute(() -> {
            try {
                var loadedAnimals = AnimalService.getAll();
                var loadedApplicants = ShelterService.getApplicants();
                var loadedApplications = ShelterService.getApplications();
                var loadedMedical = ShelterService.getMedicalRecords();
                var loadedCare = ShelterService.getCareLogs();
                var loadedUsers = canManageUsers() ? ShelterService.getUsers() : staffUsers;
                Platform.runLater(() -> {
                    animals.setAll(loadedAnimals);
                    applicants.setAll(loadedApplicants);
                    applications.setAll(loadedApplications);
                    medicalRecords.setAll(loadedMedical);
                    careLogs.setAll(loadedCare);
                    staffUsers.setAll(loadedUsers);
                    statusLabel.setText("Loaded shelter data from API.");
                    refreshAll();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    seedFallbackData();
                    statusLabel.setText("API unavailable. Showing local demo data: " + e.getMessage());
                    refreshAll();
                });
            }
        });
    }

    private void openAnimalDialog(Animal existing) {
        var dialog = new Dialog<Void>();
        dialog.setTitle(existing == null ? "New Animal" : "Edit Animal");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        var name = new TextField(existing == null ? "" : valueOr(existing.getName(), ""));
        var species = combo("Dog", "Cat", "Rabbit", "Bird", "Other");
        species.setValue(existing == null ? "Dog" : valueOr(existing.getSpecies(), "Dog"));
        var breed = new TextField(existing == null ? "" : valueOr(existing.getBreed(), ""));
        var age = new Spinner<Integer>(0, 30, existing == null || existing.getAge() == null ? 1 : existing.getAge());
        var sex = combo("Female", "Male", "Unknown");
        sex.setValue(existing == null ? "Unknown" : valueOr(existing.getSex(), "Unknown"));
        var status = combo("Available", "Pending", "Adopted", "Unavailable");
        status.setValue(existing == null ? "Available" : valueOr(existing.getAdoptionStatus(), "Available"));
        var health = new TextField(existing == null ? "Healthy" : valueOr(existing.getHealthStatus(), ""));
        var intake = new TextField(existing == null ? LocalDate.now().toString() : valueOr(existing.getIntakeDate(), ""));
        var selectedPhoto = new SimpleObjectProperty<File>();
        var photoLabel = new Label(existing == null || valueOr(existing.getPhotoUrl(), "").isBlank() ? "No photo selected" : "Current photo");
        var choosePhoto = new Button("Upload Photo");
        var notes = new TextArea(existing == null ? "" : valueOr(existing.getNotes(), ""));
        notes.setPrefRowCount(3);

        var grid = formGrid();
        grid.addRow(0, new Label("Name"), name);
        grid.addRow(1, new Label("Species"), species);
        grid.addRow(2, new Label("Breed"), breed);
        grid.addRow(3, new Label("Age"), age);
        grid.addRow(4, new Label("Sex"), sex);
        grid.addRow(5, new Label("Status"), status);
        grid.addRow(6, new Label("Health"), health);
        grid.addRow(7, new Label("Intake Date"), intake);
        grid.addRow(8, new Label("Photo"), new HBox(10, choosePhoto, photoLabel));
        grid.addRow(9, new Label("Notes"), notes);

        var preview = new ImageView();
        preview.setFitWidth(220);
        preview.setFitHeight(160);
        preview.setPreserveRatio(true);
        choosePhoto.setOnAction(e -> {
            var chooser = new FileChooser();
            chooser.setTitle("Choose Animal Photo");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.jpeg", "*.png", "*.webp", "*.gif"));
            var file = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedPhoto.set(file);
                photoLabel.setText(file.getName());
                updatePreview(preview, file.toURI().toString());
            }
        });
        updatePreview(preview, existing == null ? null : existing.getPhotoUrl());

        var saveStatus = new Label();
        saveStatus.setStyle("-fx-text-fill: #57606f;");
        dialog.getDialogPane().setContent(new VBox(10, new HBox(18, grid, preview), saveStatus));
        dialog.setOnShown(e -> {
            var okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                event.consume();
                var animal = existing == null ? new Animal() : copyAnimal(existing);
                animal.setAnimalId(existing == null ? nextAnimalId() : existing.getAnimalId());
                animal.setName(name.getText().trim());
                animal.setSpecies(species.getValue());
                animal.setBreed(breed.getText().trim());
                animal.setAge(age.getValue());
                animal.setSex(sex.getValue());
                animal.setAdoptionStatus(status.getValue());
                animal.setHealthStatus(health.getText().trim());
                animal.setIntakeDate(intake.getText().trim());
                animal.setPhotoUrl(existing == null ? null : existing.getPhotoUrl());
                animal.setNotes(notes.getText().trim());
                if (animal.getName().isBlank()) {
                    saveStatus.setStyle("-fx-text-fill: red;");
                    saveStatus.setText("Animal name is required.");
                    return;
                }

                okButton.setDisable(true);
                saveStatus.setStyle("-fx-text-fill: #57606f;");
                saveStatus.setText(selectedPhoto.get() == null ? "Saving animal..." : "Uploading photo...");
                executor.execute(() -> {
                    try {
                        var saved = saveAnimalToApi(existing, animal, selectedPhoto.get());
                        Platform.runLater(() -> {
                            applySavedAnimal(existing, saved);
                            statusLabel.setText("Animal saved to API.");
                            refreshAll();
                            dialog.close();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            okButton.setDisable(false);
                            saveStatus.setStyle("-fx-text-fill: red;");
                            saveStatus.setText("Save failed: " + ex.getMessage());
                            statusLabel.setText("Animal save failed: " + ex.getMessage());
                        });
                    }
                });
            });
        });
        dialog.showAndWait();
    }

    private void saveAnimal(Animal existing, Animal animal) {
        saveAnimal(existing, animal, null);
    }

    private void saveAnimal(Animal existing, Animal animal, File photoFile) {
        if (animal.getName().isBlank()) {
            statusLabel.setText("Animal name is required.");
            return;
        }
        executor.execute(() -> {
            try {
                var saved = saveAnimalToApi(existing, animal, photoFile);
                Platform.runLater(() -> {
                    applySavedAnimal(existing, saved);
                    statusLabel.setText("Animal saved to API.");
                    refreshAll();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Animal save failed: " + e.getMessage());
                });
            }
        });
    }

    private Animal saveAnimalToApi(Animal existing, Animal animal, File photoFile) throws Exception {
        if (photoFile != null) {
            Platform.runLater(() -> statusLabel.setText("Uploading animal photo..."));
            animal.setPhotoUrl(AnimalService.uploadPhoto(photoFile));
        }
        return existing == null ? AnimalService.create(animal) : AnimalService.update(animal);
    }

    private void applySavedAnimal(Animal existing, Animal saved) {
        if (existing == null) animals.add(saved);
        else animals.set(animals.indexOf(existing), saved);
    }

    private void changeSelectedAnimalStatus() {
        var selected = animalTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        var dialog = new ChoiceDialog<>(valueOr(selected.getAdoptionStatus(), "Available"), "Available", "Pending", "Adopted", "Unavailable");
        dialog.setTitle("Change Animal Status");
        dialog.setHeaderText(selected.getName());
        dialog.showAndWait().ifPresent(status -> {
            var updated = copyAnimal(selected);
            updated.setAdoptionStatus(status);
            saveAnimal(selected, updated);
        });
    }

    private void archiveSelectedAnimal() {
        var selected = animalTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        var updated = copyAnimal(selected);
        updated.setAdoptionStatus("Unavailable");
        saveAnimal(selected, updated);
    }

    private void showSelectedApplication() {
        var selected = applicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        var applicant = applicant(selected.getApplicantId());
        new Alert(Alert.AlertType.INFORMATION,
                "Applicant: " + applicantName(selected.getApplicantId())
                        + "\nEmail: " + applicantEmail(selected.getApplicantId())
                        + "\nPhone: " + (applicant == null ? "" : valueOr(applicant.getPhone(), ""))
                        + "\nHousing: " + (applicant == null ? "" : valueOr(applicant.getHousingType(), ""))
                        + "\nAnimal: " + animalName(selected.getAnimalId())
                        + "\nStatus: " + valueOr(selected.getStatus(), "")
                        + "\nReason: " + valueOr(selected.getReason(), "")
                        + "\nLiving: " + valueOr(selected.getLivingSituation(), "")
                        + "\nWork: " + valueOr(selected.getWorkSchedule(), ""),
                ButtonType.OK).showAndWait();
    }

    private void changeApplicationStatus() {
        var selected = applicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        var dialog = new ChoiceDialog<>(valueOr(selected.getStatus(), "Pending"), "Pending", "UnderReview", "Approved", "Rejected");
        dialog.setTitle("Application Status");
        dialog.setHeaderText(applicantName(selected.getApplicantId()) + " - " + animalName(selected.getAnimalId()));
        Optional<String> nextStatus = dialog.showAndWait();
        if (nextStatus.isEmpty()) return;

        var memoDialog = new TextInputDialog(valueOr(selected.getReason(), ""));
        memoDialog.setTitle("Approval / Rejection Memo");
        memoDialog.setHeaderText("Leave a staff memo");
        Optional<String> memo = memoDialog.showAndWait();
        if (memo.isEmpty()) return;

        executor.execute(() -> {
            try {
                var updated = copyApplication(selected);
                updated.setStatus(nextStatus.get());
                updated.setReviewedBy(ApiClient.getUserId() == 0 ? null : ApiClient.getUserId());
                updated.setReviewedDate(LocalDate.now().toString());
                updated.setReason(memo.get());
                ShelterService.updateApplication(updated);
                Platform.runLater(() -> {
                    applications.set(applications.indexOf(selected), updated);
                    applicationTable.refresh();
                    statusLabel.setText("Application status and memo saved to API.");
                    refreshAll();
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Application update failed: " + e.getMessage()));
            }
        });
    }

    private void openMedicalDialog(String type) {
        var animal = animalChoiceDialog("Select Animal");
        if (animal.isEmpty()) return;
        var dialog = new Dialog<MedicalRecord>();
        dialog.setTitle("Add " + type);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        var description = new TextArea();
        description.setPrefRowCount(3);
        var veterinarian = new TextField();
        var next = new TextField(LocalDate.now().plusMonths(1).toString());
        var grid = formGrid();
        grid.addRow(0, new Label("Animal"), new Label(animal.get().getName()));
        grid.addRow(1, new Label("Description"), description);
        grid.addRow(2, new Label("Veterinarian"), veterinarian);
        grid.addRow(3, new Label("Next Visit"), next);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            var record = new MedicalRecord();
            record.setAnimalId(animal.get().getAnimalId());
            record.setRecordDate(LocalDate.now().toString());
            record.setTreatmentType(type);
            record.setDescription(description.getText().trim());
            record.setVeterinarianName(veterinarian.getText().trim());
            record.setNextAppointment(next.getText().trim());
            return record;
        });
        dialog.showAndWait().ifPresent(this::saveMedicalRecord);
    }

    private void saveMedicalRecord(MedicalRecord record) {
        executor.execute(() -> {
            try {
                ShelterService.createMedicalRecord(record);
                Platform.runLater(() -> {
                    medicalRecords.add(record);
                    statusLabel.setText("Medical record saved to API.");
                    refreshAll();
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Medical record save failed: " + e.getMessage()));
            }
        });
    }

    private void openCareDialog() {
        var animal = animalChoiceDialog("Select Animal");
        if (animal.isEmpty()) return;
        var dialog = new Dialog<CareLog>();
        dialog.setTitle("Add Care Log");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        var feeding = new TextArea();
        feeding.setPrefRowCount(2);
        var cleaning = new TextArea();
        cleaning.setPrefRowCount(2);
        var behavior = new TextArea();
        behavior.setPrefRowCount(2);
        var grid = formGrid();
        grid.addRow(0, new Label("Animal"), new Label(animal.get().getName()));
        grid.addRow(1, new Label("Feeding"), feeding);
        grid.addRow(2, new Label("Cleaning"), cleaning);
        grid.addRow(3, new Label("Behavior"), behavior);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            var log = new CareLog();
            log.setAnimalId(animal.get().getAnimalId());
            log.setUserId(ApiClient.getUserId());
            log.setLogDate(LocalDate.now().toString());
            log.setFeedingNotes(feeding.getText().trim());
            log.setCleaningNotes(cleaning.getText().trim());
            log.setBehaviorNotes(behavior.getText().trim());
            return log;
        });
        dialog.showAndWait().ifPresent(this::saveCareLog);
    }

    private void saveCareLog(CareLog log) {
        executor.execute(() -> {
            try {
                ShelterService.createCareLog(log);
                Platform.runLater(() -> {
                    careLogs.add(log);
                    statusLabel.setText("Care log saved to API.");
                    refreshAll();
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Care log save failed: " + e.getMessage()));
            }
        });
    }

    private void openStaffDialog(StaffUser existing, TableView<StaffUser> table) {
        var dialog = new Dialog<StaffFormResult>();
        dialog.setTitle(existing == null ? "New Staff" : "Edit Staff");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        var name = new TextField(existing == null ? "" : valueOr(existing.getFullName(), ""));
        var email = new TextField(existing == null ? "" : valueOr(existing.getEmail(), ""));
        var password = new PasswordField();
        password.setPromptText(existing == null ? "Required" : "Leave blank to keep current password");
        var role = combo("Staff", "Admin", "AdoptionRepresentative", "Volunteer");
        role.setValue(existing == null ? "Staff" : valueOr(existing.getRole(), "Staff"));
        var active = new CheckBox("Active");
        active.setSelected(existing == null || existing.isActive());

        var grid = formGrid();
        grid.addRow(0, new Label("Name"), name);
        grid.addRow(1, new Label("Email"), email);
        grid.addRow(2, new Label("Password"), password);
        grid.addRow(3, new Label("Role"), role);
        grid.addRow(4, new Label("Status"), active);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            var user = existing == null ? new StaffUser() : copyStaffUser(existing);
            user.setFullName(name.getText().trim());
            user.setEmail(email.getText().trim());
            user.setRole(role.getValue());
            user.setActive(active.isSelected());
            return new StaffFormResult(user, password.getText());
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.user().getFullName().isBlank() || result.user().getEmail().isBlank()) {
                statusLabel.setText("Staff name and email are required.");
                return;
            }
            if (existing == null && result.password().isBlank()) {
                statusLabel.setText("Password is required for new staff.");
                return;
            }
            saveStaffUser(existing, result.user(), result.password(), table);
        });
    }

    private void saveStaffUser(StaffUser existing, StaffUser user, String password, TableView<StaffUser> table) {
        if (existing != null && existing.getUserId() == ApiClient.getUserId()
                && (!user.isActive() || !"Admin".equalsIgnoreCase(valueOr(user.getRole(), "")))) {
            statusLabel.setText("You cannot deactivate or remove Admin from your own account.");
            return;
        }
        executor.execute(() -> {
            try {
                var saved = existing == null
                        ? ShelterService.createUser(user, password)
                        : ShelterService.updateUser(user, password);
                Platform.runLater(() -> {
                    if (existing == null) staffUsers.add(saved);
                    else staffUsers.set(staffUsers.indexOf(existing), saved);
                    table.refresh();
                    statusLabel.setText("Staff user saved to API.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Staff save failed: " + e.getMessage()));
            }
        });
    }

    private Optional<Animal> animalChoiceDialog(String title) {
        var dialog = new ChoiceDialog<Animal>(animals.isEmpty() ? null : animals.get(0), animals);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        return dialog.showAndWait();
    }

    private void refreshAll() {
        refreshDashboard();
        refreshReports();
        if (applicationTable != null) applicationTable.refresh();
        if (medicalTable != null) medicalTable.refresh();
        if (careTable != null) careTable.refresh();
    }

    private void refreshDashboard() {
        if (dashboardContent == null) return;
        dashboardContent.getChildren().clear();
        if (isVolunteerRole()) {
            refreshVolunteerDashboard();
            return;
        }
        var stats = new HBox(12,
                statCard("Total Animals", String.valueOf(animals.size())),
                statCard("Available", String.valueOf(countStatus("Available"))),
                statCard("Pending", String.valueOf(countStatus("Pending"))),
                statCard("Adopted", String.valueOf(countStatus("Adopted"))),
                statCard("Unreviewed Applications", String.valueOf(countApplicationStatus("Pending")))
        );
        var today = LocalDate.now().toString();
        var tasks = new ListView<String>(FXCollections.observableArrayList(
                careLogs.stream().anyMatch(c -> today.equals(c.getLogDate())) ? "Today care logs have entries." : "Add today's care logs.",
                medicalRecords.stream().anyMatch(m -> today.equals(m.getNextAppointment())) ? "Medical visit scheduled today." : "Review upcoming medical visits.",
                countApplicationStatus("Pending") > 0 ? "Review pending adoption applications." : "No unreviewed applications."
        ));
        tasks.setPrefHeight(120);
        var recent = new ListView<String>(FXCollections.observableArrayList(animals.stream()
                .sorted(Comparator.comparing((Animal a) -> valueOr(a.getIntakeDate(), "")).reversed())
                .limit(5)
                .map(a -> a.getName() + " - " + valueOr(a.getSpecies(), "") + " - " + valueOr(a.getAdoptionStatus(), ""))
                .toList()));
        recent.setPrefHeight(150);
        dashboardContent.getChildren().addAll(stats, section("Today's Work", tasks), section("Recently Added Animals", recent));
    }

    private void refreshVolunteerDashboard() {
        var stats = new HBox(12,
                statCard("Total Animals", String.valueOf(animals.size())),
                statCard("Available", String.valueOf(countStatus("Available"))),
                statCard("Pending", String.valueOf(countStatus("Pending"))),
                statCard("Care Logs", String.valueOf(careLogs.size()))
        );
        var today = LocalDate.now().toString();
        var tasks = new ListView<String>(FXCollections.observableArrayList(
                careLogs.stream().anyMatch(c -> today.equals(c.getLogDate())) ? "Today care logs have entries." : "Add today's care logs.",
                countStatus("Available") > 0 ? "Review available animal care notes." : "No available animals right now."
        ));
        tasks.setPrefHeight(95);
        var recent = new ListView<String>(FXCollections.observableArrayList(animals.stream()
                .sorted(Comparator.comparing((Animal a) -> valueOr(a.getIntakeDate(), "")).reversed())
                .limit(5)
                .map(a -> a.getName() + " - " + valueOr(a.getSpecies(), "") + " - " + displayAnimalStatus(a.getAdoptionStatus()))
                .toList()));
        recent.setPrefHeight(150);
        dashboardContent.getChildren().addAll(stats, section("Today's Care", tasks), section("Recent Animals", recent));
    }

    private void refreshReports() {
        if (reportsContent == null) return;
        reportsContent.getChildren().clear();
        long approved = countApplicationStatus("Approved");
        long rejected = countApplicationStatus("Rejected");
        long decided = approved + rejected;
        String approvalRate = decided == 0 ? "0%" : Math.round((approved * 100.0) / decided) + "%";
        var summary = FXCollections.observableArrayList(
                "Month: " + YearMonth.now(),
                "Monthly adoptions: " + countStatus("Adopted"),
                "Available animals: " + countStatus("Available"),
                "Archived animals: " + countStatus("Unavailable"),
                "Pending applications: " + applications.stream().filter(a -> statusIs(a, "Pending") || statusIs(a, "UnderReview")).count(),
                "Dogs: " + countSpecies("Dog"),
                "Cats: " + countSpecies("Cat"),
                "Approval rate: " + approvalRate,
                "Operation summary: " + animals.size() + " animals, " + applications.size() + " applications, " + careLogs.size() + " care logs."
        );
        reportsContent.getChildren().add(section("Shelter Reports", new ListView<>(summary)));
    }

    private VBox section(String title, javafx.scene.Node content) {
        var label = new Label(title);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        var box = new VBox(8, label, content);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: white; -fx-border-color: #dfe4ea; -fx-background-radius: 6; -fx-border-radius: 6;");
        return box;
    }

    private VBox statCard(String title, String value) {
        var valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
        var titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #57606f;");
        var box = new VBox(4, valueLabel, titleLabel);
        box.setPadding(new Insets(14));
        box.setMinWidth(165);
        box.setStyle("-fx-background-color: white; -fx-border-color: #dfe4ea; -fx-background-radius: 6; -fx-border-radius: 6;");
        return box;
    }

    private GridPane formGrid() {
        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        return grid;
    }

    private void updatePreview(ImageView preview, String url) {
        try {
            preview.setImage(url == null || url.isBlank() ? null : new Image(url, true));
        } catch (Exception e) {
            preview.setImage(null);
        }
    }

    private String animalName(int animalId) {
        return animals.stream().filter(a -> a.getAnimalId() == animalId).findFirst().map(Animal::getName).orElse("Animal #" + animalId);
    }

    private Applicant applicant(int applicantId) {
        return applicants.stream().filter(a -> a.getApplicantId() == applicantId).findFirst().orElse(null);
    }

    private String applicantName(int applicantId) {
        var applicant = applicant(applicantId);
        return applicant == null ? "Applicant #" + applicantId : valueOr(applicant.getFullName(), "Applicant #" + applicantId);
    }

    private String applicantEmail(int applicantId) {
        var applicant = applicant(applicantId);
        return applicant == null ? "" : valueOr(applicant.getEmail(), "");
    }

    private String staffName(int userId) {
        return staffUsers.stream().filter(u -> u.getUserId() == userId).findFirst().map(StaffUser::getFullName).orElse("Staff #" + userId);
    }

    private long countStatus(String status) {
        return animals.stream().filter(a -> status.equalsIgnoreCase(valueOr(a.getAdoptionStatus(), ""))).count();
    }

    private long countApplicationStatus(String status) {
        return applications.stream().filter(a -> statusIs(a, status)).count();
    }

    private boolean statusIs(AdoptionApplication application, String status) {
        return normalizeStatus(status).equals(normalizeStatus(application.getStatus()));
    }

    private String displayStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "underreview" -> "Under Review";
            case "pending" -> "Pending";
            case "approved" -> "Approved";
            case "rejected" -> "Rejected";
            default -> valueOr(status, "");
        };
    }

    private String displayAnimalStatus(String status) {
        return "Unavailable".equalsIgnoreCase(valueOr(status, "")) ? "Archived" : valueOr(status, "");
    }

    private String animalStatusValue(String displayStatus) {
        return "Archived".equalsIgnoreCase(valueOr(displayStatus, "")) ? "Unavailable" : valueOr(displayStatus, "");
    }

    private boolean isArchivedAnimal(Animal animal) {
        return animalStatusValue("Archived").equalsIgnoreCase(valueOr(animal.getAdoptionStatus(), ""));
    }

    private boolean canViewDashboard() {
        return isAdminRole() || isStaffRole() || isVolunteerRole();
    }

    private boolean canViewAnimals() {
        return isAdminRole() || isStaffRole() || isVolunteerRole();
    }

    private boolean canManageAnimals() {
        return isAdminRole() || isStaffRole();
    }

    private boolean canViewApplications() {
        return isAdminRole() || isAdoptionRepresentativeRole();
    }

    private boolean canReviewApplications() {
        return isAdminRole() || isAdoptionRepresentativeRole();
    }

    private boolean canViewMedicalRecords() {
        return isAdminRole() || isStaffRole();
    }

    private boolean canViewCareLogs() {
        return isAdminRole() || isStaffRole() || isVolunteerRole();
    }

    private boolean canViewReports() {
        return isAdminRole() || isStaffRole() || isAdoptionRepresentativeRole();
    }

    private boolean canManageUsers() {
        return isAdminRole();
    }

    private boolean isAdminRole() {
        return roleIs("Admin") || roleIs("Administrator");
    }

    private boolean isStaffRole() {
        return roleIs("Staff") || roleIs("ShelterStaff");
    }

    private boolean isAdoptionRepresentativeRole() {
        return roleIs("AdoptionRepresentative") || roleIs("Adoption Representative");
    }

    private boolean isVolunteerRole() {
        return roleIs("Volunteer");
    }

    private boolean roleIs(String role) {
        return normalizeStatus(role).equals(normalizeStatus(ApiClient.getRole()));
    }

    private String displayRole() {
        if (isAdminRole()) return "Administrator";
        if (isStaffRole()) return "Shelter Staff";
        if (isAdoptionRepresentativeRole()) return "Adoption Representative";
        if (isVolunteerRole()) return "Volunteer";
        return valueOr(ApiClient.getRole(), "Staff");
    }

    private String normalizeStatus(String status) {
        return valueOr(status, "").replace(" ", "").toLowerCase(Locale.ROOT);
    }

    private long countSpecies(String species) {
        return animals.stream().filter(a -> species.equalsIgnoreCase(valueOr(a.getSpecies(), ""))).count();
    }

    private int nextAnimalId() {
        return animals.stream().mapToInt(Animal::getAnimalId).max().orElse(0) + 1;
    }

    private static ComboBox<String> combo(String... values) {
        var combo = new ComboBox<String>(FXCollections.observableArrayList(values));
        combo.setValue(values.length == 0 ? null : values[0]);
        return combo;
    }

    private static String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static Animal copyAnimal(Animal source) {
        var animal = new Animal();
        animal.setAnimalId(source.getAnimalId());
        animal.setName(source.getName());
        animal.setSpecies(source.getSpecies());
        animal.setBreed(source.getBreed());
        animal.setAge(source.getAge());
        animal.setSex(source.getSex());
        animal.setIntakeDate(source.getIntakeDate());
        animal.setAdoptionStatus(source.getAdoptionStatus());
        animal.setHealthStatus(source.getHealthStatus());
        animal.setNotes(source.getNotes());
        animal.setPhotoUrl(source.getPhotoUrl());
        animal.setCreatedAt(source.getCreatedAt());
        return animal;
    }

    private static StaffUser copyStaffUser(StaffUser source) {
        var user = new StaffUser();
        user.setUserId(source.getUserId());
        user.setFullName(source.getFullName());
        user.setEmail(source.getEmail());
        user.setRole(source.getRole());
        user.setActive(source.isActive());
        user.setCreatedAt(source.getCreatedAt());
        return user;
    }

    private static AdoptionApplication copyApplication(AdoptionApplication source) {
        var application = new AdoptionApplication();
        application.setApplicationId(source.getApplicationId());
        application.setAnimalId(source.getAnimalId());
        application.setApplicantId(source.getApplicantId());
        application.setApplicationDate(source.getApplicationDate());
        application.setStatus(source.getStatus());
        application.setReason(source.getReason());
        application.setLivingSituation(source.getLivingSituation());
        application.setWorkSchedule(source.getWorkSchedule());
        application.setHasYard(source.isHasYard());
        application.setLandlordApproval(source.isLandlordApproval());
        application.setOtherPetsDetails(source.getOtherPetsDetails());
        application.setReviewedBy(source.getReviewedBy());
        application.setReviewedDate(source.getReviewedDate());
        return application;
    }

    private void seedFallbackData() {
        if (!animals.isEmpty()) return;
        var mochi = new Animal();
        mochi.setAnimalId(1);
        mochi.setName("Mochi");
        mochi.setSpecies("Dog");
        mochi.setBreed("Shiba Mix");
        mochi.setAge(3);
        mochi.setSex("Female");
        mochi.setIntakeDate(LocalDate.now().minusDays(2).toString());
        mochi.setAdoptionStatus("Available");
        mochi.setHealthStatus("Healthy");

        var luna = new Animal();
        luna.setAnimalId(2);
        luna.setName("Luna");
        luna.setSpecies("Cat");
        luna.setBreed("Domestic Short Hair");
        luna.setAge(2);
        luna.setSex("Female");
        luna.setIntakeDate(LocalDate.now().minusDays(8).toString());
        luna.setAdoptionStatus("Pending");
        luna.setHealthStatus("Monitoring");
        animals.setAll(mochi, luna);

        var applicant = new Applicant();
        applicant.setApplicantId(1);
        applicant.setFullName("Aiko Tanaka");
        applicant.setEmail("aiko@example.com");
        applicant.setPhone("555-0101");
        applicant.setHousingType("House");
        applicants.setAll(applicant);

        var application = new AdoptionApplication();
        application.setApplicationId(1);
        application.setAnimalId(1);
        application.setApplicantId(1);
        application.setApplicationDate(LocalDate.now().toString());
        application.setStatus("Pending");
        application.setReason("Looking for a family dog.");
        applications.setAll(application);

        var user = new StaffUser();
        user.setUserId(ApiClient.getUserId());
        user.setFullName(valueOr(ApiClient.getRole(), "Staff"));
        user.setRole(valueOr(ApiClient.getRole(), "Staff"));
        staffUsers.setAll(user);
    }

    private static <T> TableColumn<T, Integer> intCol(String title, IntValue<T> value, int width) {
        var col = new TableColumn<T, Integer>(title);
        col.setCellValueFactory(c -> new SimpleIntegerProperty(value.get(c.getValue())).asObject());
        col.setMaxWidth(width);
        col.setMinWidth(width);
        return col;
    }

    private static TableColumn<Animal, Integer> intAnimalCol(String title, AnimalIntValue value, int width) {
        var col = new TableColumn<Animal, Integer>(title);
        col.setCellValueFactory(c -> new SimpleIntegerProperty(value.get(c.getValue())).asObject());
        col.setMaxWidth(width);
        col.setMinWidth(width);
        return col;
    }

    private static <T> TableColumn<T, String> textCol(String title, TextValue<T> value) {
        var col = new TableColumn<T, String>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(value.get(c.getValue())));
        return col;
    }

    @FunctionalInterface
    private interface TextValue<T> {
        String get(T value);
    }

    @FunctionalInterface
    private interface IntValue<T> {
        int get(T value);
    }

    @FunctionalInterface
    private interface AnimalIntValue {
        int get(Animal animal);
    }

    private record StaffFormResult(StaffUser user, String password) {}
}
