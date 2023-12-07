import java.io.IOException;

public class DensityAnalysis {

    public static void main(String[] args) throws IOException {

        var simulations = 1;
        var iterations = 150000;
        int n = 50;
        double d = 1.2;
        double l = 30;
        int turnstiles = 5;
        double minR = 0.3;
        double maxR = 0.58;
        double m = 50;
        double minV = 0.5;
        double maxV = 0.7;
        double tau = 0;
        double beta = 0;
        double transactionTime = 2;

        boolean completed;
        int broken = 0;

        for (int i = 0; i < simulations; i++) {
            System.out.println("##############################\n");
            System.out.println("Starting Simulation number " + i);
            Board board = Board.getRandomBoard(n, d, turnstiles, transactionTime, l, Board.optM(l, maxR),
                    minR, maxR, minV, maxV, maxV, tau, beta, maxV, m);

            PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau, "else", i+1);
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
