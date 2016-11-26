package weka.clusterers;

import java.util.Arrays;

/**
 * Created by suhel on 11/22/16.
 */

public class PairIndexArray {
    final int centroidIndex;
    final double[] v;

    public PairIndexArray(int centroidIndex, double[] v) {
        this.centroidIndex = centroidIndex;
        this.v = v;
    }

    @Override
    public String toString() {
        return "pair(" + centroidIndex + ", " + Arrays.toString(v) + ")";
    }

    public static PairIndexArray of(PairArray pair) {
        return new PairIndexArray(Point.minIndex(pair.v), pair.k);
    }
}
