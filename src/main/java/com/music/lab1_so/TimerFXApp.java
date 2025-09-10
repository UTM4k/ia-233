package com.music.lab1_so;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TimerFXApp extends Application {

    // Таймер для периодической задачи
    private Timer periodicTimer;
    private TextArea logTextArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Менеджер Таймеров (JavaFX)");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // --- Секция 1: Таймер с задержкой ---
        TitledPane delayPane = createDelayPane();

        // --- Секция 2: Задача по расписанию ---
        TitledPane specificTimePane = createSpecificTimePane();

        // --- Секция 3: Периодический таймер ---
        TitledPane periodicPane = createPeriodicPane();

        // --- Секция 4: Лог ---
        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setPrefHeight(300);

        root.getChildren().addAll(delayPane, specificTimePane, periodicPane, logTextArea);
        Scene scene = new Scene(root, 700, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Секция 1: Таймер, срабатывающий через указанную задержку.
     */
    private TitledPane createDelayPane() {
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10));

        Label label = new Label("Сработать через (сек):");
        Spinner<Integer> delaySpinner = new Spinner<>(1, 3600, 5);
        Button startButton = new Button("Запустить");

        startButton.setOnAction(event -> {
            int delaySeconds = delaySpinner.getValue();
            logMessage("ℹ️ [УВЕДОМЛЕНИЕ] Запланировано через " + delaySeconds + " сек.");

            Timer delayTimer = new Timer();
            delayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    logMessage("🔔 [УВЕДОМЛЕНИЕ] Пора сделать перерыв! ☕");
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Уведомление");
                        alert.setHeaderText(null);
                        alert.setContentText("Пора сделать перерыв на чай! ☕");
                        alert.showAndWait();
                    });
                    delayTimer.cancel();
                }
            }, delaySeconds * 1000L);
        });

        content.getChildren().addAll(label, delaySpinner, startButton);
        return new TitledPane("1. Одноразовое уведомление", content);
    }



    /**
     * Секция 2: Таймер, срабатывающий в определенное время.
     */
    private TitledPane createSpecificTimePane() {
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10));

        Label hourLabel = new Label("Время (ЧЧ:ММ):");
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, Calendar.getInstance().get(Calendar.MINUTE) + 1);
        hourSpinner.setPrefWidth(60);
        minuteSpinner.setPrefWidth(60);

        Button scheduleButton = new Button("Запланировать");

        scheduleButton.setOnAction(event -> {
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTime().before(new Date())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            Date scheduledTime = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'в' HH:mm:ss");
            logMessage("⏰ [ЗАДАЧА] Запланирована на " + sdf.format(scheduledTime));

            Timer specificTimer = new Timer();
            specificTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    logMessage("✅ [ЗАДАЧА] Время пришло! Выполнение задачи по расписанию.");

                    createBackupFile();
                    specificTimer.cancel();
                }
            }, scheduledTime);
        });

        content.getChildren().addAll(hourLabel, hourSpinner, minuteSpinner, scheduleButton);
        return new TitledPane("2. Задача по расписанию", content);
    }

    /**
     * Секция 3: Таймер, срабатывающий периодически.
     */
    private TitledPane createPeriodicPane() {
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10));

        Label initialLabel = new Label("Начать через (сек):");
        Spinner<Integer> initialDelaySpinner = new Spinner<>(0, 3600, 2);

        Label intervalLabel = new Label("Повторять каждые (сек):");
        Spinner<Integer> intervalSpinner = new Spinner<>(1, 3600, 3);

        Button startButton = new Button("Начать мониторинг");
        Button stopButton = new Button("Остановить");
        stopButton.setDisable(true);

        Circle statusIndicator = new Circle(8, Color.GRAY);

        startButton.setOnAction(event -> {
            int initialDelay = initialDelaySpinner.getValue();
            int interval = intervalSpinner.getValue();
            logMessage("🔄 [МОНИТОРИНГ] Запущен. Начнётся через " + initialDelay + " сек, повтор каждые " + interval + " сек.");

            periodicTimer = new Timer();
            periodicTimer.scheduleAtFixedRate(new TimerTask() {
                private final Random random = new Random();
                @Override
                public void run() {
                    String status = random.nextInt(100) < 80 ? "OK" : "ОШИБКА";
                    logMessage("   ➡️ [МОНИТОРИНГ] Статус сервера: " + status);

                    // НОВОЕ ДЕЙСТВИЕ: Изменить цвет индикатора
                    Platform.runLater(() -> {
                        if ("OK".equals(status)) {
                            statusIndicator.setFill(Color.LIMEGREEN);
                        } else {
                            statusIndicator.setFill(Color.TOMATO);
                        }
                    });
                }
            }, initialDelay * 1000L, interval * 1000L);

            startButton.setDisable(true);
            stopButton.setDisable(false);
        });

        stopButton.setOnAction(event -> {
            if (periodicTimer != null) {
                periodicTimer.cancel();
                logMessage("⏹️ [МОНИТОРИНГ] Остановлен пользователем.");
                startButton.setDisable(false);
                stopButton.setDisable(true);
                // Сброс цвета индикатора на нейтральный
                Platform.runLater(() -> statusIndicator.setFill(Color.GRAY));
            }
        });

        content.getChildren().addAll(initialLabel, initialDelaySpinner, intervalLabel, intervalSpinner, startButton, stopButton, statusIndicator);
        return new TitledPane("3. Периодическая проверка статуса", content);
    }

    /**
     * Создает пустой файл в домашней директории пользователя.
     */
    private void createBackupFile() {
        try {
            String homeDir = System.getProperty("user.home");
            String fileName = "backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
            File backupFile = new File(homeDir, fileName);

            if (backupFile.createNewFile()) {
                logMessage("   ➡️ Создан файл резервной копии: " + backupFile.getAbsolutePath());
            } else {
                logMessage("   ⚠️ Не удалось создать файл резервной копии.");
            }
        } catch (IOException e) {
            logMessage("   ❌ ОШИБКА при создании файла: " + e.getMessage());
        }
    }

    private void logMessage(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(String.format("[%tT] %s\n", new Date(), message));
        });
    }

    @Override
    public void stop() {
        if (periodicTimer != null) {
            periodicTimer.cancel();
            System.out.println("Периодический таймер остановлен при выходе из приложения.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
