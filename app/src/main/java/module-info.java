module com.easypencil {
    requires javafx.controls;
    requires javafx.graphics;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires java.prefs;
    opens com.easypencil to javafx.graphics;
    exports com.easypencil;
}
