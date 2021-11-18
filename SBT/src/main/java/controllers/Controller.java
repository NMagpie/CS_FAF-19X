package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.FileProcessing;
import core.registryutils.Registry;
import core.registryutils.registryitem.Item;
import customitem.CustomItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    @FXML
    TableView<CustomItem> table = new TableView<>();

    @FXML
    Label statusText = new Label();

    @FXML
    TextField searchBar = new TextField();

    @FXML
    Button browse = new Button();

    @FXML
    Button save = new Button();

    @FXML
    CheckBox selectAll = new CheckBox();

    @FXML
    Button checkItems = new Button();

    private Stage stageCheck = null;

    private void initializeStageCheck() throws IOException {
        stageCheck = new Stage();
        Scene scene = new Scene(Main.getFxmlLoaderCheck().load());
        stageCheck.setTitle("Check items");
        stageCheck.setScene(scene);
    }

    public void setCheckItemsActive(boolean bool) { this.checkItems.setDisable(!bool); }

    public boolean getCheckItemsActive() { return !this.checkItems.isDisabled(); }

    public void setStatusText(String string) {
        this.statusText.setText(string);
    }

    @FXML
    protected void checkItemsPressed() throws IOException {

        ArrayList<Item> items = Registry.checkItems();

        if (items == null || items.isEmpty()) return;

        if (stageCheck == null)
            initializeStageCheck();

        ControllerCheck controller = Main.getFxmlLoaderCheck().getController();
        controller.setItemListView(items);

        stageCheck.show();
    }

    @FXML
    protected void browseFile() throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audit or JSON files", "*.audit", "*.json"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            statusText.setText("File was imported successfully!");

            if (file.getName().matches(".*\\.audit$"))
                FileProcessing.parseFileObject(file);
            else FileProcessing.parseJSONFile(file);

            updateTable();

            statusText.setText("File was parsed successfully!");

        }
    }

    @FXML
    protected void saveFile() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        ArrayList<CustomItem> items = FileProcessing.selectedArrayList();

        if (items.isEmpty()) return;

        FileChooser fileChooserSave = new FileChooser();
        fileChooserSave.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        File fileSave = fileChooserSave.showSaveDialog(null);

        if (fileSave != null) {

            mapper.writerWithDefaultPrettyPrinter().writeValue(fileSave, items);

            statusText.setText("File was saved successfully!");
        }
    }

    @FXML
    protected void selectAllAction() {
        ArrayList<CustomItem> customItems = FileProcessing.getCustomItems();
        boolean selected = selectAll.isSelected();
        if (customItems != null)
            for (CustomItem customItem : customItems)
                if (selected != customItem.isSelected())
                    customItem.fire();
    }

    private void updateTable() {

        ObservableList<CustomItem> observableList = FXCollections.observableArrayList(FileProcessing.getCustomItems());

        FilteredList<CustomItem> filteredItems = new FilteredList<>(observableList, b -> true);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filteredItems.setPredicate(customItem -> {
                            if (newValue == null || newValue.isEmpty()) {
                                return true;
                            }

                            String lowerCaseFilter = newValue.toLowerCase();

                            if (customItem.getRegKey() != null && customItem.getRegKey().toLowerCase().contains(lowerCaseFilter))
                                return true;
                            else if (customItem.getRegItem() != null && customItem.getRegItem().toLowerCase().contains(lowerCaseFilter)) {
                                return true;
                            } else if (customItem.getType() != null && customItem.getType().toLowerCase().contains(lowerCaseFilter)) {
                                return true;
                            } else if (customItem.getValueData() != null && customItem.getValueData().toLowerCase().contains(lowerCaseFilter)) {
                                return true;
                            } else if (customItem.getValueType() != null && customItem.getValueType().toLowerCase().contains(lowerCaseFilter)) {
                                return true;
                            } else if (customItem.getDescription() != null && customItem.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                                return true;
                            } else if (customItem.getInfo() != null && customItem.getInfo().toLowerCase().contains(lowerCaseFilter)) {
                                return true;
                            } else if (customItem.getReference() != null && customItem.getReference().toLowerCase().contains(lowerCaseFilter)) {
                                return true;
                            } else
                                return customItem.getRegOption() != null && customItem.getRegOption().toLowerCase().contains(lowerCaseFilter);

                        }
                )
        );

        SortedList<CustomItem> sortedList = new SortedList<>(filteredItems);

        //table.setItems(sortedList);

        sortedList.comparatorProperty().bind(table.comparatorProperty());

        table.setItems(sortedList);
    }

}