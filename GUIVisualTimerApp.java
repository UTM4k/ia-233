import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

/**
 * Класс GUIVisualTimerApp наследует JFrame, что делает его окном верхнего уровня.
 */
public class GUIVisualTimerApp extends JFrame {

    // метки для отображения статуса каждого таймера.
    private JLabel delayLabel;
    private JLabel specificTimeLabel;
    private JLabel periodicLabel;

    /**
     * Конструктор класса. Инициализирует окно, создает компоненты и запускает таймеры.
     */
    public GUIVisualTimerApp() {
        setTitle("Приложение с визуальными таймерами"); // Заголовок окна
        setSize(500, 300); // Размеры окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Операция при закрытии окна
        setLocationRelativeTo(null); // окно по центру экрана
        setLayout(new GridLayout(4, 1, 10, 10)); // GridLayout для компоновки

        // метки в окно
        JLabel titleLabel = new JLabel("<html><center><b>Статус таймеров</b></center></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20)); 
        add(titleLabel);

        //  метки для каждого таймера
        delayLabel = new JLabel("<html>1. Таймер задержки: Ожидание 5 секунд...</html>", SwingConstants.CENTER);
        specificTimeLabel = new JLabel("<html>2. Таймер по времени: Ожидание определенного времени...</html>", SwingConstants.CENTER);
        periodicLabel = new JLabel("<html>3. Периодический таймер: Запущен...</html>", SwingConstants.CENTER);
        
        Font labelFont = new Font("Arial", Font.PLAIN, 16);
        delayLabel.setFont(labelFont);
        specificTimeLabel.setFont(labelFont);
        periodicLabel.setFont(labelFont);

        // Добавила метки в окно
        add(delayLabel);
        add(specificTimeLabel);
        add(periodicLabel);

        // Запустила методы, которые настраивают и запускают таймеры
        scheduleDelayTimer();
        scheduleSpecificTimeTimer();
        schedulePeriodicTimer();
    }

    /**
     * Метод для настройки таймера, который срабатывает один раз после заданной задержки.
     * Реализует требование 1: "Реагирует по истечении определенного промежутка времени".
     */
    private void scheduleDelayTimer() {
        // javax.swing.Timer, который сработает через 5000 мс (5 секунд)
        Timer delayTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Изменение текста метки и окрашивание его в красный цвет с помощью HTML-разметки
                delayLabel.setText("<html><center>1. Таймер задержки: <font color='red'>Прошло 5 секунд! ✅</font></center></html>");
                // Останавливает таймер, так как он должен сработать только один раз
                ((Timer) e.getSource()).stop();
            }
        });
        delayTimer.setRepeats(false); // Устанавливаем таймер как одноразовый
        delayTimer.start(); // Запускаем таймер
    }

    /**
     * Метод для настройки таймера, который срабатывает в определенное время.
     * Реализует требование 2: "Реагирует в определенное время".
     */
    private void scheduleSpecificTimeTimer() {
        // Вычисляем время в будущем (например, через 20 секунд от текущего момента)
        Calendar futureTime = Calendar.getInstance();
        futureTime.add(Calendar.SECOND, 20);
        long delayMillis = futureTime.getTimeInMillis() - System.currentTimeMillis();

        // Проверяем, что время еще не прошло
        if (delayMillis > 0) {
            // Создаем таймер с вычисленной задержкой
            Timer specificTimeTimer = new Timer((int) delayMillis, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Форматируем текущее время
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    // Обновляем метку, выделяя время синим цветом
                    specificTimeLabel.setText("<html><center>2. Таймер по времени: <font color='blue'>Наступило время " + sdf.format(new Date()) + "! ✅</font></center></html>");
                    ((Timer) e.getSource()).stop(); // Останавливаем таймер после срабатывания
                }
            });
            specificTimeTimer.setRepeats(false); // Делаем его одноразовым
            specificTimeTimer.start(); // Запускаем таймер
        } else {
            // Если время уже в прошлом, выводим соответствующее сообщение
            specificTimeLabel.setText("<html><center>2. Таймер по времени: <font color='gray'>Время уже прошло! ❌</font></center></html>");
        }
    }

    /**
     * Метод для настройки периодического таймера.
     * Реализует требование 3: "Реагирует с указанным периодом".
     */
    private void schedulePeriodicTimer() {
        // Создаем таймер, который срабатывает каждые 3000 мс (3 секунды)
        Timer periodicTimer = new Timer(3000, new ActionListener() {
            private int counter = 0;
            // Массив цветов для визуального эффекта
            private String[] colors = {"red", "green", "blue", "purple", "orange"};

            @Override
            public void actionPerformed(ActionEvent e) {
                counter++; // Увеличиваем счетчик
                // Проверяем, если таймер сработал 5 раз, то останавливаем его
                if (counter > 5) {
                    ((Timer) e.getSource()).stop();
                    periodicLabel.setText("<html><center>3. Периодический таймер: <font color='black'>Завершен после 5 выполнений! ✅</font></center></html>");
                    return; // Выходим из метода
                }

                // Создаем строку с полным текстом для каждого обновления
                StringBuilder coloredText = new StringBuilder("<html><center>3. ");
                // Выбираем цвет из массива, чтобы он менялся на каждом шаге
                int colorIndex = (counter - 1) % colors.length;
                String fullText = "Периодический таймер: Выполнен " + counter + " раз(а)...";

                // Перебираем каждый символ в строке для применения форматирования
                for (char c : fullText.toCharArray()) {
                    // Проверяем, является ли символ гласной буквой
                    if ("АаОоУуИиЭэЫыЁёЕеЯяЮю".indexOf(c) != -1) {
                        // Если да, добавляем его с HTML-тегом цвета
                        coloredText.append("<font color='").append(colors[colorIndex]).append("'>").append(c).append("</font>");
                    } else {
                        // Иначе, добавляем символ без изменения
                        coloredText.append(c);
                    }
                }

                // Закрываем HTML-разметку
                coloredText.append("</center></html>");
                // Устанавливаем готовый текст в метку
                periodicLabel.setText(coloredText.toString());
            }
        });
        periodicTimer.start(); // Запускаем периодический таймер
    }

    /**
     * Главный метод приложения.
     * SwingUtilities.invokeLater гарантирует, что GUI-компоненты
     * будут созданы и запущены в потоке диспетчеризации событий (EDT),
     * что является обязательным для Swing-приложений.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GUIVisualTimerApp().setVisible(true);
        });
    }
}