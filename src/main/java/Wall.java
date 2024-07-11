public class Wall {
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;

    public Wall(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public double[] distanceAgentToWall(double[] point) {
        double[] p0 = {startX, startY};
        double[] p1 = {endX, endY};
        double[] d = {p1[0] - p0[0], p1[1] - p0[1]};
        double[] ymp0 = {point[0] - p0[0], point[1] - p0[1]};
        double t = SFM.dotProduct(d, ymp0) / SFM.dotProduct(d, d);
        double dist;
        double[] cross;

        if (t <= 0.0) {
            dist = Math.sqrt(SFM.dotProduct(ymp0, ymp0));
            cross = new double[]{p0[0] + t * d[0], p0[1] + t * d[1]};
        } else if (t >= 1.0) {
            double[] ymp1 = {point[0] - p1[0], point[1] - p1[1]};
            dist = Math.sqrt(SFM.dotProduct(ymp1, ymp1));
            cross = new double[]{p0[0] + t * d[0], p0[1] + t * d[1]};
        } else {
            cross = new double[]{p0[0] + t * d[0], p0[1] + t * d[1]};
            dist = Math.sqrt(Math.pow(cross[0] - point[0], 2) + Math.pow(cross[1] - point[1], 2));
        }

        double[] npw = SFM.normalize(new double[]{cross[0] - point[0], cross[1] - point[1]});
        return new double[]{dist, npw[0], npw[1]};
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }
}
