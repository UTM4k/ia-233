/* import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
class SoundPlayer extends TimerTask {
    @Override
    public void run() {
        Toolkit.getDefaultToolkit().beep();
        System.out.println("Sound played");
    }
}
class Message extends TimerTask {
    String msg;
    public Message(String msg) {
        this.msg = msg;
    }
    @Override
    public void run() {
        System.out.println(msg);
    }
}
public class Main {
    public static void main(String[] args) {
        SoundPlayer soundPlayer = new SoundPlayer();
        Message message = new Message("Salut, Salut, Salut!!! ");
        Timer soundTimer = new Timer();
        Timer messageTimer = new Timer();
        soundTimer.scheduleAtFixedRate(soundPlayer, 0, 2000);
        messageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("5 seconds have passed");
                soundTimer.cancel();
            }
        }, 5000);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 24);
        calendar.set(Calendar.SECOND, 0);
        Date date = calendar.getTime();
        Timer clock = new Timer();
        clock.schedule(message, date);
        System.out.println("Start");
    }
}

 */