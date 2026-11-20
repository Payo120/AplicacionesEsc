package com.serviciosweb.controller;

import com.serviciosweb.model.PerfilUsuario;
import com.serviciosweb.service.ApiUsuariosClient;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

public class DirectorioController {

    private final ApiUsuariosClient clienteApi = new ApiUsuariosClient();
    private final ObservableList<PerfilUsuario> registrosBase = FXCollections.observableArrayList();
    private FilteredList<PerfilUsuario> registrosFiltrados;

    @FXML
    private Label lblEstadoBadge;

    @FXML
    private TextField txtFiltro;

    @FXML
    private ProgressIndicator indicadorCarga;

    @FXML
    private Button btnCargar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<PerfilUsuario> tablaDirectorio;

    @FXML
    private TableColumn<PerfilUsuario, Integer> colId;

    @FXML
    private TableColumn<PerfilUsuario, String> colNombre;

    @FXML
    private TableColumn<PerfilUsuario, String> colUsuario;

    @FXML
    private TableColumn<PerfilUsuario, String> colCorreo;

    @FXML
    private TableColumn<PerfilUsuario, String> colTelefono;

    @FXML
    private TableColumn<PerfilUsuario, String> colCiudad;

    @FXML
    private TableColumn<PerfilUsuario, String> colOrganizacion;

    @FXML
    private Label lblConteo;

    @FXML
    private Label lblDetalle;

    @FXML
    public void initialize() {
        indicadorCarga.managedProperty().bind(indicadorCarga.visibleProperty());
        indicadorCarga.setVisible(false);

        configurarColumnas();
        configurarFiltrado();
        configurarSeleccion();

        actualizarEstado("Listo", "badge-idle");
        actualizarConteo();
        lblDetalle.setText("Origen: " + clienteApi.getEndpoint());
    }

    @FXML
    private void onCargarRegistros() {
        Task<List<PerfilUsuario>> tarea = new Task<>() {
            @Override
            protected List<PerfilUsuario> call() throws Exception {
                return clienteApi.cargarDirectorio();
            }
        };

        tarea.setOnRunning(event -> {
            alternarCarga(true);
            actualizarEstado("Consultando...", "badge-loading");
            lblDetalle.setText("Solicitando datos desde " + clienteApi.getEndpoint());
        });

        tarea.setOnSucceeded(event -> {
            alternarCarga(false);
            registrosBase.setAll(tarea.getValue());
            actualizarEstado("Sincronizado", "badge-ok");
            actualizarConteo();
            lblDetalle.setText("Origen: " + clienteApi.getEndpoint());
        });

        tarea.setOnFailed(event -> {
            alternarCarga(false);
            Throwable error = tarea.getException();
            String mensaje = error != null && error.getMessage() != null
                    ? error.getMessage()
                    : "No se pudo completar la solicitud";
            actualizarEstado("Error de red", "badge-error");
            lblDetalle.setText("Error: " + mensaje);
        });

        Thread hilo = new Thread(tarea, "directorio-api-worker");
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onLimpiarTabla() {
        txtFiltro.clear();
        registrosBase.clear();
        tablaDirectorio.getSelectionModel().clearSelection();
        actualizarEstado("Listo", "badge-idle");
        actualizarConteo();
        lblDetalle.setText("Origen: " + clienteApi.getEndpoint());
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().id()));
        colNombre.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().nombreCompleto()));
        colUsuario.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().usuario()));
        colCorreo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().correo()));
        colTelefono.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().telefono()));
        colCiudad.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().ciudad()));
        colOrganizacion.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().organizacion()));

        tablaDirectorio.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void configurarFiltrado() {
        registrosFiltrados = new FilteredList<>(registrosBase, item -> true);

        txtFiltro.textProperty().addListener((obs, anterior, nuevo) -> {
            registrosFiltrados.setPredicate(usuario -> usuario.coincideCon(nuevo));
            actualizarConteo();
        });

        registrosFiltrados.addListener((ListChangeListener<PerfilUsuario>) cambio -> actualizarConteo());

        SortedList<PerfilUsuario> registrosOrdenados = new SortedList<>(registrosFiltrados);
        registrosOrdenados.comparatorProperty().bind(tablaDirectorio.comparatorProperty());
        tablaDirectorio.setItems(registrosOrdenados);
    }

    private void configurarSeleccion() {
        tablaDirectorio.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual == null) {
                lblDetalle.setText("Origen: " + clienteApi.getEndpoint());
                return;
            }
            String web = actual.sitioWeb().isEmpty() ? "N/D" : actual.sitioWeb();
            lblDetalle.setText(actual.nombreCompleto() + " (@" + actual.usuario() + ")  |  Web: " + web);
        });
    }

    private void alternarCarga(boolean cargando) {
        indicadorCarga.setVisible(cargando);
        btnCargar.setDisable(cargando);
        btnLimpiar.setDisable(cargando);
    }

    private void actualizarEstado(String texto, String claseEstilo) {
        lblEstadoBadge.setText(texto);
        lblEstadoBadge.getStyleClass().removeAll("badge-idle", "badge-loading", "badge-ok", "badge-error");
        lblEstadoBadge.getStyleClass().add(claseEstilo);
    }

    private void actualizarConteo() {
        int total = registrosBase.size();
        int visibles = registrosFiltrados != null ? registrosFiltrados.size() : 0;

        if (total == 0) {
            lblConteo.setText("Sin registros cargados");
        } else if (visibles == total) {
            lblConteo.setText(total + " registros cargados");
        } else {
            lblConteo.setText("Mostrando " + visibles + " de " + total + " registros");
        }
    }
}
