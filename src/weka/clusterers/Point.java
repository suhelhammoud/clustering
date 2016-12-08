package weka.clusterers;

import weka.clusterers.BaadelDataStructures.PointDistance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiFunction;

/**
 * Created by suhel on 11/16/16.
 */


public class Point {
    /* coordinates */
    final private double[] v;

    private double weight;

    private int clusterIndex;

    /* Main constructor */
    public Point(double[] v, double weight) {
        this.v = v;//TODO check weather defensive copy is needed
        this.weight = weight;
    }

    public static Point of(double... v) {
        return new Point(v, 1.0);
    }

    public static Point of(PointDistance pointDistance) {
        Point result = new Point(pointDistance.p, 1.0);
        result.setClusterIndex(minIndex(pointDistance.d));
        return result;
    }
    public static Point ofDist(PointDistance pointDistance) {
        Point result = new Point(pointDistance.d, 1.0);
        result.setClusterIndex(minIndex(pointDistance.d));
        return result;
    }



    public Point clone() {
        return new Point(Arrays.copyOf(v, v.length), weight);
    }

    /**
     * Calculate manhattan distance between two points
     * @param a coordinates array
     * @param b coordinates array
     * @return manhattan distance
     */
    public static double mDistance(double[] a, double[] b) {
        assert a.length == b.length;
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = Math.abs(a[i] - b[i]);
            result += diff;
        }
        return result;
    }

    /**
     * Calculate euclidean between two points
     * @param a coordinates array
     * @param b coordinates array
     * @return euclidean distance
     */
    public static double eDistanceSquared(double[] a, double[] b) {
        assert a.length == b.length;
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            result += diff * diff;
        }
        return result;
    }

    /**
     * Calculate the distances between one point and a group of centroids, based on distance function
     * @param dPoint
     * @param dCentroids
     * @param distanceF
     * @return
     */
    public static double[] distances(double[] dPoint,
                                     List<double[]> dCentroids,
                                     BiFunction<double[], double[], Double> distanceF) {
        return dCentroids.stream()
                .filter(e -> e.length != 0) //TODO remove filter later
                .mapToDouble(e -> distanceF.apply(dPoint, e))
                .toArray();
    }

    public int getClusterIndex() {
        return clusterIndex;
    }

    public void setClusterIndex(int clusterIndex) {
        this.clusterIndex = clusterIndex;
    }

    /**
     * Divide the accumulated coordinates by the weight
     * @param point
     * @return point with divided coordinates but with the same old weight
     */
    public static Point getVNormalized(Point point) {
//        System.out.println("getVNormalized point = [" + point + "]");
        if (point.weight == 1.0) {
            return point;
        }
        double[] v = new double[point.v.length];
        for (int i = 0; i < v.length; i++) {
            v[i] = point.v[i] / point.weight;
        }
        Point result = Point.of(v);
        result.weight = point.weight;
        return result;
    }

    public double getWeight() {
        return weight;
    }

    public double[] getV() {
        return v;
    }

    public void accumulate(Point that) {
        //System.out.println("accumulate  centroid : " + clusterIndex + " = [" + that + "]");
        for (int i = 0; i < v.length; i++) {
            this.v[i] += that.v[i];
        }
        this.weight += that.weight;
    }

    public static Point combine(Point p1, Point p2) {
//        System.out.println("combine p1 = [" + p1 + "], p2 = [" + p2 + "]");
        Point result = p1.clone();
        result.accumulate(p2);
        return result;
    }

    public static List<String> formated(double[] dPoint) {
        List<String> result = new ArrayList<>(dPoint.length);
        for (double x : dPoint) {
            result.add(String.format("%4.4f", x));
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Point.class.toString(), "")
                .add("w:" + weight)
                .add("d:" + Arrays.toString(v))
                .add("cIndex" + clusterIndex)
                .toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(v);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Point))
            return false;
        Point that = (Point) obj;
        return Arrays.equals(this.v, that.v);
    }

    public static int minIndex(double[] m) {
        int result = -1;
        double min = Double.MAX_VALUE;

        for (int i = 0; i < m.length; i++) {
            if (m[i] < min) {
                min = m[i];
                result = i;
            }

        }
        return result;
    }

    public static double[] add(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    public static double[] sumDPoints(List<double[]> points, int dimension) {
        return points.stream()
                .reduce(new double[dimension], Point::add);
    }

}


