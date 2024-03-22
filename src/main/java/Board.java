import lombok.Data;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Board {

    public static final double TARGET_DISTANCE_FROM_DOOR = -10;
    public static final double TARGET_LENGTH = 3;
    public static final double TARGET_TRIM = 0.2;
    public static final double X_PADDING = 5.0;
    public static final double Y_PADDING = 2.0;

    private final double L;
    private final double minR;
    private final double maxR;
    private final double minV;
    private final double maxV;
    private final double Ve;
    private final double dt;
    private final double doorWidth;
    private final List<Turnstile> turnstiles;
    private final int M;
    private final Map<Integer, List<Particle>> cells;
    private final double referenceDt;
    private List<Particle> particles;
    private List<Particle> assignableParticles;
    private SFM sfm;

    public Board(double l, double d, int turnstiles, double transactionTime,
                 double minR, double maxR, double minV, double maxV,
                 double Ve, int m, List<Particle> particles) {
        L = l;
        this.minR = minR;
        this.maxR = maxR;
        this.minV = minV;
        this.maxV = maxV;
        this.Ve = Ve;
        this.doorWidth = d;
        this.turnstiles = new ArrayList<>(turnstiles);
        this.assignableParticles = new ArrayList<>();
        this.dt = 0.0028;//Math.round(Math.sqrt(60.0 / 120000) / 8, 4);
        this.referenceDt = minR / (2 * Math.max(maxV, Ve));
        M = m;
        this.cells = new HashMap<>();
        generateTurnstiles(turnstiles, transactionTime);
        sortBoard(particles);
    }

    private void generateTurnstiles(final int t, final double transactionTime) {
        if (L - 2*X_PADDING < t*doorWidth || t <= 0){
            throw new IllegalArgumentException("Invalid amount of turnstiles");
        }
        var turnstilePadding = (L - 2*X_PADDING - t*doorWidth)/(t+1);
        for (int i = 0; i < t; i++) {
            turnstiles.add(new Turnstile(X_PADDING + (i+1)*turnstilePadding + i*doorWidth, Y_PADDING, 1.5, doorWidth, transactionTime));
        }
    }

    public double getDoorWidth() {
        return doorWidth;
    }

    public static double getXPadding() {
        return X_PADDING;
    }

    public static double getYPadding() {
        return Y_PADDING;
    }

    public double getRealWidth(){
        return L - 2*X_PADDING;
    }

    public double getRealHeight(){
        return L - 2*Y_PADDING;
    }
    public double getL() {
        return L;
    }

    public boolean isBetweenTurnstiles(Particle p, Turnstile t1, Turnstile t2) {
        //ASUME QUE T1 ESTA ANTES QUE T2
        return p.getX() > t1.x + t1.width && p.getX() < t2.x;
    }

    public boolean collidesLeftWall(final Particle p) {
        return p.getX() - p.getRadius() <= 0 + getXPadding();
    }

    public boolean collidesRightWall(final Particle p) {
        return p.getX() + p.getRadius() >= getL() - getXPadding();
    }

    public boolean collidesUpperWall(final Particle p) {
        return p.getY() + p.getRadius() >= getL() - getYPadding();
    }
    public boolean isInLowArea(final Particle p) {
        //centro por arriba de linea de zona baja y borde por debajo de linea zona baja
        //o centro por abajo de linea de zona baja y borde por arriba
        return (p.getY() <= getYPadding() && p.getY() + p.getRadius() >= getYPadding()) ||
                (p.getY() > getYPadding() && p.getY() - p.getRadius() <= getYPadding());
    }

    public boolean isWithinTurnstile(Particle p, Turnstile t) {
        //Solo mira por coord x
        return p.getX() > t.x && p.getX() < t.x + t.width;
    }

    public void sortBoard(List<Particle> newParticles) {
        for (int i = 0; i < M * M; i++) {
            cells.put(i, new ArrayList<>());
        }
        this.particles = newParticles;
        divideParticles();
    }

    public void divideParticles() {
        for (Particle p : particles) {
            if (p.getX() < 0 || p.getX() > L || p.getY() < 0 || p.getY() > L) {
                throw new IllegalArgumentException("Partícula " + p.getId() + " fuera de los límites." + "X: " + p.getX() + " " + "Y: " + p.getY());
            }
            cells.get(calculateCellIndexOnBoard(p.getX(), p.getY())).add(p);
        }
    }

    public Integer calculateCellIndexOnBoard(double x, double y) {
        int i = (int) (x / (L / M));
        int j = (int) (y / (L / M));
        return i + M * j;
    }

    private static boolean overlap(double x, double y, double r, double l, List<Particle> particles) {
        if (x - r <= X_PADDING || x + r >= l - X_PADDING || y + r >= l - Y_PADDING || y - r <= Y_PADDING) {
            return true;
        }
        for (Particle p : particles) {
            if (Math.hypot(x - p.getX(), y - p.getY()) - r - p.getRadius() <= 0) {
                return true;
            }
        }
        return false;
    }

    public void assignTurnstiles(String mode, double decisionPoint) {
        double[] vel;
        double[] target;
        List<Particle> toRemove = new ArrayList<>();
        for (Particle p: assignableParticles) {
            if(p.getY() <= decisionPoint) {
                // area de decision
                if(mode.equals("distance")) {
                    // molinete mas cercano
                    target = getParticleTarget(getTurnstiles(), L, p.getX());
                } else {
                    //dame el mas desocupado
                    Turnstile free = turnstiles
                            .stream()
                            .min(Comparator.comparing(Turnstile::getTargeted,
                                    Comparator.comparingInt(List::size))).get();
                    //poner target a la particula, asignar particula a molinete
                    free.targeted.add(p);
                    target = new double[]{free.getX() + free.getWidth()/2, Y_PADDING};
                    p.setTurnstileTargeted(turnstiles.indexOf(free));
                }
                vel = calculateVelocityToTarget(p.vx, p.vy, p.getX(), p.getY(), target);
                p.setTarget(target);
                p.setVx(vel[0]);
                p.setVy(vel[1]);
                toRemove.add(p);
            }
        }
        assignableParticles.removeAll(toRemove);
    }

    public static Board getRandomBoard(int n, double d, int turnstiles, double transactionTime, double l,
                                       int m, double minR, double maxR, double minV, double maxV, double vd,
                                       double ve,double maxMass) {

        List<Particle> particles = new ArrayList<>();

        double x, y, radius;
        double[] vel;
        Board board = new Board(l, d, turnstiles, transactionTime, minR, maxR, minV, maxV, ve, m, new ArrayList<>());
        var sfm = new SFM(1.2E5, 2.4E5, 2000, 0.08, 0.5, board);
        board.setSfm(sfm);

        int i;
        for (i = 0; i < n; i++) {
            do {
                x = X_PADDING + Math.random() * (l-2*X_PADDING);
                y = Y_PADDING + Math.random() * (l-2*Y_PADDING);
                radius = ThreadLocalRandom.current().nextDouble(minR, maxR);
            } while (overlap(x, y, radius, l, particles));

            var target = new double[]{0.0, -1000}; //   getParticleTarget(board.getTurnstiles(), l, x);
            vel = calculateVelocityToTarget(minV, maxV, x, y, target);
            Particle p = new Particle(i, x, y, vel[0], vel[1], vd, target, maxMass, radius);
            p.setIntegrator(new Verlet(p, sfm));
            particles.add(p);
        }

        board.setParticles(particles);
        board.assignableParticles.addAll(particles);
        return board;
    }

    private static double[] getParticleTarget(final List<Turnstile> turnstiles, final double l, final double x) {
        var t = (l-2*X_PADDING) / turnstiles.size();
        var turnstile = turnstiles.get((int)((x-X_PADDING)/t));

        return new double[]{turnstile.getX() + turnstile.getWidth()/2, Y_PADDING};
    }

    public static double[] calculateVelocityToTarget(final double minV, final double maxV, final double x, final double y, final double[] target) {
        double v = minV + Math.random() * (maxV - minV);
        double dx = target[0] - x;
        final double dy = target[1] - y;
        final double distance = Math.hypot(dx, dy);

        double vx = v * (dx / distance);
        double vy = v * (dy / distance);

        return new double[]{vx, vy};
    }

    public static int optM(final double l, final double rc) {
        return (int) (Math.floor(l / rc) * 0.5);
    }

    public List<Particle> getCell(int idx) {
        return cells.get(idx);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(M * M);
        b.append("Board:\n");
        for (int i = 0; i < M * M; i++) {
            b.append(cells.get(i).size()).append(" ");
            if (i % M == M - 1) {
                b.append("\n");
            }
        }
        return b.toString();
    }

    public void updateParticles(final List<Particle> currentState) {
        this.particles = currentState;
        sortBoard(particles);
    }

    public void updateTurnstiles(final double t) {
        for(final Turnstile turnstile : turnstiles){
            turnstile.tryUnlock(t);
        }
    }
}