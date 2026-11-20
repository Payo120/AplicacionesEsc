package com.serviciosweb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(App.class.getResource("/com/serviciosweb/views/directorio-view.fxml"))
        );
        Parent raiz = loader.load();

        Scene escena = new Scene(raiz, 1080, 620);
        escena.getStylesheets().add(
                Objects.requireNonNull(App.class.getResource("/com/serviciosweb/styles/tema.css")).toExternalForm()
        );

        stage.setTitle("ServiciosWEB - Directorio de Usuarios");
        stage.setMinWidth(860);
        stage.setMinHeight(500);
        stage.setScene(escena);
        stage.centerOnScreen();
        stage.show();
    }
}
