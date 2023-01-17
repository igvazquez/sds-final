import lombok.Value;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OutputData {

    private final Map<Double, Integer> escapeData;
    private final Board board;
    private final FileWriter timesFw;
    private final FileWriter densityFw;
    private final FileWriter particlesFw;
    private final double densityAnalysisY;


    public OutputData(final Board board) throws IOException {
        this.escapeData = new TreeMap<>();
        this.board = board;
        this.densityAnalysisY = 0.25*board.getRealHeight();
        this.timesFw = new FileWriter(String.format("timesN=%d.csv", board.getParticles().size()));
        this.densityFw = new FileWriter(String.format("density_A=%,.2f.csv", densityAnalysisY*board.getRealWidth()));
        this.particlesFw = new FileWriter("particles.xyz");

        timesFw.write("time;escaped_particles");
        timesFw.write('\n');

        densityFw.write("density");
        densityFw.write('\n');
    }

    public Map<Double, Integer> getEscapeData() {
        return escapeData;
    }

    public void writeTimesToFile() throws IOException {
        for(Map.Entry<Double, Integer> time : escapeData.entrySet()) {
            timesFw.write(time.getKey().toString() + ";" + time.getValue().toString());
            timesFw.write('\n');
        }

        escapeData.clear();
    }

    public void closeFileWriter() throws IOException {
        timesFw.close();
        densityFw.close();
        particlesFw.close();
    }

    public void writeBoardToFile(List<List<ParticleOutputData>> states) throws IOException {
        var dummyParticlesSize = 8 + board.getTurnstiles().size()*4;
        for(List<ParticleOutputData> particles : states) {
            particlesFw.write(String.valueOf(particles.size() + dummyParticlesSize));
            particlesFw.write('\n');
            particlesFw.write('\n');
            writeDummyParticles();
            for(OutputData.ParticleOutputData p : particles) {
                particlesFw.write(p.getId() + " " + p.getX() + " " + p.getY() + " " + p.getVx() + " " + p.getVy() + " " + p.getRadius());
                particlesFw.write('\n');
            }
        }

        writeDensityFile(states);
        states.clear();
    }

    private void writeDummyParticles() throws IOException {
        particlesFw.write("201 0 0 0 0 0.0001");
        particlesFw.write('\n');
        particlesFw.write("202 "+board.getL()+" 0 0 0 0.0001");
        particlesFw.write('\n');
        particlesFw.write("203 0 "+board.getL()+" 0 0 0.0001");
        particlesFw.write('\n');
        particlesFw.write("204 "+board.getL()+" "+board.getL()+" 0 0 0.0001");
        particlesFw.write('\n');
        particlesFw.write("214 "+Board.getXPadding()+" "+Board.getYPadding()+" 0 0 0.05");
        particlesFw.write('\n');
        particlesFw.write("215 "+(board.getL()-Board.getXPadding())+" "+Board.getYPadding()+" 0 0 0.05");
        particlesFw.write('\n');
        particlesFw.write("216 "+Board.getXPadding()+" "+(board.getL()-Board.getYPadding())+" 0 0 0.05");
        particlesFw.write('\n');
        particlesFw.write("217 "+(board.getL()-Board.getXPadding())+" "+(board.getL()-Board.getYPadding())+" 0 0 0.05");
        particlesFw.write('\n');

        for(int i = 0; i < board.getTurnstiles().size(); i++){
            var t = board.getTurnstiles().get(i);
            particlesFw.write((-1-4*i)+ " " + t.getX() +" 0 0 0 0.1");
            particlesFw.write('\n');
            particlesFw.write((-2-4*i)+ " " + t.getX() +" "+t.getY()+" 0 0 0.1");
            particlesFw.write('\n');
            particlesFw.write((-3-4*i)+ " " + (t.getX()+t.getWidth()) +" 0 0 0 0.1");
            particlesFw.write('\n');
            particlesFw.write((-4-4*i)+ " " + (t.getX()+t.getWidth()) +" "+t.getY()+" 0 0 0.1");
            particlesFw.write('\n');
        }
    }

    private void writeDensityFile(List<List<ParticleOutputData>> states) throws IOException {
        var analysisArea = board.getRealWidth() * densityAnalysisY;

        var particlesInRange = states.stream().map(state -> state.stream().map(p -> {
            if (p.getY() < densityAnalysisY){
                return 1;
            }else {
                return 0;
            }
        }).reduce(0, Integer::sum)).collect(Collectors.toList());

        for(int p : particlesInRange){
            densityFw.write(String.valueOf(p/analysisArea));
            densityFw.write('\n');
        }
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
