import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        var p1 = new Particle(1, 7, 2.65, 0.5, 0, 0.7, new double[]{10, 2}, 60, 0.5);
        var p2 = new Particle(2, 7, 3.65, 0.5, 0, 0.7, new double[]{10, 2}, 60, 0.5);


        var particles = List.of(p1, p2);
        var neighbours = new HashMap<Particle, List<Particle>>();
        neighbours.put(p1, List.of(p2));
        neighbours.put(p2, List.of(p1));
        var dt = 0.1;
        var t = 0.0;
        var iterations = 10000;
        int n = 10;
        double d = 1.2;
        double l = 20;
        double minR = 0.5;
        double maxR = 0.6;
        double m = 60;
        double maxV = 0.7;
        double tau = 0;
        double beta = 0;

        Board board = new Board(l, d, minR, maxR, maxV, tau, beta, maxV, Board.optM(l, maxR), particles);
        var sfm = new SFM(1.2E5, 2.4E5, 2000, 0.08, 0.5, board);


        p1.setIntegrator(new Verlet(p1, sfm));
        p2.setIntegrator(new Verlet(p2, sfm));

        board = Board.getRandomBoard(n, d, l, Board.optM(l, maxR), minR, maxR, maxV, maxV, tau, beta, maxV, m);
        PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau);
        simulation.simulate(iterations, true);
    }
}
