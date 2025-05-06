module com.abircode.cruddp {
    requires javafx.controls;
    requires javafx.fxml;
    requires jbcrypt;
    requires com.google.zxing;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires java.mail;
    requires webcam.capture;
    requires java.desktop;
    requires java.prefs;
    requires spring.webflux;
    requires spring.web;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    opens com.abircode.cruddp.services to javafx.fxml;
    exports com.abircode.cruddp.services;
    exports com.abircode.cruddp.Controller.user to javafx.fxml;
    exports com.abircode.cruddp.Controller.user.admin to javafx.fxml;
    opens com.abircode.cruddp.Controller;
    opens com.abircode.cruddp.Controller.user;
    opens com.abircode.cruddp.Controller.user.admin;
    exports com.abircode.cruddp.entities;
    exports com.abircode.cruddp.utils;
    exports com.abircode.cruddp;
    requires java.net.http;
    requires twilio;
    requires javafx.web;
    requires google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client;
    requires google.api.services.oauth2.v2.rev157; // Add this line
    requires reactor.core;
    requires java.base;
    requires com.google.api.client.auth;
    requires com.google.api.services.calendar; // Add this line

    opens com.abircode.cruddp to gson;
}