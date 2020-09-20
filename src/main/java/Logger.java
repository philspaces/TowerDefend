import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* multiples producers which from Arena class components will add items to this queue => no limits producers and numbers of items
 *   GUI will wait for new message arrive to consume, multiple consumers since multiples events happen need to log*/
//note: however im using Platform.runlater() to update GUI component, which make the number of consumers 0~1
public class Logger{
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(); //Log area is responsible for printing msgQueue
    private BlockingQueue<String> scoreQueue = new LinkedBlockingQueue<>();//label on tool bar is responsible for printing scoreQueue

    public Logger() {
        msgQueue = new LinkedBlockingQueue<>();
        scoreQueue = new LinkedBlockingQueue<>();
    }

    public void addNewMessage(String newMessage){
        try {
            msgQueue.put(newMessage);
        } catch (InterruptedException e) {
            System.out.println("Logger Error Occur!!!");
        }
    }
    public String takeMessage(){
        String message = null;
        try {
            message = msgQueue.take();
        } catch (InterruptedException e) {
            System.out.println("Logger Error Occur!!!");
        }
        return message;
    }

    public void addNewScore(String newScore ){
        try {
            scoreQueue.put(newScore);
        } catch (InterruptedException e) {
            System.out.println("Logger Error Occur!!!");
        }
    }

    public String takeScore(){
        String score = null;
        try {
            score = scoreQueue.take();
        } catch (InterruptedException e) {
            System.out.println("Logger Error Occur!!!");
        }
        return score;
    }
}
