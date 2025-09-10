module com.music.lab1_so {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.music.lab1_so to javafx.fxml;
    exports com.music.lab1_so;
}