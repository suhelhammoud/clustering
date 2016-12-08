package weka.clusterers.BaadelDataStructures;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by suhel on 11/29/16.
 */
public class PointDistance {

    public final double[] p;
    public final double[] d;

    public PointDistance(double[] p, double[] d) {
        this.p = p;
        this.d = d;
    }


    @Override
    public String toString() {
        return new StringJoiner(",", PointDistance.class.toString(), "")
                .add("p:" + Arrays.toString(p))
                .add("d:" + Arrays.toString(d))
                .toString();
    }

    public static PointDistance of(double[] k, double[] v) {
        return new PointDistance(k, v);
    }

}

class PointClusterIdentity{
    public final int dimension;

    PointClusterIdentity(int dimension) {
        this.dimension = dimension;
    }


}
