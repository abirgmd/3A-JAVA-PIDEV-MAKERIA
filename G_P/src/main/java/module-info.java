module com.abircode.cruddp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens com.abircode.cruddp.Controller to javafx.fxml;
    exports com.abircode.cruddp.Controller;
    exports com.abircode.cruddp.entities;
    exports com.abircode.cruddp.utils;
    exports com.abircode.cruddp;

}