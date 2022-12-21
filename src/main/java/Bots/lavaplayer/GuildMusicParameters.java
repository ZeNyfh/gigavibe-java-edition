package Bots.lavaplayer;

public class GuildMusicParameters {
    public double pitch = 1d;
    public double depth = 1d;
    private double bass = 1d;
    private double speed = 1d;

    public double getBass() {
        return bass;
    }

    public void setBass(double bass) {
        this.bass = bass;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }
}
