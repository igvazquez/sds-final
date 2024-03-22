import java.io.IOException;

public class DensityAnalysis {

    public static void main(String[] args) throws IOException {

        var simulations = 1;
        var iterations = 150000;
        int n = 300;
        double d = 1.2;
        double l = 50;
        int turnstiles = 30;
        double minR = 0.2;
        double maxR = 0.5;
        double m = 70;
        double minV = 1.0;
        double maxV = 1.0;
        double transactionTime = 3.0;
        double decisionPoint = 0.3*l;

        boolean completed;
        int broken = 0;

        for (int i = 0; i < simulations; i++) {
            System.out.println("##############################\n");
            System.out.println("Starting Simulation number " + i);
            Board board = Board.getRandomBoard(n, d, turnstiles, transactionTime, l, Board.optM(l, maxR),
                    minR, maxR, minV, maxV, maxV, maxV, m);

            PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, "availability", i+1, decisionPoint);
            completed = simulation.simulate(iterations, true);

            if(completed) {
//                OutputData.writeSimulation(simulation, "output/transactionTime/t10/");
                OutputData.writeSimulation(simulation, "output/changeN/N"+n+"/");
            } else {
                broken++;
                i--;
                break;
            }

        }
        System.out.println("Broken simulations: " + broken);
    }
}
