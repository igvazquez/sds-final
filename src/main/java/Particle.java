import lombok.Data;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Data
public class Particle {

    int id;
    double x;
    double y;
    double vx;
    double vy;
    double vd; // Desired Velocity
    double[] target;
    double mass;
    double radius;
    boolean locked;
    boolean payed;
    int turnstileTargeted;
    Verlet integrator;

    private double bodyFactor;
    private double F;
    private double delta;
    private double dSpeed;
    private double acclTime;
    private double[] dVelocity;

    public Particle(int id, double x, double y, double vx, double vy,
                    double vd, double[] target, double mass, double radius) {
        this.turnstileTargeted = -1;
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.vd = vd;
        this.target = target;
        this.mass = mass;
        this.radius = radius;
        this.locked = false;
        this.payed = false;

        this.bodyFactor = 120000;
        this.F = 2000;
        this.delta = 0.08 * 50;
        this.dSpeed = 12;
        this.acclTime = 0.5;
        this.dVelocity = SFM.multiply(getTargetVector(), this.dSpeed);
    }

    public Particle(int id, double x, double y, double vx, double vy,
                    double vd, double[] target, double mass, double radius, Verlet integrator) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.vd = vd;
        this.target = target;
        this.mass = mass;
        this.radius = radius;
        this.integrator = integrator;
    }

    public double[] getNij(final Particle other){
        double[] nij = new double[2];
        double d = Math.hypot(x - other.getX(), y - other.getY());

        nij[0] = (x - other.getX()) / d;
        nij[1] = (y - other.getY()) / d;

        return nij;
    }

    public double[] getTangentVector(final Particle other) {
        double[] tij = new double[2];
        double d = Math.hypot(x - other.getX(), y - other.getY());

        tij[0] = -(other.getY() - y) / d;
        tij[1] = (other.getX() - x) / d;

        return tij;
    }

    public double distanceTo(final Particle p) {
        return Math.hypot(x - p.getX(), y - p.getY()) - radius - p.radius;
    }

    public double centerDistanceTo(final Particle p) {
        return Math.hypot(x - p.getX(), y - p.getY());
    }

    public double[] getTargetVector(){
        double d = Math.hypot(target[0] - x, target[1] - y);
        return new double[]{(target[0] - x)/d, (target[1] - y)/d};
    }

    public double calculateDistance(Particle p, double L, boolean periodicOutline) {
        double x = distanceFromAxis(this.x, p.getX(), L, periodicOutline);
        double y = distanceFromAxis(this.y, p.getY(), L, periodicOutline);

        return Math.sqrt(x*x + y*y) - this.radius - p.getRadius();
    }

    private double distanceFromAxis(double ax1, double ax2, double L, boolean periodicOutline){
        double distance = Math.abs(ax1 - ax2);

        if (periodicOutline){
            if(distance > L/2){
                distance = L - distance;
            }
        }
        return distance;
    }

    public boolean collides(final Particle particle) {
        return distanceTo(particle) <= 0;
    }

    public void advanceParticle(double t, final double dt, final Set<Particle> neighbours) {
        if(!locked) {
            var newState = integrator.fixedStep(t, dt, neighbours);

            x = newState[0].getR();
            vx = newState[0].getV();
            y = newState[1].getR();
            vy = newState[1].getV();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Particle)) return false;
        Particle particle = (Particle) o;
        return id == particle.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public double[] f_ij(Particle other) {
        double r_ij = this.radius + other.radius;
        double d_ij = Math.sqrt(SFM.dotProduct(SFM.subtract(this.pos(), other.pos()), SFM.subtract(this.pos(), other.pos())));
        double[] e_ij = SFM.normalize(SFM.subtract(this.pos(), other.pos()));

        return SFM.add(SFM.multiply(SFM.multiply(e_ij, this.F), Math.exp((r_ij - d_ij) / this.delta)),
                SFM.multiply(SFM.multiply(e_ij, this.bodyFactor), SFM.g(r_ij - d_ij)));
    }

    public double[] f_ik_wall(Wall wall) {
        double r_i = this.radius;
        double[] result = wall.distanceAgentToWall(this.pos());
        double d_iw = result[0];
        double[] e_iw = Arrays.copyOfRange(result, 1, result.length);
        return SFM.add(SFM.multiply(e_iw, -this.F * Math.exp((r_i - d_iw) / this.delta)),
                SFM.multiply(SFM.multiply(e_iw, this.bodyFactor), SFM.g(r_i - d_iw)));
    }

    public double[] velocityForce() {
        double[] deltaV = SFM.subtract(this.dVelocity, new double[]{vx, vy});
        if (Arrays.equals(deltaV, new double[]{0, 0})) {
            deltaV = new double[]{0, 0};
        }
        return SFM.multiply(deltaV, this.mass / this.acclTime);
    }

    double[] pos() {
        return new double[]{x, y};
    }

    double[] aVelocity() {
        return new double[]{vx, vy};
    }

    void setAVelocity(double[] v) {
        vx = v[0];
        vy = v[1];
    }
}
