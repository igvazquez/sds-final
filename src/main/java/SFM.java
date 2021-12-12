import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.apache.commons.math3.util.Precision.EPSILON;

@Data
public class SFM {

    double kn;
    double kt;
    double A;
    double B;
    double tau;
    Board board;

    public SFM(double kn, double kt, double a, double b, double tau, Board board) {
        this.kn = kn;
        this.kt = kt;
        A = a;
        B = b;
        this.tau = tau;
        this.board = board;
    }

    public double[] getAcceleration(final Particle p, final Set<Particle> neighbours){
        double[] Fg = new double[2]; // Granular Force
        double[] Fs = new double[2]; // Social Force
        double[] Fd; // Desire Force

        Fd = calculateDesireForce(p);

        neighbours.remove(p);

        var wallFg = calculateWallGranularForce(p);
        Fg[0] += wallFg[0];
        Fg[1] += wallFg[1];

        if (Math.abs(Fg[0]) <= EPSILON || Math.abs(Fg[1]) <= EPSILON){
            for(final Particle other : neighbours){
                var fg = calculateGranularForce(p, other);
                var fs = calculateSocialForce(p, other);

                Fg[0] += fg[0];
                Fg[1] += fg[1];
                Fs[0] += fs[0];
                Fs[1] += fs[1];
            }
        }else {
            System.out.println("asd");
        }


        final var a = new double[2];
        a[0] = (Fg[0] + Fs[0] + Fd[0])/p.getMass();
        a[1] = (Fg[1] + Fs[1] + Fd[1])/p.getMass();
        return a;
    }

    private double[] calculateWallGranularForce(Particle p) {
        double d, g = 0;
        double[] niw = new double[2];
        if (p.getX() - p.getRadius() <= 0 + Board.getxPadding()) {
            // WALL TO THE LEFT
            g = Math.abs(p.getX() - Board.getxPadding()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(Board.getxPadding() - p.getX());
            d = Math.hypot(p.getX() - Board.getxPadding(), 0);  // closest point of the wall shares the same y value
            niw = new double[] {(p.getX() - Board.getxPadding()) / d, 0};
        } else if (p.getX() + p.getRadius() >= board.getL() - Board.getxPadding()) {
            // WALL TO THE RIGHT
            g = Math.abs(board.getL() - Board.getxPadding() - p.getX()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(board.getL() - Board.getxPadding() - p.getX());
            d = Math.hypot((board.getL() - Board.getxPadding() - p.getX()), 0);     // closest point of the wall shares the same y value
            niw = new double[] {(p.getX()-(board.getL() - Board.getxPadding())) / d, 0};
        } else if (p.getY() + p.getRadius() >= board.getL() - Board.getyPadding()) {
            // WALL ABOVE
            g = Math.abs((board.getL() - Board.getyPadding()) - p.getY()) > p.getRadius() ? 0 : p.getRadius() - Math.abs((board.getL() - Board.getyPadding()) - p.getY());
            d = Math.hypot(0, (board.getL() - Board.getyPadding() - p.getY()));     // closest point of the wall shares the same x value
            niw= new double[] {0, (p.getY() - (board.getL() - Board.getyPadding())) / d};
        } else if ((p.getY() <= Board.getyPadding() && p.getY() + p.getRadius() >= Board.getyPadding()) || (p.getY() > Board.getyPadding() && p.getY() - p.getRadius() <= Board.getyPadding())) {
            // TODO: Fix condition that locates turnstiles
            // If it's not in front of a turnstile
            if (p.getX() <= board.getL()/2 - board.getDoorWidth()/2 || p.getX() >= board.getL()/2 + board.getDoorWidth()/2) {
                // WALL BELOW
                g = Math.abs(p.getY() - Board.getyPadding()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getY() - Board.getyPadding());
                d = Math.hypot(0, p.getY() - Board.getyPadding());     // closest point of the wall shares the same x value
                niw= new double[] {0, (p.getY() - Board.getyPadding()) / d};
            // If it bounces against the walls of the turnstile
            } else if (p.getX() - p.getRadius() <= board.getL()/2 - board.getDoorWidth()/2) {
                // TURNSTILE WALL TO THE LEFT
                if(p.getY() > Board.getyPadding()) {
                    // Border (has its own x and y coord)
                    double gx = Math.abs(p.getX() - (board.getL()/2 - board.getDoorWidth()/2)) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getX() - (board.getL()/2 - board.getDoorWidth()/2));
                    double gy = Math.abs(p.getY() - Board.getyPadding()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getY() - Board.getyPadding());
                    g = Math.hypot(gx, gy);
                    d = Math.hypot(p.getX() - (board.getL()/2 - board.getDoorWidth()/2), p.getY() - Board.getyPadding());
                    niw= new double[] {(p.getX() - (board.getL()/2 - board.getDoorWidth()/2)) / d, (p.getY() - Board.getyPadding()) / d};
                } else {
                    // Wall (same y as particle, has its own x)
                    // Should this happen? Or do we guide the particle to the center if the turnstile is empty?
                    g = Math.abs(p.getX() - (board.getL()/2 - board.getDoorWidth()/2)) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getX() - (board.getL()/2 - board.getDoorWidth()/2));
                    d = Math.hypot(p.getX() - (board.getL()/2 - board.getDoorWidth()/2), 0);  // closest point of the wall shares the same y value
                    niw = new double[] {(p.getX() - (board.getL()/2 - board.getDoorWidth()/2)) / d, 0};
                }

            } else if (p.getX() + p.getRadius() >= board.getL()/2 + board.getDoorWidth()/2) {
                // TURNSTILE WALL TO THE RIGHT
                if(p.getY() > Board.getyPadding()) {
                    // Border (has its own x and y coord)
                    double gx = Math.abs(p.getX() - (board.getL()/2 + board.getDoorWidth()/2)) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getX() - (board.getL()/2 + board.getDoorWidth()/2));
                    double gy = Math.abs(p.getY() - Board.getyPadding()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getY() - Board.getyPadding());
                    g = Math.hypot(gx, gy);
                    d = Math.hypot(p.getX() - (board.getL()/2 + board.getDoorWidth()/2), p.getY() - Board.getyPadding());
                    niw= new double[] {(p.getX() - (board.getL()/2 + board.getDoorWidth()/2)) / d, (p.getY() - Board.getyPadding()) / d};
                } else {
                    // Wall (same y as particle, has its own x)
                    // Should this happen? Or do we guide the particle to the center if the turnstile is empty?
                    g = Math.abs(p.getX() - (board.getL()/2 + board.getDoorWidth()/2)) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getX() - (board.getL()/2 + board.getDoorWidth()/2));
                    d = Math.hypot(p.getX() - (board.getL()/2 + board.getDoorWidth()/2), 0);  // closest point of the wall shares the same y value
                    niw = new double[] {(p.getX() - (board.getL()/2 + board.getDoorWidth()/2)) / d, 0};
                }
            }
        }
        double[] fn = new double[] {kn * g * niw[0], kn * g * niw[1]};
        double[] ft = new double[] {-fn[1], fn[0]};
        return new double[]{fn[0] + ft[0], fn[1] + ft[1]};
    }

    public double[] getForce(final Particle p, final Set<Particle> neighbours){
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
