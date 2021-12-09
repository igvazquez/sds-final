import lombok.Data;

@Data
public class State {

    private final double r;
    private final double v;
    private final double dt;

    public State(final double r, final double v, final double dt) {
        this.r = r;
        this.v = v;
        this.dt = dt;
    }
}