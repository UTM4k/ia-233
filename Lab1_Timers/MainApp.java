import javax.swing.*;
import java.awt.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::createGUI);
    }

    private static void createGUI() {
        JFrame frame = new JFrame("Timers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLayout(new GridLayout(3, 1, 5, 5));

        frame.add(new IntervalTimerPanel());
        frame.add(new ReportingTimerPanel());
        frame.add(new EverySecondTimerPanel());

        frame.setVisible(true);
    }
}