import java.util.*;

public class CellIndexMethod {
    private final Board board;
    private final double rc;
    private final int M;
    private final boolean per;
    private final Map<Integer, Set<Particle>> neighboursMap;

    private static final int OUT_OF_BOUNDS = -1;

    public CellIndexMethod(Board board, double rc, boolean per) {
        this.board = board;
        this.per = per;
        M = board.getM();

//        if (rc < board.getL()/M){
//            throw new IllegalArgumentException("El radio de interaccion no puede ser mayor que L/M");
//        }

        this.rc = rc;

        neighboursMap = new HashMap<>(board.getParticles().size());
        for(Particle p : board.getParticles()) {
            neighboursMap.put(p.getId(), new HashSet<>());
        }
        /*for (int i = 0; i < board.getParticles().size(); i++) {
            neighboursMap.put(i, new HashSet<>());
        } */
    }

    private int getRightIndex(int currentCellIndex, int row, int col, boolean periodicOutline) {
        int rightCol;
        if(periodicOutline) {
            rightCol = Math.floorMod(currentCellIndex + 1, M);
            return rightCol + row*M;
        } else {
            rightCol = col + 1;
            return rightCol >= M ? OUT_OF_BOUNDS : rightCol + row*M;
        }
    }

    private int getUpperRightIndex(int row, int col, boolean periodicOutline) {
        int upperRightRow;
        int upperRightCol;

        if(periodicOutline) {
            upperRightRow = Math.floorMod(row - 1, M);
            upperRightCol = Math.floorMod(col + 1, M);
            return upperRightCol + upperRightRow*M;
        } else {
            upperRightCol = col + 1;
            upperRightRow = row - 1;
            return (upperRightRow < 0 || upperRightCol >= M) ? OUT_OF_BOUNDS : upperRightCol + upperRightRow*M;
        }
    }

    private int getLowerRightIndex(int row, int col, boolean periodicOutline) {
        int lowerRightRow;
        int lowerRightCol;

        if(periodicOutline) {
            lowerRightRow = Math.floorMod(row + 1, M);
            lowerRightCol = Math.floorMod(col + 1, M);
            return lowerRightCol + lowerRightRow*M;
        } else {
            lowerRightRow = row + 1;
            lowerRightCol = col + 1;
            return (lowerRightRow >= M || lowerRightCol >= M) ? OUT_OF_BOUNDS : lowerRightCol + lowerRightRow*M;
        }
    }

    private int getLowerIndex(int row, int col, boolean periodicOutline) {
        int lowerRow;

        if(periodicOutline) {
            lowerRow = Math.floorMod(row + 1, M);
            return col + lowerRow*M;
        } else {
            lowerRow = row + 1;
            return lowerRow >= M ? OUT_OF_BOUNDS : col + lowerRow*M;
        }
    }

    private void addNeighboursToCells(int currentIdx, int neighbourIdx) {
        if(neighbourIdx != OUT_OF_BOUNDS) {
            List<Particle> currentCell = board.getCell(currentIdx);
            for(Particle p : currentCell){
                for(Particle n : board.getCell(neighbourIdx)){
                    if (p.calculateDistance(n, board.getL(), per) < rc){
                        neighboursMap.get(p.getId()).add(n);
                        neighboursMap.get(n.getId()).add(p);
                    }
                }
            }
        }
    }

    public void calculateNeighbours(){
        for (int i=0; i<M*M; i++) {
            int row = i/M;
            int col = i%M;
            //add this cell particles as neighbours
            addNeighboursToCells(i, i);
            //add right neighbours
            addNeighboursToCells(i, getRightIndex(i, row, col, per));
            //add upper right neighbours
            addNeighboursToCells(i, getUpperRightIndex(row, col, per));
            //add lower right neighbours
            addNeighboursToCells(i, getLowerRightIndex(row, col, per));
            //add lower neighbours
            addNeighboursToCells(i, getLowerIndex(row, col, per));
        }
//        return neighboursMap;
    }

    public Map<Integer, Set<Particle>> getNeighboursMap() {
        return neighboursMap;
    }

    public void calculateNeighboursBrute() {
        for(Particle p : board.getParticles()) {
            for(Particle n : board.getParticles()) {
                if(p.collides(n)) {
                    neighboursMap.get(p.getId()).add(n);
                }
            }
        }
    }

    public Board getBoard() {
        return board;
    }
}