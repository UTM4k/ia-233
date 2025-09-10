import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ReportingTimerPanel extends JPanel {
    private Timer timer;   // java.util.Timer
    private int remaining; // оставшееся время в секундах

    private final JLabel label;
    private final JTextField input;
    private final JButton startBtn;
    private final JButton stopBtn;

    public ReportingTimerPanel() {
        setBorder(BorderFactory.createTitledBorder("Отчётный таймер (countdown)"));
        setLayout(new BorderLayout(6,6));
        setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Старт (сек): "), BorderLayout.WEST);
        input = new JTextField("10");
        top.add(input, BorderLayout.CENTER);

        label = new JLabel("Ожидание...", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(14f));
        label.setOpaque(true);

        JPanel btns = new JPanel();
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        btns.add(startBtn);
        btns.add(stopBtn);

        add(top, BorderLayout.NORTH);
        add(label, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            try {
                int s = Integer.parseInt(input.getText().trim());
                startCountdown(s);
            } catch (NumberFormatException ex) {
                label.setText("Введите корректное число");
            }
        });

        stopBtn.addActionListener(e -> stopCountdown());
    }

    private void startCountdown(int seconds) {
        if (seconds <= 0) {
            SwingUtilities.invokeLater(() -> {
                label.setText("Время вышло!");
                label.setBackground(Color.PINK);
                label.setForeground(Color.BLACK);
            });
            return;
        }

        stopCountdown(); 

        remaining = seconds;
        timer = new Timer();

        // Задача срабатывает раз в секунду
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                remaining--;
                SwingUtilities.invokeLater(() -> {
                    if (remaining > 0) {
                        label.setText("Осталось: " + remaining + " сек.");
                        label.setBackground(Color.WHITE);
                        label.setForeground(Color.BLACK);
                    } else {
                        label.setText("Время вышло!");
                        label.setBackground(Color.PINK);
                        label.setForeground(Color.BLACK);
                    }
                });

                if (remaining <= 0) {
                    timer.cancel();
                    timer = null;
                }
            }
        }, 1000L, 1000L);

        input.setEnabled(false);
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);

        SwingUtilities.invokeLater(() -> {
            label.setText("Старт: " + seconds + " сек.");
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
        });
    }

    private void stopCountdown() {
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
}