import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TimerApp {

    enum TimerType { STANDARD, INTERVAL, TARGET }

    interface TimerListener {
        default void onColorPulse() {}
        default void onIntervalComplete(int cycles) {}
        default void onTargetReached() {}
    }

    static class TimerModel {
        private final TimerType type;
        private int seconds;
        private int intervalMax = 0;
        private int intervalCycles = 0;
        private Integer targetHHmm = null;

        private volatile boolean running = false;
        private ScheduledFuture<?> future;

        private final ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        private TimerListener listener;


        private int colorPulsePeriodSec = 5;

        TimerModel(TimerType type) {
            this.type = type;
        }

        void setIntervalMax(int max) { this.intervalMax = Math.max(0, max); }
        void setTargetHHmm(int hhmm) { this.targetHHmm = hhmm; }
        void setListener(TimerListener l) { this.listener = l; }
        void setColorPulsePeriodSec(int p) { this.colorPulsePeriodSec = Math.max(1, p); }

        boolean isRunning() { return running; }
        int getIntervalCycles() { return intervalCycles; }
        TimerType getType() { return type; }

        void start(Runnable onTick) {
            if (running) return;
            running = true;


            if (type == TimerType.TARGET && targetHHmm != null) {
                seconds = computeSecondsUntil(targetHHmm);
            }


            future = scheduler.scheduleAtFixedRate(() -> {
                tick1s();
                SwingUtilities.invokeLater(onTick);
            }, 0, 1, TimeUnit.SECONDS);
        }

        void stop() {
            running = false;
            if (future != null) {
                future.cancel(false);
            }
        }

        void shutdown() {
            stop();
            scheduler.shutdownNow();
        }

        void reset() {
            stop();
            if (type == TimerType.TARGET && targetHHmm != null) {
                seconds = computeSecondsUntil(targetHHmm);
            } else {
                seconds = 0;
            }
            if (type == TimerType.INTERVAL) {
                intervalCycles = 0;
            }
        }

        void tick1s() {
            if (!running) return;

            switch (type) {
                case STANDARD: {
                    seconds++;
                    if (colorPulsePeriodSec > 0 && seconds % colorPulsePeriodSec == 0 && listener != null) {
                        SwingUtilities.invokeLater(listener::onColorPulse);
                    }
                    break;
                }
                case INTERVAL: {
                    seconds++;
                    boolean completed = false;
                    if (seconds > intervalMax) {
                        seconds = 0;
                        intervalCycles++;
                        completed = true;
                    }
                    if (completed && listener != null) {
                        final int cyclesNow = intervalCycles;
                        SwingUtilities.invokeLater(() -> listener.onIntervalComplete(cyclesNow));
                    }
                    break;
                }
                case TARGET: {
                    if (seconds > 0) {
                        seconds--;
                        if (seconds <= 0) {
                            running = false;
                            stop();
                            if (listener != null) {
                                SwingUtilities.invokeLater(listener::onTargetReached);
                            }
                        }
                    }
                    break;
                }
            }
        }

        String formatDisplay() {
            switch (type) {
                case STANDARD:
                    return "⏱ " + toHHMMSS(seconds);
                case INTERVAL:
                    return "🔁 " + toHHMMSS(seconds) + " / " + toHHMMSS(intervalMax)
                            + "   |   Циклы: " + intervalCycles;
                case TARGET:
                    return "🎯 Осталось: " + toHHMMSS(seconds);
                default:
                    return String.valueOf(seconds);
            }
        }

        private static String toHHMMSS(int totalSeconds) {
            if (totalSeconds < 0) totalSeconds = 0;
            int h = totalSeconds / 3600;
            int m = (totalSeconds % 3600) / 60;
            int s = totalSeconds % 60;
            if (h > 0)
                return String.format("%02d:%02d:%02d", h, m, s);
            else
                return String.format("%02d:%02d", m, s);
        }

        private static int computeSecondsUntil(int hhmm) {
            int hh = hhmm / 100;
            int mm = hhmm % 100;
            if (hh < 0 || hh > 23 || mm < 0 || mm > 59) return 0;
            LocalDate today = LocalDate.now();
            LocalTime target = LocalTime.of(hh, mm);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime targetDateTime = LocalDateTime.of(today, target);
            if (!targetDateTime.isAfter(now)) {
                targetDateTime = LocalDateTime.of(today.plusDays(1), target);
            }
            long diff = java.time.Duration.between(now, targetDateTime).getSeconds();
            return (int) Math.max(0, diff);
        }
    }

    // Панель одного таймера
    static class TimerPanel extends JPanel {
        private final TimerModel model;
        private final JLabel label;
        private final JLabel statusLabel;
        private final JButton startBtn, stopBtn, resetBtn, deleteBtn;

        private final Color baseColor;
        private final Color[] pulseColors = {
                new Color(33,150,243),
                new Color(76,175,80),
                new Color(244,67,54),
                new Color(255,152,0)
        };
        private int colorIndex = 0;

        TimerPanel(TimerModel model, Runnable onDelete) {
            this.model = model;

            setLayout(new BorderLayout(8, 8));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            label = new JLabel(model.formatDisplay());
            label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
            baseColor = label.getForeground();

            statusLabel = new JLabel(" ");
            statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
            statusLabel.setForeground(new Color(120, 120, 120));

            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(label);
            left.add(Box.createVerticalStrut(4));
            left.add(statusLabel);

            startBtn = new JButton("Старт");
            stopBtn = new JButton("Стоп");
            resetBtn = new JButton("Сброс");
            deleteBtn = new JButton("Удалить");

            startBtn.addActionListener(e -> {
                model.start(this::refresh);
                refresh();
            });

            stopBtn.addActionListener(e -> {
                model.stop();
                refresh();
            });

            resetBtn.addActionListener(e -> {
                model.reset();
                clearStatus();
                restoreBaseColor();
                refresh();
            });

            deleteBtn.addActionListener(e -> {
                model.shutdown();
                onDelete.run();
            });

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            right.add(startBtn);
            right.add(stopBtn);
            right.add(resetBtn);
            right.add(deleteBtn);

            add(left, BorderLayout.CENTER);
            add(right, BorderLayout.EAST);


            model.setListener(new TimerListener() {
                @Override public void onColorPulse() {
                    if (model.getType() == TimerType.STANDARD) {
                        label.setForeground(pulseColors[colorIndex]);
                        colorIndex = (colorIndex + 1) % pulseColors.length;
                        statusLabel.setText("Пульс цвета (каждые 5 сек)");
                    }
                }

                @Override public void onIntervalComplete(int cycles) {
                    if (model.getType() == TimerType.INTERVAL) {
                        Toolkit.getDefaultToolkit().beep();
                        statusLabel.setText("✔ Завершён отсчёт #" + cycles);
                        refresh(); // чтобы обновился текст с количеством циклов
                    }
                }

                @Override public void onTargetReached() {
                    if (model.getType() == TimerType.TARGET) {
                        Toolkit.getDefaultToolkit().beep();
                        label.setForeground(new Color(200, 0, 0));
                        statusLabel.setText("⏰ Время пришло!");
                        JOptionPane.showMessageDialog(
                                TimerPanel.this,
                                "Время пришло!",
                                "Готово",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        refresh();
                    }
                }
            });

            refresh();
        }

        private void clearStatus() {
            statusLabel.setText(" ");
        }

        private void restoreBaseColor() {
            label.setForeground(baseColor);
            colorIndex = 0;
        }

        void refresh() {
            label.setText(model.formatDisplay());
            boolean running = model.isRunning();
            startBtn.setEnabled(!running);
            stopBtn.setEnabled(running);
        }
    }


    static class TimerManager {
        private final List<TimerModel> models = new ArrayList<>();
        private final List<TimerPanel> panels = new ArrayList<>();
        private final JPanel listContainer;

        TimerManager(JPanel listContainer) {
            this.listContainer = listContainer;
        }

        void addTimer(TimerModel model) {
            TimerPanel[] holder = new TimerPanel[1];
            holder[0] = new TimerPanel(model, () -> removeTimer(model, holder[0]));
            models.add(model);
            panels.add(holder[0]);
            listContainer.add(holder[0]);
            listContainer.revalidate();
            listContainer.repaint();
        }

        private void removeTimer(TimerModel m, TimerPanel p) {
            m.shutdown();
            models.remove(m);
            panels.remove(p);
            listContainer.remove(p);
            listContainer.revalidate();
            listContainer.repaint();
        }

        void startAll() {
            for (int i = 0; i < models.size(); i++) {
                TimerModel model = models.get(i);
                TimerPanel panel = panels.get(i);
                model.start(panel::refresh);
            }
        }

        void stopAll() {
            for (TimerModel model : models) {
                model.stop();
            }
            for (TimerPanel panel : panels) {
                panel.refresh();
            }
        }
    }

    static class MainFrame extends JFrame {
        final TimerManager manager;

        private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Standard", "Interval", "Target (HHmm)"});
        private final JTextField intervalField = new JTextField(6);
        private final JTextField targetField = new JTextField(6);
        private final JButton addBtn = new JButton("Добавить таймер");
        private final JButton startAllBtn = new JButton("Старт все");
        private final JButton stopAllBtn = new JButton("Стоп все");

        MainFrame() {
            super("Многопоточные таймеры");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setSize(800, 600);
            setLocationRelativeTo(null);

            JPanel top = new JPanel();
            top.setBorder(new EmptyBorder(10, 10, 10, 10));
            top.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

            top.add(new JLabel("Тип:"));
            top.add(typeCombo);

            top.add(new JLabel("Interval (сек):"));
            top.add(intervalField);

            top.add(new JLabel("Target (HHmm):"));
            top.add(targetField);

            top.add(addBtn);
            top.add(startAllBtn);
            top.add(stopAllBtn);

            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(new EmptyBorder(10, 10, 10, 10));

            manager = new TimerManager(list);

            addBtn.addActionListener(e -> {
                String selected = (String) typeCombo.getSelectedItem();
                if (selected == null) return;

                switch (selected) {
                    case "Standard": {
                        TimerModel std = new TimerModel(TimerType.STANDARD);
                        std.reset();
                        manager.addTimer(std);
                        break;
                    }
                    case "Interval": {
                        Integer max = parsePositiveInt(intervalField.getText());
                        if (max == null) {
                            msg("Введите целое положительное число для Interval.");
                            return;
                        }
                        TimerModel iv = new TimerModel(TimerType.INTERVAL);
                        iv.setIntervalMax(max);
                        iv.reset();
                        manager.addTimer(iv);
                        break;
                    }
                    case "Target (HHmm)": {
                        Integer hhmm = parseHHmm(targetField.getText());
                        if (hhmm == null) {
                            msg("Введите время в формате HHmm (например 0930, 1745).");
                            return;
                        }
                        TimerModel tg = new TimerModel(TimerType.TARGET);
                        tg.setTargetHHmm(hhmm);
                        tg.reset();
                        manager.addTimer(tg);
                        break;
                    }
                }
            });

            startAllBtn.addActionListener(e -> manager.startAll());
            stopAllBtn.addActionListener(e -> manager.stopAll());

            setLayout(new BorderLayout());
            add(top, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
        }

        private static Integer parsePositiveInt(String s) {
            if (s == null || s.trim().isEmpty()) return null;
            try {
                int v = Integer.parseInt(s.trim());
                return v >= 0 ? v : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private static Integer parseHHmm(String s) {
            if (s == null || s.trim().isEmpty()) return null;
            String t = s.trim();
            if (!t.matches("\\d{4}")) return null;
            int hhmm = Integer.parseInt(t);
            int hh = hhmm / 100;
            int mm = hhmm % 100;
            if (hh < 0 || hh > 23 || mm < 0 || mm > 59) return null;
            return hhmm;
        }

        private static void msg(String text) {
            JOptionPane.showMessageDialog(null, text, "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}
