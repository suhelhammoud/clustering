package weka.clusterers;

import com.google.common.base.MoreObjects;

import java.util.Arrays;

/**
 * Created by suhel on 11/25/16.
 */
public class PairArray {
    public final double[] k;
    public final double[] v;

    public PairArray(double[] k, double[] v) {
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

    public static PairArray of(double[] k, double[] v) {
        return new PairArray(k, v);
    }
}
