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
                board.updateTurnstiles(t);
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
        var dummyParticlesSize = 8 + board.getTurnstiles().size()*4;
        for(List<OutputData> particles : states) {
            buffer.write(String.valueOf(particles.size() + dummyParticlesSize));
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
        buffer.write("202 "+board.getL()+" 0 0 0 0.0001");
        buffer.newLine();
        buffer.write("203 0 "+board.getL()+" 0 0 0.0001");
        buffer.newLine();
        buffer.write("204 "+board.getL()+" "+board.getL()+" 0 0 0.0001");
        buffer.newLine();
        buffer.write("214 "+Board.getXPadding()+" "+Board.getYPadding()+" 0 0 0.05");
        buffer.newLine();
        buffer.write("215 "+(board.getL()-Board.getXPadding())+" "+Board.getYPadding()+" 0 0 0.05");
        buffer.newLine();
        buffer.write("216 "+Board.getXPadding()+" "+(board.getL()-Board.getYPadding())+" 0 0 0.05");
        buffer.newLine();
        buffer.write("217 "+(board.getL()-Board.getXPadding())+" "+(board.getL()-Board.getYPadding())+" 0 0 0.05");
        buffer.newLine();

        for(int i = 0; i < board.getTurnstiles().size(); i++){
            var t = board.getTurnstiles().get(i);
            buffer.write((-1-4*i)+ " " + t.getX() +" 0 0 0 0.1");
            buffer.newLine();
            buffer.write((-2-4*i)+ " " + t.getX() +" "+t.getY()+" 0 0 0.1");
            buffer.newLine();
            buffer.write((-3-4*i)+ " " + (t.getX()+t.getWidth()) +" 0 0 0 0.1");
            buffer.newLine();
            buffer.write((-4-4*i)+ " " + (t.getX()+t.getWidth()) +" "+t.getY()+" 0 0 0.1");
            buffer.newLine();
        }
    }
}
