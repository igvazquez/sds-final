import lombok.Data;

import java.util.Set;

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

    public double[] getAcceleration(final Particle p, final Set<Particle> neighbours, final double time) {
        double[] Fg = new double[2];    // Granular Force
        double[] Fs = new double[2];    // Social Force
        double[] Fd;                    // Desire Force

        Fd = calculateDesireForce(p);

        if(!p.isLocked()){
            var wallFg = calculateWallForce(p, time);
            Fg[0] += wallFg[0];
            Fg[1] += wallFg[1];

            neighbours.remove(p);
            for (final Particle other : neighbours) {
                var fg = calculateGranularForce(p, other);
                var fs = calculateSocialForce(p, other);

                Fg[0] += fg[0];
                Fg[1] += fg[1];
                Fs[0] += fs[0];
                Fs[1] += fs[1];
            }
        }

        final var a = new double[2];
        a[0] = (Fg[0] + Fs[0] + Fd[0]) / p.getMass();
        a[1] = (Fg[1] + Fs[1] + Fd[1]) / p.getMass();
        return a;
    }

    private static double wallCheat(double magnitudeInsideWall) {
        return Math.abs(magnitudeInsideWall) > 0.001 ? (Math.abs(magnitudeInsideWall) / magnitudeInsideWall) * 0.0001 : magnitudeInsideWall;
    }

    private double calculateOverlap(Particle p, Particle other) {
        return Math.hypot(p.getX() - other.getX(), p.getY() - other.getY()) > p.getRadius() + other.getRadius()
                ? 0 : wallCheat(p.getRadius() + other.getRadius() - Math.hypot(p.getX() - other.getX(), p.getY() - other.getY()));
    }

    private double[] calculateWallForce(final Particle p, final double time) {
        double g = 0;
        double[] niw;
        double[] tiw;
        Particle wall = new Particle(-1, 0, 0, 0.0, 0.0,
                0.0, new double[]{0.0, 0.0}, 0.0, 0.0);

        if (board.collidesLeftWall(p)) {
            wall = new Particle(-1, Board.getXPadding(), p.getY(), 0.0, 0.0,
                    0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
            g = calculateOverlap(p, wall);
        } else if (board.collidesRightWall(p)) {
            wall = new Particle(-1, board.getL() - Board.getXPadding(), p.getY(), 0.0, 0.0,
                    0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
            g = calculateOverlap(p, wall);
        } else if (board.collidesUpperWall(p)) {
            wall = new Particle(-1, p.getX(), board.getL() - Board.getYPadding(), 0.0, 0.0,
                    0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
            g = calculateOverlap(p, wall);
        } else if (board.isInLowArea(p)) {
            // If it's not in front of a turnstile
            var t = board.getTurnstiles().size();
            var turnstilePadding = (board.getL() - 2*Board.X_PADDING - t*board.getDoorWidth())/(t+1);
            boolean bounce = false;

            for (int i = 0; i <= t && !bounce; i++) {
                //estos 2 son para muros entre molinetes
                Turnstile left = i != 0 ? board.getTurnstiles().get(i-1) :
                        new Turnstile(Board.X_PADDING + (-1+1)*turnstilePadding + -1*board.getDoorWidth(),
                                Board.Y_PADDING, 1.5, board.getDoorWidth(), 0.0);
                Turnstile right = i != t ? board.getTurnstiles().get(i) :
                        new Turnstile(Board.X_PADDING + (i+1)*turnstilePadding + i*board.getDoorWidth(),
                                Board.Y_PADDING, 1.5, board.getDoorWidth(), 0.0);
                //este es para evaluar si dentro del area molinete
                Turnstile current = i != t ? right : left;

                if(board.isBetweenTurnstiles(p, left, right)) {
                    wall = new Particle(-1, p.getX(), Board.getYPadding(), 0.0, 0.0,
                            0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
                    g = calculateOverlap(p, wall);
                    bounce = true;
                } else if(board.isWithinTurnstile(p, current) && current.isLocked()) {
                    //tiene que rebotar como si fuera la pared porque esta siendo usado
                    wall = new Particle(-1, p.getX(), Board.getYPadding(), 0.0, 0.0,
                            0.0, new double[]{0.0, 0.0}, 0.0, 0.0);
                    g = calculateOverlap(p,wall);
                    bounce = true;
                } else if(board.isWithinTurnstile(p, current) && !current.isLocked()) {
                    //puede chocar con las puntas o entrar
                    if(p.getX() - p.getRadius() <= current.x) {
                        //punta izquierda
                        wall = new Particle(-1, current.x, current.y, 0.0, 0.0,
                                0.0, new double[]{0.0, 0.0}, 0.0, 0.02);
                        g = calculateOverlap(p, wall);
                    } else if(p.getX() + p.getRadius() >= current.x + current.width) {
                        //punta derecha
                        wall = new Particle(-1,current.x + current.width, current.y, 0.0, 0.0,
                                0.0, new double[]{0.0, 0.0}, 0.0, 0.02);
                        g = calculateOverlap(p, wall);
                    } else {
                        // Entering turnstile
                        current.lockTurnstile(p, time);
                    }
                    bounce = true;
                }
            }
        }
        niw = p.getNij(wall);
        tiw = p.getTangentVector(wall);

        var prod = p.getVx() * tiw[0] + p.getVy() * tiw[1];
        var exp = A * Math.exp((p.getRadius() - p.centerDistanceTo(wall)) / B);

        double[] fn = new double[]{kn * g * niw[0], kn * g * niw[1]};
        double[] ft = {kt * g * prod * tiw[0], kt * g * prod * tiw[1]};

        return new double[]{fn[0] + ft[0] + exp * niw[0], fn[1] + ft[1] + exp * niw[1]};
    }

    public double[] getForce(final Particle p, final Set<Particle> neighbours, final double time) {
        var a = getAcceleration(p, neighbours, time);

        return new double[]{p.getMass() * a[0], p.getMass() * a[1]};
    }

    private double[] calculateGranularForce(final Particle p, final Particle other) {
        var nij = p.getNij(other);
        var tij = p.getTangentVector(other);
        var dvt = (other.getVx() - p.getVx()) * tij[0] + (other.getVy() - p.getVy()) * tij[1];
        var g = calculateOverlap(p, other);
                /* p.centerDistanceTo(other) > p.getRadius() + other.getRadius() ?
                0 : p.getRadius() + other.getRadius() - p.centerDistanceTo(other);*/

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
