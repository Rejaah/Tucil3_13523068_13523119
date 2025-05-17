module tucil3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;  // tambahan
    
    opens gui to javafx.fxml;
    
    exports backend.model;
    exports backend.algorithm;
    exports backend.exception;
    exports gui;
}