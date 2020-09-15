public class Bullet {
    private int positionX;
    private int positionY;
    private long startTime;
    private long endTime;

    public Bullet(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        startTime = System.currentTimeMillis();
    }

    public void resetTime(){
        startTime = System.currentTimeMillis();
    }
    public long stopTime(){
        endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }
}
