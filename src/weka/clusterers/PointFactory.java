package weka.clusterers;

import java.util.function.Supplier;

/**
 * Created by suhel on 11/25/16.
 */
class PointFactory implements Supplier<Point> {
    final public int dimension;

    public PointFactory(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public Point get() {
        System.out.println("PointFactory.get");
        return new Point(new double[dimension], 0.0);
    }
}
