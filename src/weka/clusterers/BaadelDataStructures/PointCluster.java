package weka.clusterers.BaadelDataStructures;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by suhel on 11/29/16.
 */
public class PointCluster {
    public final double[] k;
    public final double[] v;

    public PointCluster(double[] k, double[] v) {
        this.k = k;
        this.v = v;
    }

    @Override
    public String toString() {
        return new StringJoiner(",", PointCluster.class.toString(), "")
                .add("k:" + Arrays.toString(k))
                .add("v:" + Arrays.toString(v))
                .toString();
    }

    public static PointCluster of(double[] k, double[] v) {
        return new PointCluster(k, v);
    }

}


