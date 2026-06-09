module com.matchpaw {
    requires javafx.controls;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    opens com.matchpaw.model to com.fasterxml.jackson.databind;
    exports com.matchpaw;
}
