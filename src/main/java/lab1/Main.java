package lab1;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private Timer timer;

    private TextField delayField;
    private TextField intervalField;
    private TextField timeField;
    private Text coloredText;

    private final Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.PURPLE};
    private int colorIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        delayField = new TextField("3000");
        intervalField = new TextField("500");
        timeField = new TextField("18:00:00");

        Button startButton = new Button("Запустить");
        coloredText = new Text("Внезапный текст!");
        coloredText.setFont(Font.font("Arial", 40));
        coloredText.setVisible(false); // Изначально текст скрыт

        // --- Разметка UI ---
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.add(new Label("Задержка (мс):"), 0, 0);
        inputGrid.add(delayField, 1, 0);
        inputGrid.add(new Label("Интервал (мс):"), 0, 1);
        inputGrid.add(intervalField, 1, 1);
        inputGrid.add(new Label("Время исчезновения (чч:мм:сс):"), 0, 2);
        inputGrid.add(timeField, 1, 2);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(inputGrid, startButton, coloredText);

        // --- Обработка нажатия кнопки ---
        startButton.setOnAction(e -> {
            // Отменяем предыдущий таймер, если он был
            if (timer != null) {
                timer.cancel();
            }

            try {
                // Получаем и парсим значения из полей ввода
            	String[] timeParts = timeField.getText().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                int second = Integer.parseInt(timeParts[2]);

                // Создаем объект Calendar с текущей датой
                Calendar calendar = Calendar.getInstance();
                
                // Устанавливаем время, взятое из поля ввода
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);
                calendar.set(Calendar.MILLISECOND, 0);

                Date specificTime = calendar.getTime();
                long delay = Long.parseLong(delayField.getText());
                long interval = Long.parseLong(intervalField.getText());

                timer = new Timer(true); // Запускаем как демон-поток, чтобы он не мешал завершению
                                         // программы, если окно будет закрыто.

                // 1. Задача: показать текст после задержки
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            coloredText.setVisible(true);
                        });
                    }
                }, delay);

                // 2. Задача: менять цвет текста по интервалу
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            // Циклично меняем цвет
                            coloredText.setFill(colors[colorIndex]);
                            colorIndex = (colorIndex + 1) % colors.length;
                        });
                    }
                }, delay, interval);

                System.out.println(specificTime);
                
                // 3. Задача: скрыть текст в указанное время
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            coloredText.setVisible(false);
                            timer.cancel(); // Останавливаем таймер
                        });
                    }
                }, specificTime);

            } catch (NumberFormatException ex) {
                System.err.println("Ошибка: Задержка и интервал должны быть числами.");
            }
        });

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Таймер");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public static void main(String[] args) {
    	System.out.println("Actual time: " + LocalDateTime.now());
        launch(args);
    }
}