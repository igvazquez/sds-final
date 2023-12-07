import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
//        var p1 = new Particle(1, 7, 4, 0, -0.6, 0.7, new double[]{10, 2}, 60, 0.5);
//        var p2 = new Particle(2, 10, 4, 0, -0.5, 0.7, new double[]{10, 2}, 60, 0.5);
//        var p3 = new Particle(3, 13, 4, 0, -0.5, 0.7, new double[]{10, 2}, 60, 0.5);


//        var particles = List.of(p1, p2, p3);

        var iterations = 100000;
        int n = 60;
        double d = 1.6;
        double l = 30;
        int turnstiles = 5;
        double minR = 0.3;
        double maxR = 0.58;
        double m = 60;
        double minV = 0.5;
        double maxV = 0.7;
        double tau = 0;
        double beta = 0;
        double transactionTime = 2;

        Board board = Board.getRandomBoard(n, d, turnstiles, transactionTime, l, Board.optM(l, maxR),
                minR, maxR, minV, maxV, maxV, tau, beta, maxV, m);

        PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau, "else", 1);
        simulation.simulate(iterations, true);
    }
}
