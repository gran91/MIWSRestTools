package com.kles;

import com.kles.mi.MIInputData;
import com.kles.model.MIWS;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.kles.view.mi.MIRestTestToolsController;
import java.util.prefs.Preferences;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import resources.ResourceApp;

public class MIRestTools extends MainApp {

    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        prefs = Preferences.userRoot().node("3KLES_" + resources.ResourceApp.TITLE);
        initPrefs();
        initApp();
        resourceBundle = ResourceBundle.getBundle("resources/miresttools", Locale.getDefault());
        addToDataMap("MIWS", FXCollections.observableArrayList(), MIWS.class);
        addToDataMap("MIInputData", FXCollections.observableArrayList(), MIInputData.class);
        loadView();
    }

    @Override
    public void loadView() {
        super.title.unbind();
        super.title.bind(Bindings.concat(ResourceApp.TITLE).concat("\t").concat(super.clock.getTimeText()));
        this.primaryStage.titleProperty().unbind();
        this.primaryStage.titleProperty().bind(title);
        showMIRestTools();
        primaryStage.show();
    }

    public void showMIRestTools() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resourceBundle);
            loader.setLocation(MIRestTools.class.getResource("/com/kles/view/mi/MIRestTestTools.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            MIRestTestToolsController controller = loader.getController();
            controller.setMainApp(this);
            controller.setStage(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    static {
        loadLanguages();
    }
}
