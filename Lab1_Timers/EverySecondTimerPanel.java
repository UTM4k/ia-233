import javax.swing.*;
import java.awt.*;

public class EverySecondTimerPanel extends JPanel {
    private Timer timer;
    private int seconds = 0;
    private JLabel label;

    public EverySecondTimerPanel() {
        setBorder(BorderFactory.createTitledBorder("Ежесекундный таймер"));
        setLayout(new GridLayout(2, 1));

        label = new JLabel("Ожидание...", SwingConstants.CENTER);

        JButton start = new JButton("Start");
        JButton stop = new JButton("Stop");

        start.addActionListener(e -> startTimer());
        stop.addActionListener(e -> stopTimer());

        add(label);
        JPanel buttons = new JPanel();
        buttons.add(start);
        buttons.add(stop);
        add(buttons);
    }

    private void startTimer() {
        seconds = 0;
        if (timer != null) timer.stop();

        timer = new Timer(1000, e -> {
            seconds++;
            label.setText("Секунда: " + seconds);
            if (seconds % 5 == 0) {
                setBackground(Color.CYAN);
            } else {
                setBackground(Color.WHITE);
            }
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) timer.stop();
        label.setText("Остановлен");
    }
}