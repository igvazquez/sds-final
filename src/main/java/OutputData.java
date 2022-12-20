import lombok.Value;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class OutputData {

    private final Map<Double, Integer> escapeData;

    public OutputData() {
        this.escapeData = new TreeMap<>();
    }

    public Map<Double, Integer> getEscapeData() {
        return escapeData;
    }

    public void writeTimesToFile(boolean isFirstWrite) throws IOException {
        if(isFirstWrite) {
            try {
                Files.delete(Paths.get("times.csv"));
            } catch (Exception e) {

            }
        }
        FileWriter pos = new FileWriter("times.csv", true);
        BufferedWriter buffer = new BufferedWriter(pos);
        if(isFirstWrite) {
            buffer.write("time;escaped_particles");
            buffer.newLine();
        }

        for(Map.Entry<Double, Integer> time : escapeData.entrySet()) {
            buffer.write(time.getKey().toString() + ";" + time.getValue().toString());
            buffer.newLine();
        }
        buffer.flush();
        buffer.close();
        pos.close();
        escapeData.clear();
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
