module com.example.quickcommercedeliverysystemdesktop {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // Allow FXML to reflectively access controllers
    opens com.example.quickcommercedeliverysystemdesktop to javafx.fxml;
    opens com.example.quickcommercedeliverysystemdesktop.controllers.auth to javafx.fxml;
    opens com.example.quickcommercedeliverysystemdesktop.controllers.dashboard to javafx.fxml;
    opens com.example.quickcommercedeliverysystemdesktop.controllers.dialogs to javafx.fxml;

    // (optional) open model package for table bindings
    opens com.example.quickcommercedeliverysystemdesktop.models to javafx.fxml;

    // Export public packages so JavaFX + other modules can use them
    exports com.example.quickcommercedeliverysystemdesktop;
    exports com.example.quickcommercedeliverysystemdesktop.controllers.auth;
    exports com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;
    exports com.example.quickcommercedeliverysystemdesktop.controllers.dialogs;
}
