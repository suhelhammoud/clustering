package weka.clusterers;


import java.util.Arrays;
import java.util.StringJoiner;

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
        return new StringJoiner(",", this.getClass().getName() + "{","}")
                .add("p:" + Arrays.toString(k))
                .add("d:" + Arrays.toString(v))
                .toString();
    }

    public static PairArray of(double[] k, double[] v) {
        return new PairArray(k, v);
    }
}
