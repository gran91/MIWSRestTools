package com.kles.view.mi;

import com.kles.MainApp;
import com.kles.mi.MIRecord;
import com.kles.utils.MIUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import resources.ResourceCSS;

public class MIRecordListCell extends ListCell<MIRecord> {

    private static final String FONT_AWESOME = "FontAwesome";

    private final GridPane grid = new GridPane();
    private final Button buttonDel = new Button();
    private final Button buttonEdit = new Button();
    private final FontAwesomeIconView iconEdit = new FontAwesomeIconView();
    private final FontAwesomeIconView iconDel = new FontAwesomeIconView();
    private final Label name = new Label();
    private final Label detail = new Label();
//    private MIRecord data;
    private ObservableList<MIRecord> listData = FXCollections.observableArrayList();
    private PopOver popOver;
    private final MIRecordListCell cell;
    private boolean draggable = false;
    private static final DataFormat MIRecordDataFormat = new DataFormat("com.kles.mi.MIRecord");
    private MainApp mainApp;

    public MIRecordListCell(MainApp main) {
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
            mainApp.getDataMap().get("MIInputData").getList().remove(getItem());
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
                Logger.getLogger(MIRecordListCell.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void addControlsToGrid() {
        grid.add(name, 0, 0);
        grid.add(detail, 1, 0);
//        grid.add(buttonEdit, 0, 0);
//        grid.add(buttonDel, 0, 1);
//        grid.add(name, 1, 0);
//        grid.add(detail, 1, 1);
        this.setOnMouseClicked(click -> {
            needToHidePopOver();
            if (click.getClickCount() == 2 && click.getButton() == MouseButton.PRIMARY) {
//                showPopOver(cell);
                showPopOver(this);
            }
        });
    }

    public void dragAndDrop() {
        setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            ObservableList<MIRecord> items = getListView().getItems();

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(MIRecordDataFormat, getItem());
//            dragboard.setDragView();
            dragboard.setContent(content);

            event.consume();
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != cell
                    && event.getDragboard().hasContent(MIRecordDataFormat)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != cell
                    && event.getDragboard().hasContent(MIRecordDataFormat)) {
                setOpacity(0.3);
            }
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != cell
                    && event.getDragboard().hasContent(MIRecordDataFormat)) {
                setOpacity(1);
            }
        });

        setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasContent(MIRecordDataFormat)) {
                ObservableList<MIRecord> items = getListView().getItems();
                int draggedIdx = MIRecordIndexOf(items, (MIRecord) db.getContent(MIRecordDataFormat));
                int thisIdx = items.indexOf(getItem());

                //MIRecord temp = listData.get(draggedIdx);
                //listData.set(draggedIdx, listData.get(thisIdx));
                //listData.set(thisIdx, temp);
                items.set(draggedIdx, getItem());
                items.set(thisIdx, (MIRecord) db.getContent(MIRecordDataFormat));

                List<MIRecord> itemscopy = new ArrayList<>(getListView().getItems());
                getListView().getItems().setAll(itemscopy);

                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        setOnDragDone(DragEvent::consume);
    }

    public static int MIRecordIndexOf(ObservableList<MIRecord> list, MIRecord r) {
        String recordValue = r.getNameValue().get(0).getValue().trim();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNameValue().get(0).getValue().trim().equals(recordValue)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void updateItem(MIRecord mirecord, boolean empty) {
        super.updateItem(mirecord, empty);
        if (empty) {
            clearContent();
        } else {
            addContent(mirecord);
        }
    }

    private void clearContent() {
        Platform.runLater(() -> {
            setText(null);
            setGraphic(null);
        });

    }

    private void addContent(MIRecord midata) {
        setItem(midata);
        Platform.runLater(() -> {
            setText(null);
            iconEdit.setIcon(FontAwesomeIcon.EDIT);
            iconEdit.setGlyphStyle(ResourceCSS.INFORMATION_STYLE);
            iconDel.setIcon(FontAwesomeIcon.TRASH);
            iconDel.setGlyphStyle(ResourceCSS.FAIL_STYLE);
            name.setText(midata.getNameValue().get(0).getValue()
                    + " (" + midata.getNameValue().get(1).getValue() + " " + midata.getNameValue().get(2).getValue().trim() + "," + midata.getNameValue().get(3).getValue().trim() + ")");
            detail.setText(midata.getNameValue().get(4).getValue());
            setGraphic(grid);
        });
    }

    private PopOver createPopOver() throws IOException {
        popOver = new PopOver();
        popOver.setDetachable(true);
        popOver.setDetached(false);
        popOver.setArrowSize(10);
        popOver.setContentNode(MIUtils.buildOutputPanel(this.getItem(), MIUtils.getMetadataMIField()));
//        if (getItem() != null) {
//            FXMLLoader loader = new FXMLLoader();
//            loader
//                    .setLocation(M3UpgraderApp.class
//                            .getResource("/com/kles/view/mi/MIInputDataPanelChange.fxml"));
//            popOver.setContentNode(loader.load());
//            MIInputDataPanelChangeController controller = loader.getController();
//            controller.setData(data);
//            controller.getIsClickedOK().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                if (newValue) {
//                    popOver.hide(Duration.ONE);
//                    cell.getListView().refresh();
//                    controller.getIsClickedOK().set(false);
//                }
//            });
        return popOver;
//        }
//        return null;
    }

    public MIRecord getData() {
        return getItem();
    }

    public void setData(MIRecord data) {
        setItem(data);
    }

    public ObservableList<MIRecord> getListData() {
        return listData;
    }

    public void setListData(ObservableList<MIRecord> listData) {
        this.listData = listData;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        if (draggable) {
            dragAndDrop();
        }
    }

}
