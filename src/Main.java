import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.TimerTask;
import java.util.Timer;
import java.awt.*;

import java.util.List;
import com.google.gson.Gson;
import java.io.FileReader;

class Sound extends TimerTask {
    JButton stop;

    public Sound(JButton stop){
        this.stop = stop;
    }



    @Override
    public void run(){
        if (!stop.isEnabled()) stop.setEnabled(true);
        Toolkit.getDefaultToolkit().beep();
        if (stop.isSelected()) cancel();
    }
}

class Typing extends TimerTask {
    JTextArea inputText;
    JButton finishButton;
    JLabel label;
    Timer timer;
    long start;

    public Typing(JTextArea inputText, JButton finishButton, JLabel label) {
        this.inputText = inputText;
        this.finishButton = finishButton;
        this.label = label;
    }
    @Override
    public void run(){
        start = System.currentTimeMillis();
        inputText.setEditable(true);
        Main.timer_running = true;

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                inputText.setEditable(false);
                finishButton.setEnabled(false);
                Main.finished = true;

                String text = inputText.getText();
                stats(text, 30);
                Main.timer_running = false;
                timer.cancel();
            }
        }, 10000);
    }
    public void stats(String inputText, int duration) {
        String[] words = inputText.trim().split("\\s+");
        int wordCount = inputText.trim().isEmpty() ? 0 : words.length;
        double wps = (double) wordCount / duration;
        label.setText("<html>Time is up!<br>Your typing speed is " + wps + " words/sec</html>");
    }
}

class AnswerOptions extends JPanel {
    private JRadioButton[] options = new JRadioButton[4];
    private ButtonGroup group = new ButtonGroup();

    public AnswerOptions(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (int i=0; i<options.length; i++){
            options[i] = new JRadioButton();
            group.add(options[i]);
            add(options[i]);
        }
    }

    public void addVariant(List<String> arr){
        for (int i = 0; i < options.length; i++){
            options[i].setText(arr.get(i));
        }
        group.clearSelection();
    }

    public String getSelectedOption() {
        for (JRadioButton btn : options) {
            if (btn.isSelected()) {
                return btn.getText();
            }
        }
        return null;
    }
}

class QuizInfo{
    private String question;
    private List<String> options;
    private String answer;

    public String getQuestion() {return question;}
    public List<String> getOptions() {return options;}
    public String getAnswer(){return answer;}

    public static QuizInfo[] loadQuestions(String filename){
        try (FileReader reader = new FileReader(filename)){
            Gson gson = new Gson();
            return gson.fromJson(reader, QuizInfo[].class);
        }
        catch(Exception e){
            System.out.println("Error loading questions: " + e);
            return null;
        }
    }

    public void connectGui(AnswerOptions options, JTextArea question){
        question.setText(this.getQuestion());
        List<String> opts = this.getOptions();
        options.addVariant(opts);
    }
}

class Quiz extends TimerTask{
    QuizInfo[] info;
    JTextArea question_field;
    AnswerOptions options;
    int count, c = 0;
    String[] answers;

    public Quiz(QuizInfo[] info, JTextArea question_field, AnswerOptions options){
        this.info = info;
        this.question_field = question_field;
        this.options = options;
        this.answers = new String[info.length];
    }

    @Override
    public void run(){
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run(){
                if (c < info.length){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    timer.cancel();
                }
                c++;
            }
        }, 9000, 1000);

        if(count < info.length){
            if (count == 0) {
                Main.timer_running = true;
                question_field.setVisible(true);
                options.setVisible(true);
            } else {
                answers[count-1] = options.getSelectedOption();
                System.out.println(answers[count-1]);
            }
            info[count].connectGui(options, question_field);
            count++;
        } else {
            answers[count-1] = options.getSelectedOption();
            System.out.println(answers[count-1]);
            compare();
            timer.cancel();
            this.cancel();
            Main.timer_running = false;
        }
        c = 0;
    }

    private void compare() {
        int count = 0;
        question_field.setText("");
        for (int i = 0; i < info.length; i++) {
            if (info[i].getAnswer().equals(this.answers[i])) {
                question_field.append((i+1) + " : correct\n");
                count++;
            } else {question_field.append((i+1) + " : incorrect\n");}
        }
        question_field.append("Correct answers: " + count + "/" + info.length);
    }
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class Main {
    private final static Timer timer = new Timer();
    public static boolean timer_running = false;
    private static boolean window_opened = false;
    public static volatile boolean finished = false;

    public static void main(String[] args) {

        JFrame frame = new JFrame("Timers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 500);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Choose and start a timer!:");
        JButton examTimerButton = new JButton("Exam Timer");
        JButton typeTextButton = new JButton("Type Text on time");
        JButton QuizButton = new JButton("Quiz");

        frame.add(label);
        frame.add(examTimerButton);
        frame.add(typeTextButton);
        frame.add(QuizButton);

        //Кнопка экзамен-------------------------------------------------------
        examTimerButton.addActionListener(_ -> {
            if (window_opened) return;
            window_opened = true;
            JFrame exam_window = new JFrame("Start your exam!");
            exam_window.setSize(300, 300);
            exam_window.setLayout(new FlowLayout());

            JButton startButton = new JButton("Start");
            JButton stopButton = new JButton("Stop timer");
            stopButton.setEnabled(false);
            JLabel startLabel = new JLabel("Click the button to start your exam");

            Sound sound = new Sound(stopButton);

            startButton.addActionListener( _ -> {
                if (timer_running) return;
                timer_running = true;

                if (finished) return;
                startLabel.setText("The exam has started - you have 1 hour. Good luck!");
                timer.schedule(sound, 6000, 1000);
            });
            stopButton.addActionListener(_ -> {
                sound.cancel();
                timer_running = false;
                finished = true;
                stopButton.setEnabled(false);
            });

            exam_window.add(startLabel);
            exam_window.add(startButton);
            exam_window.add(stopButton);

            exam_window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            exam_window.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (timer_running) {
                        int a = JOptionPane.showConfirmDialog(exam_window, "The timer is running - clock is ticking!.\nAre you sure you want to exit?", "Exit timer", JOptionPane.OK_CANCEL_OPTION);
                        if (a == JOptionPane.OK_OPTION) {
                            sound.cancel();
                            exam_window.dispose();
                        }
                    } else {
                        exam_window.dispose();
                    }
                }
                public void windowClosed(WindowEvent e) {
                    timer_running = false;
                    window_opened = false;
                }
            });
            
            exam_window.setVisible(true);
        });

        //Кнопка перепечатать-------------------------------------------------------
        typeTextButton.addActionListener(_ -> {
            if (window_opened) return;
            window_opened = true;

            JFrame typing_window = new JFrame("Start your exam!");
            typing_window.setSize(500, 500);
            typing_window.setLayout(new FlowLayout(FlowLayout.LEFT));

            JLabel message = new JLabel("<html>Type the next text on time<br>You will have 30 seconds<br><br></html>");
            message.setHorizontalAlignment(SwingConstants.LEFT);
            JLabel lorem_ipsum = new JLabel("<html><body style='width: 300px'>&emsp;Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.<br></body></html>");
            JTextArea input = new JTextArea(10, 50);
            input.setEditable(false);
            input.setLineWrap(true);
            input.setWrapStyleWord(true);
            JButton finishButton = new JButton("Finish");
            JButton startButton = new JButton("Start typing!");

            Typing task = new Typing(input, finishButton, message);

            typing_window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            typing_window.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (timer_running) {
                        int a = JOptionPane.showConfirmDialog(typing_window, "The timer is running - clock is ticking!.\nAre you sure you want to exit?", "Exit timer", JOptionPane.OK_CANCEL_OPTION);
                        if (a == JOptionPane.OK_OPTION) {
                            task.cancel();
                            typing_window.dispose();
                        }
                    } else {
                        typing_window.dispose();
                    }
                }

                public void windowClosed(WindowEvent e) {
                    timer_running = false;
                    window_opened = false;
                }
            });

            startButton.addActionListener(_ -> {
                timer.schedule(task, 0);
                startButton.setEnabled(false);
            });
            finishButton.addActionListener(_ -> {
                if(!timer_running) return;

                finishButton.setEnabled(false);
                finished =  true;
                task.cancel();
                task.timer.cancel();

                input.setEditable(false);
                JOptionPane.showMessageDialog(null, "You have finished earlier!");
                timer_running = false;

                task.stats(task.inputText.getText(), (int) (System.currentTimeMillis() - task.start));
            });

            typing_window.add(message);
            typing_window.add(lorem_ipsum);
            typing_window.add(input);
            typing_window.add(startButton);
            typing_window.add(finishButton);

            typing_window.setVisible(true);
        });

        //Кнопка квиз-------------------------------------------------------
        QuizButton.addActionListener(_ -> {
            if (window_opened) return;
            window_opened = true;

            JFrame quiz_window = new JFrame("Join our Quiz!");
            quiz_window.setSize(500, 500);
            quiz_window.setLayout(new FlowLayout(FlowLayout.CENTER));

            //Заголовок окна
            JLabel quiz_label = new JLabel("<html>Welcome to our quiz!<br>" +
                    "For each question you have 14 seconds to answer.<br>" +
                    "(you will hear beep sounds for each question when the time is up)<html>");

            //Кнопка начала квиза
            JButton startQuizButton = new JButton("Start quiz");

            //Поле для вопросов
            JTextArea question_field = new JTextArea(5, 20);
            question_field.setEditable(false);
            question_field.setVisible(false);
            question_field.setLineWrap(true);
            question_field.setWrapStyleWord(true);

            //Варианты ответа - выбать 1
            AnswerOptions your_answer = new AnswerOptions();
            your_answer.setVisible(false);

            //Загрузка информации о квизе
            QuizInfo[] info = QuizInfo.loadQuestions("src/quiz.json");

            //Создание задачи
            Quiz quiz = new Quiz(info, question_field, your_answer);

            //Начало квиза
            startQuizButton.addActionListener(_ -> {
                if (quiz.info == null) {
                    System.out.println("No info found");
                    return;
                }

                timer.schedule(quiz,0, 14000);
                startQuizButton.setEnabled(false);
            });

            quiz_window.add(quiz_label);
            quiz_window.add(startQuizButton);
            quiz_window.add(question_field);
            quiz_window.add(your_answer);

            quiz_window.setVisible(true);

            quiz_window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            quiz_window.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (timer_running) {
                        int a = JOptionPane.showConfirmDialog(quiz_window, "The timer is running - clock is ticking!.\nAre you sure you want to exit?", "Exit timer", JOptionPane.OK_CANCEL_OPTION);
                        if (a == JOptionPane.OK_OPTION) {
                            quiz.cancel();
                            quiz_window.dispose();
                        }
                    } else {
                        quiz_window.dispose();
                    }
                }
                public void windowClosed(WindowEvent e) {
                    timer_running = false;
                    window_opened = false;
                }
            });
        });

        frame.setVisible(true);
    }
}

