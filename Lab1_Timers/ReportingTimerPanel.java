import javax.swing.*;
import java.awt.*;

public class ReportingTimerPanel extends JPanel {
    private Timer timer;
    private int remaining;
    private JLabel label;
    private JTextField input;

    public ReportingTimerPanel() {
        setBorder(BorderFactory.createTitledBorder("Отчётный таймер (countdown)"));
        setLayout(new GridLayout(3, 1));

        input = new JTextField("10");
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
            remaining = Integer.parseInt(input.getText());
        } catch (NumberFormatException e) {
            remaining = 10;
        }
        if (timer != null) timer.stop();

        label.setText("Старт: " + remaining);
        label.setForeground(Color.BLACK);

        timer = new Timer(1000, e -> {
            remaining--;
            label.setText("Осталось: " + remaining);
            if (remaining <= 0) {
                label.setText("Время вышло!");
                label.setForeground(Color.RED);
                timer.stop();
            }
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) timer.stop();
        label.setText("Остановлен");
    }
}