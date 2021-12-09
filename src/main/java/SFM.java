import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class SFM {

    double kn;
    double kt;
    double A;
    double B;
    double tau;

    public SFM(double kn, double kt, double a, double b, double tau) {
        this.kn = kn;
        this.kt = kt;
        A = a;
        B = b;
        this.tau = tau;
    }

    public double[] getAcceleration(final Particle p, final List<Particle> neighbours){
        double[] Fg = new double[2]; // Granular Force
        double[] Fs = new double[2]; // Social Force
        double[] Fd; // Desire Force

        Fd = calculateDesireForce(p);

//        neighbours.remove(p);
        for(final Particle other : neighbours){
            var fg = calculateGranularForce(p, other);
            var fs = calculateSocialForce(p, other);

            Fg[0] += fg[0];
            Fg[1] += fg[1];
            Fs[0] += fs[0];
            Fs[1] += fs[1];
        }

        final var a = new double[2];
        a[0] = (Fg[0] + Fs[0] + Fd[0])/p.getMass();
        a[1] = (Fg[1] + Fs[1] + Fd[1])/p.getMass();
        return a;
    }

    public double[] getForce(final Particle p, final List<Particle> neighbours){
        var a = getAcceleration(p, neighbours);
        a[0] = p.getMass()*a[0];
        a[1] = p.getMass()*a[1];
        return a;
    }

    private double[] calculateGranularForce(final Particle p, final Particle other) {
        var nij = p.getNij(other);
        var tij = p.getTangentVector(other);
        var dvt = (other.getVx()-p.getVx())*tij[0] + (other.getVy()-p.getVy())*tij[1];
        var g = p.centerDistanceTo(other) > p.getRadius() + other.getRadius() ?
                0 : p.getRadius() + other.getRadius() - p.centerDistanceTo(other);

        double[] fn = {kn*g*nij[0], kn*g*nij[1]};
        double[] ft = {kt*g*dvt*tij[0], kt*g*dvt*tij[1]};

        return new double[]{fn[0] + ft[0], fn[1] + ft[1]};
    }

    private double[] calculateSocialForce(final Particle p, final Particle other) {
        var nij = p.getNij(other);

        var exp = A*Math.exp((p.getRadius() + other.getRadius() - p.centerDistanceTo(other))/B);

        return new double[]{exp*nij[0], exp*nij[1]};
    }

    private double[] calculateDesireForce(final Particle p) {
        var target = p.getTargetVector();

        return new double[]{(p.getVd()*target[0]-p.getVx())*p.getMass()/tau,
                            (p.getVd()*target[1]-p.getVy())*p.getMass()/tau};
    }
}
