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

    public double[] getAcceleration(final Particle p, final Set<Particle> neighbours) {
        double[] Fg = new double[2]; // Granular Force
        double[] Fs = new double[2]; // Social Force
        double[] Fd; // Desire Force

        Fd = calculateDesireForce(p);

        neighbours.remove(p);

        var wallFg = calculateWallForce(p);
        Fg[0] += wallFg[0];
        Fg[1] += wallFg[1];

        for (final Particle other : neighbours) {
            var fg = calculateGranularForce(p, other);
            var fs = calculateSocialForce(p, other);

            Fg[0] += fg[0];
            Fg[1] += fg[1];
            Fs[0] += fs[0];
            Fs[1] += fs[1];
        }


        final var a = new double[2];
        a[0] = (Fg[0] + Fs[0] + Fd[0]) / p.getMass();
        a[1] = (Fg[1] + Fs[1] + Fd[1]) / p.getMass();
        return a;
    }

    private double[] calculateWallForce(Particle p) {
        double d, g = 0;
        double[] niw = new double[2];
        double[] tiw = new double[2];
        Particle wall = new Particle(-1, 0, 0, 0.0, 0.0,
                0.0, new double[]{0.0, 0.0}, 0.0, 0.0);

        if (p.getX() - p.getRadius() <= 0 + Board.getxPadding()) {
            // WALL TO THE LEFT
            g = Math.abs(p.getX() - Board.getxPadding()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(Board.getxPadding() - p.getX());
            wall = new Particle(-1, Board.getxPadding(), p.getY(), 0.0, 0.0,
                    0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
        } else if (p.getX() + p.getRadius() >= board.getL() - Board.getxPadding()) {
            // WALL TO THE RIGHT
            g = Math.abs(board.getL() - Board.getxPadding() - p.getX()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(board.getL() - Board.getxPadding() - p.getX());
            wall = new Particle(-1, board.getL() - Board.getxPadding(), p.getY(), 0.0, 0.0,
                    0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
            niw = p.getNij(wall);
            tiw = p.getTangentVector(wall);
        } else if (p.getY() + p.getRadius() >= board.getL() - Board.getyPadding()) {
            // WALL ABOVE
            g = Math.abs((board.getL() - Board.getyPadding()) - p.getY()) > p.getRadius() ? 0 : p.getRadius() - Math.abs((board.getL() - Board.getyPadding()) - p.getY());
            wall = new Particle(-1, p.getX(), board.getL() - Board.getyPadding(), 0.0, 0.0,
                    0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
            niw = p.getNij(wall);
            tiw = p.getTangentVector(wall);
        } else if ((p.getY() <= Board.getyPadding() && p.getY() + p.getRadius() >= Board.getyPadding()) || (p.getY() > Board.getyPadding() && p.getY() - p.getRadius() <= Board.getyPadding())) {
            // TODO: Fix condition that locates turnstiles
            // If it's not in front of a turnstile
            if (p.getX() <= board.getL() / 2 - board.getDoorWidth() / 2 || p.getX() >= board.getL() / 2 + board.getDoorWidth() / 2) {
                // WALL BELOW
                g = Math.abs(p.getY() - Board.getyPadding()) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getY() - Board.getyPadding());
                wall = new Particle(-1, p.getX(), Board.getyPadding(), 0.0, 0.0,
                        0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
                niw = p.getNij(wall);
                tiw = p.getTangentVector(wall);
                // If it bounces against the walls of the turnstile
            } else if (p.getX() - p.getRadius() <= board.getL() / 2 - board.getDoorWidth() / 2) {
                // TURNSTILE WALL TO THE LEFT
                if (p.getY() > Board.getyPadding()) {
                    wall = new Particle(-1, (board.getL() / 2 - board.getDoorWidth() / 2), Board.getyPadding(), 0.0, 0.0,
                            0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
                    g = p.centerDistanceTo(wall) > p.getRadius() + wall.getRadius() ?
                            0 : p.getRadius() + wall.getRadius() - p.centerDistanceTo(wall);
                    niw = p.getNij(wall);
                    tiw = p.getTangentVector(wall);
                } else {
                    // Wall (same y as particle, has its own x)
                    // Should this happen? Or do we guide the particle to the center if the turnstile is empty?
                    g = Math.abs(p.getX() - (board.getL() / 2 - board.getDoorWidth() / 2)) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getX() - (board.getL() / 2 - board.getDoorWidth() / 2));
                    d = Math.hypot(p.getX() - (board.getL() / 2 - board.getDoorWidth() / 2), 0);  // closest point of the wall shares the same y value
                    niw = new double[]{(p.getX() - (board.getL() / 2 - board.getDoorWidth() / 2)) / d, 0};
                }

            } else if (p.getX() + p.getRadius() >= board.getL() / 2 + board.getDoorWidth() / 2) {
                // TURNSTILE WALL TO THE RIGHT
                if (p.getY() > Board.getyPadding()) {
                    wall = new Particle(-1, (board.getL() / 2 + board.getDoorWidth() / 2), Board.getyPadding(), 0.0, 0.0,
                            0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
                    g = p.centerDistanceTo(wall) > p.getRadius() + wall.getRadius() ?
                            0 : p.getRadius() + wall.getRadius() - p.centerDistanceTo(wall);
                    niw = p.getNij(wall);
                    tiw = p.getTangentVector(wall);
                } else {
                    // Wall (same y as particle, has its own x)
                    // Should this happen? Or do we guide the particle to the center if the turnstile is empty?
                    g = Math.abs(p.getX() - (board.getL() / 2 + board.getDoorWidth() / 2)) > p.getRadius() ? 0 : p.getRadius() - Math.abs(p.getX() - (board.getL() / 2 + board.getDoorWidth() / 2));

                }
            }
        }
        var prod = p.getVx() * tiw[0] + p.getVy() * tiw[1];
        var exp = A * Math.exp((p.getRadius() - p.centerDistanceTo(wall)) / B);

        double[] fn = new double[]{kn * g * niw[0], kn * g * niw[1]};
        double[] ft = {kt * g * prod * tiw[0], kt * g * prod * tiw[1]};

        //double[] ft = new double[] {-fn[1], fn[0]};
        return new double[]{fn[0] + ft[0] + exp * niw[0], fn[1] + ft[1] + exp * niw[1]};
    }

    public double[] getForce(final Particle p, final Set<Particle> neighbours) {
        var a = getAcceleration(p, neighbours);
        a[0] = p.getMass() * a[0];
        a[1] = p.getMass() * a[1];
        return a;
    }

    private double[] calculateGranularForce(final Particle p, final Particle other) {
        var nij = p.getNij(other);
        var tij = p.getTangentVector(other);
        var dvt = (other.getVx() - p.getVx()) * tij[0] + (other.getVy() - p.getVy()) * tij[1];
        var g = p.centerDistanceTo(other) > p.getRadius() + other.getRadius() ?
                0 : p.getRadius() + other.getRadius() - p.centerDistanceTo(other);

        double[] fn = {kn * g * nij[0], kn * g * nij[1]};
        double[] ft = {kt * g * dvt * tij[0], kt * g * dvt * tij[1]};

        return new double[]{fn[0] + ft[0], fn[1] + ft[1]};
    }

    private double[] calculateSocialForce(final Particle p, final Particle other) {
        var nij = p.getNij(other);

        var exp = A * Math.exp((p.getRadius() + other.getRadius() - p.centerDistanceTo(other)) / B);

        return new double[]{exp * nij[0], exp * nij[1]};
    }

    private double[] calculateDesireForce(final Particle p) {
        var target = p.getTargetVector();

        return new double[]{(p.getVd() * target[0] - p.getVx()) * p.getMass() / tau,
                (p.getVd() * target[1] - p.getVy()) * p.getMass() / tau};
    }
}
