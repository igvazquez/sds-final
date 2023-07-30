import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class PedestrianSimulation {

    private List<List<OutputData.ParticleOutputData>> states = new LinkedList<>();
    private OutputData outputData;
    private final Board board;
    private final int startParticles;
    private final String decisionMode;
    private final double rc;
    private final double beta;
    private final double tau;
    private double t;

    public PedestrianSimulation(final Board board, final double rc, final double beta, final double tau, final String decisionMode) throws IOException {
        this.board = board;
        this.decisionMode = decisionMode;
        this.rc = rc;
        this.beta = beta;
        this.tau = tau;
        this.t = 0;
        this.startParticles = board.getParticles().size();
        this.outputData = new OutputData(board);
    }

    public void simulate(final int maxIter, final boolean logToFile) throws IOException {
        List<Particle> currentState = board.getParticles();
        states.add(currentState.stream().map(OutputData::particleOutput).collect(Collectors.toList()));
        CellIndexMethod cim;

        int i = 0;
        try {
            while (!currentState.isEmpty() && i < maxIter) {
                if(i % 5000 == 0) {
                    System.out.println("Iter: " + i + " Particles Left: " + board.getParticles().size());
                    if (logToFile){
                        outputData.writeBoardToFile(states);
                        outputData.writeTimesToFile();
                    }
                }
                cim = new CellIndexMethod(board, board.getMaxR(), false);
                //cim.calculateNeighbours();
                cim.calculateNeighborsBrute();
                currentState = doStep(currentState, cim);
                board.assignTurnstiles(decisionMode);
                board.updateTurnstiles(t);
                board.updateParticles(currentState);
                states.add(currentState.stream().map(OutputData::particleOutput).collect(Collectors.toList()));
                i++;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("exploto en " + i + " a t=" + t);
            System.out.println(e.getMessage());
        } finally {
            if (logToFile){
                outputData.writeBoardToFile(states);
                outputData.writeTimesToFile();
                outputData.closeFileWriters();
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
        outputData.getEscapeData().put(this.t, startParticles - nextState.size());
        return nextState;
    }
}
