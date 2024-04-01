import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Data
public class Turnstile {
    //Suppose I am waiting for a bus at a stop. And suppose that a bus usually arrives at the stop in every 10 mins.
    //Now I define λ to be the rate of arrival of a bus per minute. So, λ = (1/10).
    static final double lambda = 1.0/2;
    static final Random rand = new Random();

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

    public Turnstile(double x, double y, double length, double width) {
        this.x = x;
        this.y = y;
        this.length = length;
        this.width = width;
        this.targeted = new LinkedList<>();
        this.locked = false;
    }

    private double getNextTransactionTime() {
        return Math.log(1-rand.nextDouble())/(-lambda);
    }

    public void lockTurnstile(final Particle particle, final double lockTime){
        locked = true;
        particleIn = particle;
        lockedTime = lockTime;
        transactionTime = getNextTransactionTime();
        var oldTarget = particle.getTarget();
        particle.setTarget(new double[]{oldTarget[0], 0});
        particle.setLocked(true);
    }

    public void tryUnlock(final double t) {
        if (locked && t-lockedTime > transactionTime) {
            locked = false;
            particleIn.setLocked(false);
            particleIn.payed = true;
            particleIn = null;
        }
    }
}
