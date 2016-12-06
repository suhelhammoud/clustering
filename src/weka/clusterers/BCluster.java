package weka.clusterers;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by suhel on 10/7/16.
 */

public class BCluster {


    public static boolean equalLists(List<Point> oCentroids, List<Point> nCentroids) {
        if (oCentroids.size() != nCentroids.size())
            return false;

        for (int i = 0; i < oCentroids.size(); i++) {
            Point point = oCentroids.get(i);
            if (!point.equals(nCentroids.get(i)))
                return false;
        }
        return true;
    }

    ;

    int dimensions = 4;
    int numClusters = 3;
    int maxIteration = 10;

//    public void buildClassifier(List<double[]> pointsArray) {
//        PointFactory pointFactory = new PointFactory(dimensions);
//        PointCollector pointCollector = new PointCollector(dimensions);
//
////        List<double[]> pointsArray = getPointsDouble();
//
//        List<Point> centroids = UtilCluster.sample(pointsArray, numClusters).stream()
//                .map(Point::of)
//                .collect(toList());
//
//        //point.v[]  distatnces[]
//        List<PairArray> xyzDistances = pointsArray.stream()
//                .map(e -> PairArray.of(e, Point.distancesSquared(centroids, e)))
//                .collect(toList());
//
//
//        Map<Integer, Point> newCentoids = xyzDistances.stream()
//                .map(Point::of) // clusterIndex => Point
//                .collect(groupingBy(Point::getClusterIndex, pointCollector));
//
//        newCentoids.entrySet().stream()
//                .forEach(System.out::println);
//
//    }

    public static <T> boolean isSame(final List<T> lst1, final List<T> lst2) {
        if(lst1 == null || lst2 == null) return false;
        /*System.out.println("lst1 = [" + lst1 + "], lst2 = [" + lst2 + "]");
        if (lst1.size() != lst2.size())
            return false;

        for (int i = 0; i < lst1.size(); i++) {
            T i1 = lst1.get(i);
            T i2 = lst2.get(i);
            System.out.println("i1 = " + i1 + ", i2 = " + i2);
            boolean isEqual = i1.equals(i2);
            System.out.println("isEqual = " + isEqual);
            if(!isEqual)
                return false;
        }
        return true;*/

        return IntStream.range(0, lst1.size())
                .allMatch(i -> lst1.get(i)
                        .equals(lst2.get(i)));
    }


    public static void main(String[] args) {
        System.out.println("Start");
        int dimension = 4;
        int numClusters = 3;
//
////        PointFactory pointFactory = new PointFactory(dimension);
//        PointCollector pointCollector = new PointCollector(dimension);
//
//        List<double[]> pointsArray = BClusterTest.getPointsDouble();
//
//        List<Point> centroids = UtilCluster.sample(pointsArray, numClusters).stream()
//                .map(Point::of)
//                .collect(toList());
//        //point.v[]  ~ distatnces[]
//        List<PairArray> xyzDistances = pointsArray.stream()
//                .map(e -> PairArray.of(e, Point.distancesSquared(centroids, e)))
//                .collect(toList());
//
//
//        Map<Integer, Point> newCentoids = xyzDistances.stream()
//                .map(Point::of) // clusterIndex => Point
//                .collect(groupingBy(Point::getClusterIndex, pointCollector));
//
//        newCentoids.entrySet().stream()
//                .forEach(System.out::println);
//

    }

    public static int whichClusterKMean(double[] distances) {
        return Point.minIndex(distances);
    }



}
