package com.kles.view.mi;

import com.kles.MainApp;
import com.kles.mi.MIInputData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import resources.ResourceCSS;

public class MIInputDataListCell extends ListCell<MIInputData> {

    private static final String FONT_AWESOME = "FontAwesome";

    private final GridPane grid = new GridPane();
    private final Button buttonDel = new Button();
    private final Button buttonEdit = new Button();
    private final FontAwesomeIconView iconEdit = new FontAwesomeIconView();
    private final FontAwesomeIconView iconDel = new FontAwesomeIconView();
    private final Label name = new Label();
    private final Label detail = new Label();
    private MIInputData data;
    private PopOver popOver;
    private final MIInputDataListCell cell;
    private MainApp mainApp;

    public MIInputDataListCell(MainApp main) {
        mainApp = main;
        cell = this;
        configureGrid();
        configureIcon();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 10, 0, 10));
    }

    private void configureIcon() {
        iconEdit.setFont(Font.font(FONT_AWESOME, FontWeight.BOLD, 16));
        buttonEdit.setPrefSize(35, 30);
        buttonEdit.setGraphic(iconEdit);
        buttonEdit.setTooltip(new Tooltip("View"));
        buttonEdit.setOnAction(e -> {
            needToHidePopOver();
            showPopOver(buttonEdit);
        });

        iconDel.setFont(Font.font(FONT_AWESOME, FontWeight.BOLD, 16));
        buttonDel.setPrefSize(35, 30);
        buttonDel.setGraphic(iconDel);
        buttonDel.setTooltip(new Tooltip("Delete"));
        buttonDel.setOnAction(e -> {
            mainApp.getDataMap().get("MIInputData").getList().remove(data);
        });
    }

    public void needToHidePopOver() {
        if (popOver != null && !popOver.isDetached()) {
            popOver.hide();
        }
    }

    public void showPopOver(Node n) {
        Platform.runLater(() -> {
            try {
                if (popOver != null && popOver.isShowing()) {
                    popOver.hide(Duration.ZERO);
                }
                popOver = createPopOver();
                popOver.show(n);
            } catch (IOException ex) {
                Logger.getLogger(MIInputDataListCell.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void addControlsToGrid() {
        grid.add(buttonEdit, 0, 0);
        grid.add(buttonDel, 0, 1);
        grid.add(name, 1, 0);
        grid.add(detail, 1, 1);
        this.setOnMouseClicked(click -> {
            needToHidePopOver();
            if (click.getClickCount() == 1 && click.getButton() == MouseButton.SECONDARY) {
                showPopOver(cell);
            }
        });
    }

    @Override
    public void updateItem(MIInputData midata, boolean empty) {
        super.updateItem(midata, empty);
        if (empty) {
            clearContent();
        } else {
            addContent(midata);
        }
    }

    private void clearContent() {
        Platform.runLater(() -> {
            setText(null);
            setGraphic(null);
        });

    }

    private void addContent(MIInputData midata) {
        data = midata;
        Platform.runLater(() -> {
            setText(null);
            iconEdit.setIcon(FontAwesomeIcon.EDIT);
            iconEdit.setGlyphStyle(ResourceCSS.INFORMATION_STYLE);
            iconDel.setIcon(FontAwesomeIcon.TRASH);
            iconDel.setGlyphStyle(ResourceCSS.FAIL_STYLE);
            name.setText(midata.getTransaction().getProgram() + ": " + midata.getTransaction().getTransaction());
            String s = "";
            s = midata.getData().entrySet().stream().map((t) -> t.getKey() + "=" + t.getValue() + "&").reduce(s, String::concat);
            detail.setText(s);
            setGraphic(grid);
        });
    }

    private PopOver createPopOver() throws IOException {
        popOver = new PopOver();
        popOver.setDetachable(true);
        popOver.setTitle(getItem().getTransaction().toString());
        popOver.setDetached(false);
        popOver.setArrowSize(10);
        if (data != null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MIInputDataListCell.class.getResource("/com/kles/view/mi/MIInputDataPanelChange.fxml"));
            loader.setResources(mainApp.getResourceBundle());
            popOver.setContentNode(loader.load());
            MIInputDataPanelChangeController controller = loader.getController();
            controller.setData(data);
            controller.getIsClickedOK().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (newValue) {
                    popOver.hide(Duration.ONE);
                    cell.getListView().refresh();
                    controller.getIsClickedOK().set(false);
                }
            });
            return popOver;
        }
        return null;
    }
}
