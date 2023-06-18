import java.io.FileWriter;
import java.io.IOException;

public class DensityAnalysis {

    public static void main(String[] args) throws IOException {

        var simulations = 2;
        var iterations = 150000;
        int n = 60;
        double d = 1.2;
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

        for (int i = 0; i < simulations; i++) {
            System.out.println("##############################\n");
            System.out.println("Starting Simulation number " + i);
            Board board = Board.getRandomBoard(n, d, turnstiles, transactionTime, l, Board.optM(l, maxR),
                    minR, maxR, minV, maxV, maxV, tau, beta, maxV, m);

            PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau);
            simulation.simulate(iterations, false);

            var analysisArea = simulation.getOutputData().getDensityAnalysisY()*board.getRealWidth();

            var densityFw = new FileWriter(String.format("density_sims=%d_A=%,.2f.csv",
                    simulations, analysisArea), true);

            densityFw.write(String.format("simulation_%d\n", i));

            var particlesInRange = simulation.getOutputData().calculateParticlesInRange(simulation.getStates());

            for(int p : particlesInRange){
                densityFw.write(String.valueOf(p/analysisArea));
                densityFw.write('\n');
            }

            densityFw.close();
        }
    }
}
