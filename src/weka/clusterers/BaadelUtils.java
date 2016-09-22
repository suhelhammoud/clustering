package weka.clusterers;

import com.google.common.base.MoreObjects;

/**
 * Created by suhel on 9/22/16.
 */
public class BaadelUtils {
    public static void main(String[] args) {
        Pair<Double, Double> p = new Pair(3, 5);
        System.out.println(p);
    }
}

class Pair<T,K>{
    public  final T p1;
    public  final K p2;

    public Pair(T p1, K p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(p1).addValue(p2)
                .toString();
    }
}