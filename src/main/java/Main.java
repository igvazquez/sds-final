import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        var p1 = new Particle(1, 0, 1, 0.7, 0, 0.7, new double[]{5, 8}, 60, 0.5);
        var p2 = new Particle(2, 0, 7, 0.7, 0, 0.7, new double[]{5, 0}, 60, 0.5);
        var sfm = new SFM(1.2E5, 2.4E5, 2000, 0.08, 0.5);

        p1.setIntegrator(new Verlet(p1, sfm));
        p2.setIntegrator(new Verlet(p2, sfm));

        var particles = List.of(p1, p2);
        var neighbours = new HashMap<Particle, List<Particle>>();
        neighbours.put(p1, List.of(p2));
        neighbours.put(p2, List.of(p1));
        var dt = 0.1;
        var t = 0.0;

        FileWriter pos = new FileWriter("board.xyz", false);
        BufferedWriter buffer = new BufferedWriter(pos);
        for (int i = 1; i < 100; i++) {
            buffer.write(String.valueOf(particles.size()+1));
            buffer.newLine();
            buffer.newLine();
            for(final Particle p : particles){
                System.out.println(p);
                buffer.write(p.getId() + " " + p.getX() + " " + p.getY() + " " + p.getVx() + " " + p.getVy() + " " + p.getRadius());
                buffer.newLine();
                p.advanceParticle(t, dt, neighbours.get(p));
                System.out.println(p);
            }
            buffer.write(0 + " " + 2 + " " + 5 + " " + 0 + " " + 0 + " " + 0.1);
            buffer.newLine();
            t += i*dt;
        }
        buffer.close();
    }
}
