module com.serviciosweb {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    opens com.serviciosweb to javafx.fxml;
    opens com.serviciosweb.controller to javafx.fxml;
    opens com.serviciosweb.model to javafx.base, com.fasterxml.jackson.databind;
    opens com.serviciosweb.service to com.fasterxml.jackson.databind;

    exports com.serviciosweb;
    exports com.serviciosweb.controller;
    exports com.serviciosweb.model;
    exports com.serviciosweb.service;
}
