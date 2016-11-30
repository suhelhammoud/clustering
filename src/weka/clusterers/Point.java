package weka.clusterers;

import com.google.common.base.MoreObjects;
import weka.clusterers.BaadelDataStructures.PointCluster;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by suhel on 11/16/16.
 */


public class Point {

    public static Point of(double... v) {
        return new Point(v, 1.0);

    }

    public static Point of(PointCluster pointCluster) {
        Point result = new Point(pointCluster.k, 1.0);
        result.setClusterIndex(minIndex(pointCluster.v));
        return result;
    }
//    public static Point of(double[] v) {
//        return new Point(v, 1.0);
//    }

    public static Point of(PairArray pairArray) {
        Point result = new Point(pairArray.k, 1.0);
        result.setClusterIndex(pairArray.v);
        return result;
    }

    public static int closestPoint(List<Point> centroids, Point point) {
        double[] distances = distancesSquared(centroids, point);
        //TODO return minIndex(v(centroids, points));
        return minIndex(distances);
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

    public static double mDistance(double[] a, double[] b) {
        assert a.length == b.length;
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = Math.abs(a[i] - b[i]);
            result += diff;
        }
        return result;
    }

    public static double eDistanceSquared(double[] a, double[] b) {
        assert a.length == b.length;
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            result += diff * diff;
        }
        return result;
    }

    public static double[] distances(double[] dPoint,
                                     List<double[]> dCentroids,
                                     BiFunction<double[], double[], Double> distanceF) {
        return dCentroids.stream()
                .mapToDouble(e -> distanceF.apply(dPoint, e))
                .toArray();
    }

    public static double eDistanceSquared(Point p1, Point p2) {
        return eDistanceSquared(p1.v, p2.v);
    }


    public static double[] distancesSquared(List<Point> centroids, double[] xyz) {
        return centroids.stream().mapToDouble(e -> eDistanceSquared(e.v, xyz))
                .toArray();
    }

    public static double[] distancesSquared(List<Point> centroids, Point point) {
        return centroids.stream()
                .mapToDouble(e -> eDistanceSquared(e, point))
                .toArray();
    }

    final private double[] v;
    private double weight;
    private int clusterIndex;


    public int getClusterIndex() {
        return clusterIndex;
    }

    public void setClusterIndex(double[] distances) {
        setClusterIndex(minIndex(distances));
    }

    public void setClusterIndex(int clusterIndex) {
        this.clusterIndex = clusterIndex;
    }


    public static Point getVNormalized(Point point) {
        System.out.println("getVNormalized point = [" + point + "]");
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

    public int closestPoint(List<Point> centroids) {
        return closestPoint(centroids, this);
    }

    public double getWeight() {
        return weight;
    }

    public double[] getV() {
        return v;
    }

    public Point(double[] v, double weight) {
//        this.v = Arrays.copyOf(v, v.length);
        this.v = v;//TODO check weather defensive copy is needed
        this.weight = weight;
    }

    public Point clone() {
        return new Point(Arrays.copyOf(v, v.length), weight);
    }

    double get(int i) {
        return v[i];
    }



    public void accumulate(Point that) {
        System.out.println("accumulate  centroid : " + clusterIndex + " = [" + that + "]");
        for (int i = 0; i < v.length; i++) {
            this.v[i] += that.v[i];
        }
        this.weight += that.weight;
    }

    public static Point combine(Point p1, Point p2) {
        System.out.println("combine p1 = [" + p1 + "], p2 = [" + p2 + "]");
        Point result = p1.clone();
        result.accumulate(p2);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Point.class)
                .add("w", weight)
                .add("v", Arrays.toString(v))
                .add("cIndex", clusterIndex)
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
}


