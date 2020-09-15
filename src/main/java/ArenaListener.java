/**
 * Represents an event handler for when the arena is clicked.
 */
public interface ArenaListener
{
    void reloadBullet(double x, double y);
    void hitRobot(int id);
    void missBullet(double x, double y);
    void spawnRobot(int robotID);
    void getCurrentScore(double currentScore);
    void gameOver(double finalScore);
}
