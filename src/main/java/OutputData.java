import lombok.Value;

@Value
public class OutputData {

    int id;
    double x;
    double y;
    double vx;
    double vy;
    double radius;

    public OutputData(final Particle particle) {
        this.id = particle.getId();
        this.x = particle.getX();
        this.y = particle.getY();
        this.vx = particle.getVx();
        this.vy = particle.getVy();
        this.radius = particle.getRadius();
    }
}
