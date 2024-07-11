import lombok.Data;

import java.util.List;
import java.util.Set;

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

    public State[] step(double t, final double dt, final Set<Particle> neighbours) {
        var f = force.getForce(particle, neighbours, t);
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

    public State[] fixedStep(double t, final double dt, final Set<Particle> neighbours) {
        var state = new State[2];
        var targetVector = particle.getTargetVector();
        particle.setDVelocity(SFM.multiply(targetVector, particle.getDSpeed()));

        double[] aVelocityForce = particle.velocityForce();
        double[] peopleInteraction = new double[]{0.0, 0.0};
        double[] wallInteraction = new double[]{0.0, 0.0};

        for (Particle other : neighbours) {
            if (particle == other) continue;
            double[] interaction = particle.f_ij(other);
            peopleInteraction = SFM.add(peopleInteraction, interaction);
        }

        for (Wall wall : force.board.getWalls()) {
            double[] interaction = particle.f_ik_wall(wall);
            wallInteraction = SFM.add(wallInteraction, interaction);
        }

        double[] sumForce = SFM.add(SFM.add(aVelocityForce, peopleInteraction), wallInteraction);
        double[] dv_dt = SFM.divide(sumForce, particle.getMass());
        double[] v = SFM.add(particle.aVelocity(), SFM.multiply(dv_dt, dt));
        double[] r = SFM.add(particle.pos(), SFM.multiply(particle.aVelocity(), dt));

        state[0] = new State(r[0], v[0], t);
        state[1] = new State(r[1], v[1], t);
        return state;
    }

}
