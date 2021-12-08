import lombok.Data;

import java.util.List;

public class Verlet {

    private double currRx;
    private double currRy;
    private Double prevRx;
    private Double prevRy;
    private double currVx;
    private double currVy;
    private double mass;
    private SFM force;
    private Particle particle;
    private double t;

    public Verlet(Particle particle, SFM forceFunction) {
        this.particle = particle;
        this.currRx = particle.getX();
        this.currVx = particle.getVx();
        this.currRy = particle.getY();
        this.currVy = particle.getVy();
        this.mass = particle.getMass();
        this.force = forceFunction;
        this.prevRx = null;
        this.prevRy = null;
    }

    public State[] step(double t, final double dt, final List<Particle> neighbours) {
        var f = force.getForce(particle, neighbours);
        if (prevRx == null)
            prevRx = estimatePrevR(currRx, currVx, f[0], dt, mass);
        if (prevRy == null)
            prevRy = estimatePrevR(currRy, currVy, f[1], dt, mass);

        var state = new State[2];

        double nextRx = 2*currRx - prevRx + (dt*dt/mass)*f[0];
        double nextVx = (nextRx - prevRx)/(2*dt);
        t += dt;
        prevRx = currRx;
        currRx = nextRx;
        state[0] = new State(nextRx, nextVx, t);

        double nextRy = 2*currRy - prevRy + (dt*dt/mass)*f[1];
        double nextVy = (nextRy - prevRy)/(2*dt);
        t += dt;
        prevRy = currRy;
        currRy = nextRy;
        state[1] = new State(nextRy, nextVy, t);

        return state;
    }

    private double estimatePrevR(final double r0, final double v0, final double f0,
                                 final double dt, final double mass){
        return r0 - dt*v0 + (dt*dt/(2*mass))*f0;
    }

    @Data
    public static class State {

        private final double r;
        private final double v;
        private final double dt;

        public State(final double r, final double v, final double dt) {
            this.r = r;
            this.v = v;
            this.dt = dt;
        }
    }
}
