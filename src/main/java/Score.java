import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Score implements Runnable {
    double totalScore = 0.0;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);


    public Score() {
        this.totalScore = 0.0;
    }

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addScore(double plusScore) {
        double oldValue = totalScore;
        totalScore += plusScore;
        support.firePropertyChange("totalScore",oldValue,totalScore);
    }

    public double getTotalScore() {
        return Math.round(totalScore * 100.0) / 100.0;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(1000);
                addScore(10.0);
            }
        } catch (InterruptedException e) {
            System.out.println("Stop Counting Score");
        }
    }
}
