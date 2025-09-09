import javax.swing.*;
import java.awt.*;

public class IntervalTimerPanel extends JPanel {
    private Timer timer;
    private int interval;
    private int tick = 0;
    private JLabel label;
    private JTextField input;

    public IntervalTimerPanel() {
        setBorder(BorderFactory.createTitledBorder("Интервальный таймер"));
        setLayout(new GridLayout(3, 1));

        input = new JTextField("3");
        JButton start = new JButton("Start");
        JButton stop = new JButton("Stop");
        label = new JLabel("Ожидание...", SwingConstants.CENTER);

        start.addActionListener(e -> startTimer());
        stop.addActionListener(e -> stopTimer());

        add(input);
        add(label);
        JPanel buttons = new JPanel();
        buttons.add(start);
        buttons.add(stop);
        add(buttons);
    }

    private void startTimer() {
        try {
            interval = Integer.parseInt(input.getText());
        } catch (NumberFormatException e) {
            interval = 3;
        }
        tick = 0;
        if (timer != null) timer.stop();

        timer = new Timer(interval * 1000, e -> {
            tick++;
            label.setText("Tick #" + tick);
            if (tick < 5) label.setForeground(Color.GREEN);
            else if (tick < 10) label.setForeground(Color.ORANGE);
            else label.setForeground(Color.RED);
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) timer.stop();
        label.setText("Остановлен");
    }
}