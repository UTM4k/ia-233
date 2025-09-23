import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ReportingTimerPanel extends JPanel {
    private Timer timer;
    private int remaining;

    private final JLabel label;
    private final JTextField input;
    private final JButton startBtn;
    private final JButton stopBtn;

    public ReportingTimerPanel() {
        setBorder(BorderFactory.createTitledBorder("Отчётный таймер (countdown)"));
        setLayout(new BorderLayout(6, 6));
        setBackground(Color.WHITE);

        // Ввод секунд
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Старт (сек): "), BorderLayout.WEST);
        input = new JTextField("10");
        top.add(input, BorderLayout.CENTER);

        // Метка
        label = new JLabel("Ожидание...", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(14f));
        label.setOpaque(true);

        // Кнопки
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        JPanel btns = new JPanel();
        btns.add(startBtn);
        btns.add(stopBtn);

        add(top, BorderLayout.NORTH);
        add(label, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            try {
                startCountdown(Integer.parseInt(input.getText().trim()));
            } catch (NumberFormatException ex) {
                updateLabel("Введите корректное число", Color.YELLOW, Color.BLACK);
            }
        });
        stopBtn.addActionListener(e -> stopCountdown());
    }

    private void startCountdown(int seconds) {
        if (seconds <= 0) {
            updateLabel("Время вышло!", Color.PINK, Color.BLACK);
            return;
        }

        stopCountdown();
        remaining = seconds;
        updateLabel("Старт: " + seconds + " сек.", Color.WHITE, Color.BLACK);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                remaining--;
                if (remaining > 0) {
                    updateLabel("Осталось: " + remaining + " сек.", Color.WHITE, Color.BLACK);
                } else {
                    updateLabel("Время вышло!", Color.PINK, Color.BLACK);
                    stopCountdown();
                }
            }
        }, 1000, 1000);

        setControlsEnabled(false);
    }

    private void stopCountdown() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        updateLabel("Остановлен", getBackground(), Color.BLACK);
        setControlsEnabled(true);
    }

    private void updateLabel(String text, Color bg, Color fg) {
        SwingUtilities.invokeLater(() -> {
            label.setText(text);
            label.setBackground(bg);
            label.setForeground(fg);
        });
    }

    private void setControlsEnabled(boolean enabled) {
        input.setEnabled(enabled);
        startBtn.setEnabled(enabled);
        stopBtn.setEnabled(!enabled);
    }
}