/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kles.view.mi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Callback;
import org.controlsfx.control.TaskProgressView;

/**
 * FXML Controller class
 *
 * @author jchau
 */
public class MIExportProgressViewController {

    @FXML
    private TaskProgressView<MIExportTask> taskProgressView;

    @FXML
    private ProgressIndicator progressError, progressSuccess, progressTotal;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private Callback<MIExportTask, Node> factory;

    private final ObservableList<MIExportTask> listTask = FXCollections.observableArrayList();

    private final ObservableList<Integer> listError = FXCollections.observableArrayList();
    private final ObservableList<Integer> listSuccess = FXCollections.observableArrayList();
    private double percent = 0d;
    private DoubleProperty errorValue = new SimpleDoubleProperty(0);
    private DoubleProperty sucessValue = new SimpleDoubleProperty(0);

    @FXML
    public void initialize() {
        progressError.progressProperty().bind(errorValue);
        progressSuccess.progressProperty().bind(sucessValue);
        progressTotal.progressProperty().bind(progressError.progressProperty().add(progressSuccess.progressProperty()));
    }
    
    

    public void run() {
        taskProgressView.getTasks().forEach((t) -> {
            t.stateProperty().addListener((ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
                switch (newValue) {
                    case FAILED:
                        listError.add(t.getId());
                        errorValue.set(errorValue.get() + percent);
                        break;
                    case SUCCEEDED:
                        listSuccess.add(t.getId());
                        sucessValue.set(sucessValue.get() + percent);
                        break;
                }
            });
            executorService.submit(t);
        });
    }

    public void setListTask(List<MIExportTask> list) {
        listTask.setAll(list);
        percent = 1d / list.size();
        taskProgressView.getTasks().setAll(listTask);
    }

}
