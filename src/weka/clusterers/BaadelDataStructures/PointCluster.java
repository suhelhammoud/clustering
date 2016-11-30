package weka.clusterers.BaadelDataStructures;

import com.google.common.base.MoreObjects;
import weka.clusterers.PairArray;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

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
        return MoreObjects.toStringHelper(PairArray.class)
                .add("k:", Arrays.toString(k))
                .add("v:", Arrays.toString(v))
                .toString();

    }

    public static PointCluster of(double[] k, double[] v) {
        return new PointCluster(k, v);
    }

}


