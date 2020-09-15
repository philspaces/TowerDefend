import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger{
    BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();
    BlockingQueue<String> scoreQueue = new LinkedBlockingQueue<>();


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
