import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class IntervalTimerPanel extends JPanel {
    private Timer timer;
    private int interval = 3; 
    private int tick = 0;

    private final JLabel label;
    private final JTextField input;
    private final JButton startBtn;
    private final JButton stopBtn;

    public IntervalTimerPanel() {
        setBorder(BorderFactory.createTitledBorder("Интервальный таймер"));
        setLayout(new BorderLayout(6, 6));
        setBackground(Color.WHITE);

        // Верхняя панель с вводом интервала
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Интервал (сек): "), BorderLayout.WEST);
        input = new JTextField("3");
        top.add(input, BorderLayout.CENTER);

        // Метка для отображения состояния
        label = new JLabel("Ожидание...", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(14f));
        label.setOpaque(true);

        // Панель с кнопками
        JPanel btns = new JPanel();
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        btns.add(startBtn);
        btns.add(stopBtn);

        // Добавляем компоненты
        add(top, BorderLayout.NORTH);
        add(label, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        // Обработчики событий
        startBtn.addActionListener(e -> startTimer());
        stopBtn.addActionListener(e -> stopTimer());
    }

    private void startTimer() {
        // Парсинг и валидация ввода
        try {
            interval = Integer.parseInt(input.getText().trim());
            if (interval <= 0) interval = 3;
        } catch (NumberFormatException ex) {
            interval = 3;
        }

        stopTimer(); // останавливаем, если уже был запущен

        tick = 0;
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick++;
                // Обновляем GUI в EDT
                SwingUtilities.invokeLater(() -> updateLabel());
            }
        }, 0, interval * 1000L);

        input.setEnabled(false);
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        SwingUtilities.invokeLater(() -> {
            label.setText("Остановлен");
            label.setBackground(getBackground());
            label.setForeground(Color.BLACK);
            input.setEnabled(true);
            startBtn.setEnabled(true);
            stopBtn.setEnabled(true);
        });
    }

    private void updateLabel() {
        label.setText("Tick #" + tick + " (каждые " + interval + "с)");
        if (tick < 5) {
            label.setBackground(Color.GREEN);
            label.setForeground(Color.BLACK);
        } else if (tick < 10) {
            label.setBackground(Color.ORANGE);
            label.setForeground(Color.BLACK);
        } else {
            label.setBackground(Color.RED);
            label.setForeground(Color.WHITE);
        }
    }
}