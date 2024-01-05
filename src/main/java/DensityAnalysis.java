import java.io.IOException;

public class DensityAnalysis {

    public static void main(String[] args) throws IOException {

        var simulations = 5;
        var iterations = 150000;
        int n = 150;
        double d = 1.2;
        double l = 50;
        int turnstiles = 5;
        double minR = 0.3;
        double maxR = 0.58;
        double m = 70;
        double minV = 1.0;
        double maxV = 1.0;
        double tau = 0;
        double beta = 0;
        double transactionTime = 6.0;
        double decisionPoint = 0.9*l;

        boolean completed;
        int broken = 0;

        for (int i = 0; i < simulations; i++) {
            System.out.println("##############################\n");
            System.out.println("Starting Simulation number " + i);
            Board board = Board.getRandomBoard(n, d, turnstiles, transactionTime, l, Board.optM(l, maxR),
                    minR, maxR, minV, maxV, maxV, tau, beta, maxV, m);

            PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau, "distance", i+1, decisionPoint);
            completed = simulation.simulate(iterations, true);

            if(completed) {
                OutputData.writeSimulation(simulation);
            } else {
                broken++;
                i--;
            }

        }
        System.out.println("Broken simulations: " + broken);
    }
}
