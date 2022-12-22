import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Turnstile {


    // Coordinates indicate the top left of the turnstile
    double x;
    double y;
    double transactionTime;
    double lockedTime;
    double length;
    double width; // "Door width"
    boolean locked;
    List<Particle> targeted;
    Particle particleIn;

    public Turnstile(double x, double y, double length, double width, double transactionTime) {
        this.x = x;
        this.y = y;
        this.transactionTime = transactionTime;
        this.length = length;
        this.width = width;
        this.targeted = new LinkedList<>();
        this.locked = false;
    }

    public void lockTurnstile(final Particle particle, final double lockTime){
        locked = true;
        particleIn = particle;
        lockedTime = lockTime;
        var oldTarget = particle.getTarget();
        particle.setTarget(new double[]{oldTarget[0], 0});
        particle.setLocked(true);
    }

    public void tryUnlock(final double t) {
        if (t-lockedTime > transactionTime){
            locked = false;
        }
    }
}
