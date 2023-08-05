import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class DensityAnalysis {

    public static void main(String[] args) throws IOException {

        var simulations = 3;
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

            PedestrianSimulation simulation = new PedestrianSimulation(board, maxR, beta, tau, "else");
            completed = simulation.simulate(iterations, false);

            if(completed) {
                var analysisArea = simulation.getOutputData().getDensityAnalysisY()*board.getRealWidth();

                var densityFw = new FileWriter(String.format("density_sims=%d_A=%,.2f.csv",
                        simulations, analysisArea), true);

                var timesFw = new FileWriter(String.format("times_sims=%d.csv", simulations), true);

                densityFw.write(String.format("simulation_%d\n", i));

                timesFw.write(String.format("simulation_%d;0\n", i));

                var particlesInRange = simulation.getOutputData().calculateParticlesInRange(simulation.getStates());

                for(int p : particlesInRange){
                    densityFw.write(String.valueOf(p/analysisArea));
                    densityFw.write('\n');
                }

                densityFw.close();

                for(Map.Entry<Double, Integer> time : simulation.getOutputData().getEscapeData().entrySet()) {
                    timesFw.write(time.getKey().toString() + ";" + time.getValue().toString());
                    timesFw.write('\n');
                }

                timesFw.close();
            } else {
                broken++;
                i--;
            }

        }
        System.out.println("Broken simulations: " + broken);
    }
}
