import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        var p1 = new Particle(1, 10, 4, 0, -0.6, 0.7, new double[]{10, 2}, 60, 0.5);
        var p2 = new Particle(2, 10, 5.1, 0, -0.5, 0.7, new double[]{10, 2}, 60, 0.5);


        var particles = List.of(p1, p2);
        var neighbours = new HashMap<Particle, List<Particle>>();
        neighbours.put(p1, List.of(p2));
        neighbours.put(p2, List.of(p1));
        var iterations = 10000;
        int n = 20;
        double d = 1.2;
        double l = 20;
        double minR = 0.5;
        double maxR = 0.6;
        double m = 60;
        double minV = 0.5;
        double maxV = 0.7;
        double tau = 0;
        double beta = 0;
        double transactionTime = 2;

        Board board = new Board(l, d, 1, transactionTime, minR, maxR, maxV, tau, beta, maxV, Board.optM(l, maxR), particles);
        var sfm = new SFM(1.2E5, 2.4E5, 2000, 0.08, 0.5, board);

        p1.setIntegrator(new Verlet(p1, sfm));
        p2.setIntegrator(new Verlet(p2, sfm));

        board = Board.getRandomBoard(n, d, 3, transactionTime, l, Board.optM(l, maxR), minR, maxR, minV, maxV, maxV, tau, beta, maxV, m);
        PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau);
        simulation.simulate(iterations, true);
    }
}
