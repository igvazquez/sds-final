import lombok.Data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class PedestrianSimulation {

    private List<List<OutputData.ParticleOutputData>> states = new LinkedList<>();
    private OutputData outputData = new OutputData();
    private final Board board;
    private final int startParticles;
    private final String decisionMode;
    private final double rc;
    private final double beta;
    private final double tau;
    private double t;

    public PedestrianSimulation(final Board board, final double rc, final double beta, final double tau, final String decisionMode) {
        this.board = board;
        this.decisionMode = decisionMode;
        this.rc = rc;
        this.beta = beta;
        this.tau = tau;
        this.t = 0;
        this.startParticles = board.getParticles().size();
    }

    public void simulate(final int maxIter, final boolean logToFile) throws IOException {
        List<Particle> currentState = board.getParticles();
        states.add(currentState.stream().map(OutputData::particleOutput).collect(Collectors.toList()));
        CellIndexMethod cim;

        //if(!decisionMode.equals("distance"))
        //    board.assignParticles();

        int i = 0;
        try {
            while (!currentState.isEmpty() && i < maxIter) {
                if(i % 5000 == 0) {
                    System.out.println("Iter: " + i);
                    writeBoardToFile(i == 0);
                    outputData.writeTimesToFile(i == 0);
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
                writeBoardToFile(i < 5000);
                outputData.writeTimesToFile(i < 5000);
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

    private void writeBoardToFile(boolean isFirstWrite) throws IOException {
        if(isFirstWrite) {
            try {
                Files.delete(Paths.get("testBoard.xyz"));
            } catch (Exception e) {

            }
        }
        FileWriter pos = new FileWriter("testBoard.xyz", true);
        BufferedWriter buffer = new BufferedWriter(pos);
        var dummyParticlesSize = 8 + board.getTurnstiles().size()*4;
        for(List<OutputData.ParticleOutputData> particles : states) {
            buffer.write(String.valueOf(particles.size() + dummyParticlesSize));
            buffer.newLine();
            buffer.newLine();
            writeDummyParticles(buffer);
            for(OutputData.ParticleOutputData p : particles) {
                buffer.write(p.getId() + " " + p.getX() + " " + p.getY() + " " + p.getVx() + " " + p.getVy() + " " + p.getRadius());
                buffer.newLine();
            }
        }
        buffer.flush();
        buffer.close();
        pos.close();
        states.clear();
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
