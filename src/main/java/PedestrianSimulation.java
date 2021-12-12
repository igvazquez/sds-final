import lombok.Data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class PedestrianSimulation {

    private List<List<OutputData>> states = new LinkedList<>();
    private final Board board;
    private final double rc;
    private final double beta;
    private final double tau;
    private double t;

    public PedestrianSimulation(final Board board, final double rc, final double beta, final double tau) {
        this.board = board;
        this.rc = rc;
        this.beta = beta;
        this.tau = tau;
        this.t = 0;
    }

    public void simulate(final int maxIter, final boolean logToFile) throws IOException {
        List<Particle> currentState = board.getParticles();
        states.add(currentState.stream().map(OutputData::new).collect(Collectors.toList()));
        CellIndexMethod cim;

        int i = 0;
        try {
            while (!currentState.isEmpty() && i < maxIter) {
                System.out.println("Iter: " + i);
                cim = new CellIndexMethod(board, board.getMaxR(), false);
                cim.calculateNeighbours();
                currentState = doStep(currentState, cim);
                board.updateParticles(currentState);
                states.add(currentState.stream().map(OutputData::new).collect(Collectors.toList()));
                i++;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("exploto en " + i);
            System.out.println(e.getMessage());
        } finally {
            if (logToFile){
                writeBoardToFile();
            }
        }
    }

    private List<Particle> doStep(final List<Particle> currentState, final CellIndexMethod cim) {
        this.t += board.getDt();
        List<Particle> nextState = new ArrayList<>(currentState.size());
        Map<Integer, Set<Particle>> neighbours = cim.getNeighboursMap();
        for (Particle p : currentState) {
            // Particle newParticle = board.advanceParticle(p, neighbours.get(p.getId()));
            p.advanceParticle(t, board.getDt(), neighbours.get(p.getId()));

            if(p.getY() > 0) {
                nextState.add(p);
            }
        }
        return nextState;
    }

    private void writeBoardToFile() throws IOException {
        FileWriter pos = new FileWriter("testBoard.xyz", false);
        BufferedWriter buffer = new BufferedWriter(pos);
        for(List<OutputData> particles : states) {
            buffer.write(String.valueOf(particles.size() + 12));
            buffer.newLine();
            buffer.newLine();
            writeDummyParticles(buffer);
            for(OutputData p : particles) {
                buffer.write(p.getId() + " " + p.getX() + " " + p.getY() + " " + p.getVx() + " " + p.getVy() + " " + p.getRadius());
                buffer.newLine();
            }
        }
        buffer.flush();
        buffer.close();
        pos.close();
    }

    private void writeDummyParticles(final BufferedWriter buffer) throws IOException {
        buffer.write("201 0 0 0 0 0.0001");
        buffer.newLine();
        buffer.write("202 20 0 0 0 0.0001");
        buffer.newLine();
        buffer.write("203 0 20 0 0 0.0001");
        buffer.newLine();
        buffer.write("204 20 20 0 0 0.0001");
        buffer.newLine();
        buffer.write("214 5 18 0 0 0.05");
        buffer.newLine();
        buffer.write("215 15 18 0 0 0.05");
        buffer.newLine();
        buffer.write("216 5 2 0 0 0.05");
        buffer.newLine();
        buffer.write("217 15 2 0 0 0.05");
        buffer.newLine();
        buffer.write("205 "+ ((board.getL()/2) - this.board.getDoorWidth()/2) +" 0 0 0 0.1");
        buffer.newLine();
        buffer.write("206 "+(board.getL()/2 + this.board.getDoorWidth()/2)+" 0 0 0 0.1");
        buffer.newLine();
        buffer.write("207 "+((board.getL()/2) - this.board.getDoorWidth()/2)+" 2 0 0 0.1");
        buffer.newLine();
        buffer.write("208 "+(board.getL()/2 + this.board.getDoorWidth()/2)+" 2 0 0 0.1");
        buffer.newLine();
    }
}
