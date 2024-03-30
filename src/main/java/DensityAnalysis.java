import java.io.IOException;

public class DensityAnalysis {

    public static void main(String[] args) throws IOException {

        var simulations = 1;
        var iterations = 20000;
        int n = 200;
        double d = 1.0;
        double l = 50;
        int turnstiles = 20;
        double minR = 0.25;
        double maxR = 0.4;
        double m = 70;
        double minV = 1.0;
        double maxV = 1.0;
        double transactionTime = 1.0;
        double decisionPoint = 0.4*l;
        double queueLength = 3.5;

        boolean completed;
        int broken = 0;

        for (int i = 0; i < simulations; i++) {
            System.out.println("##############################\n");
            System.out.println("Starting Simulation number " + i);
            Board board = Board.getRandomBoard(n, d, turnstiles, transactionTime, l, Board.optM(l, maxR),
                    minR, maxR, minV, maxV, maxV, maxV, m, queueLength);

            PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, "distance", i+1, decisionPoint);
            completed = simulation.simulate(iterations, true);

            if(completed) {
//                OutputData.writeSimulation(simulation, "output/transactionTime/t10/");
//                OutputData.writeSimulation(simulation, "output/changeN/N"+n+"/");
                OutputData.writeSimulation(simulation, "output/test/");
            } else {
                broken++;
                i--;
                break;
            }

        }
        System.out.println("Broken simulations: " + broken);
    }
}
