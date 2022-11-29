import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
//        var p1 = new Particle(1, 7, 4, 0, -0.6, 0.7, new double[]{10, 2}, 60, 0.5);
//        var p2 = new Particle(2, 10, 4, 0, -0.5, 0.7, new double[]{10, 2}, 60, 0.5);
//        var p3 = new Particle(3, 13, 4, 0, -0.5, 0.7, new double[]{10, 2}, 60, 0.5);


//        var particles = List.of(p1, p2, p3);

        var iterations = 100000;
        int n = 35;
        double d = 1.2;
        double l = 20;
        double minR = 0.5;
        double maxR = 0.5;
        double m = 60;
        double minV = 0.5;
        double maxV = 0.7;
        double tau = 0;
        double beta = 0;
        double transactionTime = 2;

//        Board board = new Board(l, d, 1, transactionTime, minR, maxR, maxV, tau, beta, maxV, Board.optM(l, maxR), particles);
        Board board = Board.getRandomBoard(n, d, 3, transactionTime, l, Board.optM(l, maxR)-5, minR, maxR, minV, maxV, maxV, tau, beta, maxV, m);
//        var sfm = new SFM(1.2E5, 2.4E5, 2000, 0.08, 0.5, board);
//        board.setSfm(sfm);
//        p1.setIntegrator(new Verlet(p1, board.getSfm()));
//        p2.setIntegrator(new Verlet(p2, board.getSfm()));
//        p3.setIntegrator(new Verlet(p3, board.getSfm()));

        PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau);
        simulation.simulate(iterations, true);
    }
}
