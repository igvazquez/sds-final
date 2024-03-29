import lombok.Data;
import lombok.Value;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
public class OutputData {

    private final Map<Double, Integer> escapeData;
    private final Map<Double, Double> densityData;
    private final Board board;
    //private final FileWriter timesFw;
    //private final FileWriter densityFw;
    private final FileWriter particlesFw;
    private final double densityAnalysisY;
    private final int simulation;
    private final double spaceBetweenTurnstiles;



    public OutputData(final Board board, final int simulationNumber) throws IOException {
        this.simulation = simulationNumber;
        this.escapeData = new TreeMap<>();
        this.densityData = new TreeMap<>();
        this.board = board;
        this.densityAnalysisY = 0.25*board.getRealHeight();
        this.particlesFw = new FileWriter("particles.xyz");
        this.spaceBetweenTurnstiles = (board.getRealWidth() - board.getTurnstiles().size() * board.getDoorWidth()) / (board.getTurnstiles().size()+1);

    }

    /*public void writeTimesToFile() throws IOException {
        for(Map.Entry<Double, Integer> time : escapeData.entrySet()) {
            timesFw.write(time.getKey().toString() + ";" + time.getValue().toString() + ";" + simulation);
            timesFw.write('\n');
        }

        escapeData.clear();
    } */

    public static void writeSimulation(PedestrianSimulation simulation, String path) throws IOException {
        System.out.println("escribiendo simulacion");

        OutputData data = simulation.getOutputData();
        var analysisArea = data.getDensityAnalysisY() * simulation.getBoard().getRealWidth();

        var densityFw = new FileWriter(String.format(path+"decision_"+simulation.getDecisionMode()+"/density_sim=%d_A=%,.2f.csv",
                data.simulation, analysisArea), true);
        var timesFw = new FileWriter(String.format(path+"decision_"+simulation.getDecisionMode()+"/times_sim=%d.csv", data.simulation), true);

        densityFw.write("time;density\n");

        timesFw.write("time;escaped\n");

        System.out.println("escribiendo densidades simulacion " + data.simulation);
        for(Map.Entry<Double, Double> density : data.getDensityData().entrySet()){
            densityFw.write(density.getKey().toString() + ";" + density.getValue().toString());
            densityFw.write('\n');
        }

        densityFw.close();
        System.out.println("densidades listas");

        System.out.println("escribiendo tiempos simulacion " + data.simulation);
        for(Map.Entry<Double, Integer> time : data.getEscapeData().entrySet()) {
            timesFw.write(time.getKey().toString() + ";" + time.getValue().toString());
            timesFw.write('\n');
        }
        System.out.println("tiempos listos");

        timesFw.close();

        System.out.println("escritura simulacion " + data.simulation + " terminada");
    }

    /* public void closeFileWriters() throws IOException {
        timesFw.close();
        densityFw.close();
        particlesFw.close();
    } */

    public void writeBoardToFile(List<List<ParticleOutputData>> states) throws IOException {
        var dummyParticlesSize = (int) (8 + board.getTurnstiles().size()*(4+2*board.getQueueLength()/0.1) + (board.getTurnstiles().size()+2)*spaceBetweenTurnstiles/0.1 + 1);
        for(List<ParticleOutputData> particles : states) {
            particlesFw.write(String.valueOf(particles.size() + dummyParticlesSize));
            particlesFw.write('\n');
            particlesFw.write('\n');
            writeDummyParticles();
            for(OutputData.ParticleOutputData p : particles) {
                particlesFw.write(p.getId() + " " + p.getX() + " " + p.getY() + " " + p.getVx() + " " + p.getVy() + " " + p.getRadius() + " 255 255 0");
                particlesFw.write('\n');
            }
        }

        //writeDensityFile(states);
        states.clear();
    }

    private void writeDummyParticles() throws IOException {
        particlesFw.write("10001 0 0 0 0 0.0001 255 255 255");
        particlesFw.write('\n');
        particlesFw.write("10002 "+board.getL()+" 0 0 0 0.0001 255 255 255");
        particlesFw.write('\n');
        particlesFw.write("10003 0 "+board.getL()+" 0 0 0.0001 255 255 255");
        particlesFw.write('\n');
        particlesFw.write("10004 "+board.getL()+" "+board.getL()+" 0 0 0.0001 255 255 255");
        particlesFw.write('\n');
        particlesFw.write("10005 "+Board.getXPadding()+" "+Board.getYPadding()+" 0 0 0.05 255 255 255");
        particlesFw.write('\n');
        particlesFw.write("10006 "+(board.getL()-Board.getXPadding())+" "+Board.getYPadding()+" 0 0 0.05 255 255 255");
        particlesFw.write('\n');
        particlesFw.write("10007 "+Board.getXPadding()+" "+(board.getL()-Board.getYPadding())+" 0 0 0.05 255 255 255");
        particlesFw.write('\n');
        particlesFw.write("10008 "+(board.getL()-Board.getXPadding())+" "+(board.getL()-Board.getYPadding())+" 0 0 0.05 255 255 255");
        particlesFw.write('\n');

        double s = 0;
        while (s <= spaceBetweenTurnstiles) {
            particlesFw.write((-30000-2^100*3^(int)(s*10)) + " " + (Board.getXPadding()+s) +" "+(board.getTurnstiles().get(0).getY()+board.getQueueLength())+" 0 0 0.1 255 255 255");
            particlesFw.write('\n');
            s += 0.1;
        }

        for(int i = 0; i < board.getTurnstiles().size(); i++){
            var t = board.getTurnstiles().get(i);
            particlesFw.write((-1000-i)+ " " + t.getX() +" 0 0 0 0.1 255 255 255");
            particlesFw.write('\n');
            particlesFw.write((-2000-i)+ " " + t.getX() +" "+t.getY()+" 0 0 0.1 255 255 255");
            particlesFw.write('\n');
            particlesFw.write((-3000-i)+ " " + (t.getX()+t.getWidth()) +" 0 0 0 0.1 255 255 255");
            particlesFw.write('\n');
            particlesFw.write((-4000-i)+ " " + (t.getX()+t.getWidth()) +" "+t.getY()+" 0 0 0.1 255 255 255");
            particlesFw.write('\n');

            double j = 0;
            while (j <= board.getQueueLength()) {
                //System.out.println("queue wall i: "+i+" j: "+j);
                int k = (int) (j*10);
                particlesFw.write((-10000-2^i*3^k)+ " " + t.getX() +" "+(t.getY()+j)+" 0 0 0.1 255 255 255");
                particlesFw.write('\n');
                particlesFw.write((-20000-2^i*3^k)+ " " + (t.getX()+t.getWidth()) +" "+(t.getY()+j)+" 0 0 0.1 255 255 255");
                particlesFw.write('\n');
                j += 0.1;
            }

            s = 0;
            while (s <= spaceBetweenTurnstiles) {
                particlesFw.write((-30000-2^i*3^(int)(s*10))+ " " + (t.getX()+t.getWidth()+s) +" "+(t.getY()+j)+" 0 0 0.1 255 255 255");
                particlesFw.write('\n');
                s += 0.1;
            }


        }
    }

    /*private void writeDensityFile(List<List<ParticleOutputData>> states) throws IOException {
        var analysisArea = board.getRealWidth() * densityAnalysisY;

        var particlesInRange = calculateParticlesInRange(states);

        for(int p : particlesInRange){
            densityFw.write(String.valueOf(p/analysisArea));
            densityFw.write('\n');
        }
    } */

    public void addDensityEntry(double t, List<Particle> currentState, double width) {
        double analysisArea = densityAnalysisY * width;
        densityData.put(t, getParticlesInRange(currentState) / analysisArea);
    }

    private int getParticlesInRange(List<Particle> state) {
        return state.stream().map(p -> {
            if (p.getY() < densityAnalysisY){
                return 1;
            }else {
                return 0;
            }
        }).reduce(0, Integer::sum);
    }

    public List<Integer> calculateParticlesInRange(List<List<ParticleOutputData>> states){
        return states.stream().map(state -> state.stream().map(p -> {
            if (p.getY() < densityAnalysisY){
                return 1;
            }else {
                return 0;
            }
        }).reduce(0, Integer::sum)).collect(Collectors.toList());
    }

    public static ParticleOutputData particleOutput(final Particle particle) {
        return new ParticleOutputData(particle);
    }

    @Value
    public static class ParticleOutputData {
        int id;
        double x;
        double y;
        double vx;
        double vy;
        double radius;

        public ParticleOutputData(final Particle particle) {
            this.id = particle.getId();
            this.x = particle.getX();
            this.y = particle.getY();
            this.vx = particle.getVx();
            this.vy = particle.getVy();
            this.radius = particle.getRadius();
        }

    }
}
