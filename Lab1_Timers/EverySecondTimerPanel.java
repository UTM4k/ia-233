import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class EverySecondTimerPanel extends JPanel {
    private Timer timer;
    private int seconds = 0;

    private final JLabel label;
    private final JButton startBtn;
    private final JButton stopBtn;

    public EverySecondTimerPanel() {
        setBorder(BorderFactory.createTitledBorder("Ежесекундный таймер"));
        setLayout(new BorderLayout(6,6));
        setBackground(Color.WHITE);

        label = new JLabel("Ожидание...", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(14f));
        label.setOpaque(true);

        JPanel btns = new JPanel();
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        btns.add(startBtn);
        btns.add(stopBtn);

        add(label, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> startTimer());
        stopBtn.addActionListener(e -> stopTimer());
    }

    private void startTimer() {
        stopTimer();

        seconds = 0;
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seconds++;
                SwingUtilities.invokeLater(() -> {
                    label.setText("Секунда: " + seconds);
                    if (seconds % 5 == 0) {
                        setBackground(Color.CYAN);
                        label.setBackground(Color.CYAN);
                    } else {
                        setBackground(Color.WHITE);
                        label.setBackground(Color.WHITE);
                    }
                });
            }
        }, 1000L, 1000L);

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
            setBackground(getBackground());
            label.setBackground(getBackground());
            startBtn.setEnabled(true);
            stopBtn.setEnabled(true);
        });
    }
}