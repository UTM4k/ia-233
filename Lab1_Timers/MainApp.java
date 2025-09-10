import javax.swing.*;
import java.awt.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::createGUI);
    }

    private static void createGUI() {
        JFrame frame = new JFrame("Timers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 520);
        frame.setLayout(new GridLayout(3, 1, 6, 6));
        frame.setLocationRelativeTo(null);

        frame.add(new IntervalTimerPanel());
        frame.add(new ReportingTimerPanel());
        frame.add(new EverySecondTimerPanel());

        frame.setVisible(true);
    }
}