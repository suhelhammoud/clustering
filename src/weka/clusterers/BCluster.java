package weka.clusterers;

import testing.BClusterTest;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by suhel on 10/7/16.
 */

public class BCluster {


    /**
     * Used to choose initial number of centroids out of list of points
     *
     * @param points
     * @param numCentroids
     * @return sampled points as centroids, if numCentroids required is greater than points return points
     */
    public static <T> List<T> sample(List<T> points, int numCentroids) {
        return points.stream()
                .distinct()
                .limit(numCentroids)
                .collect(toList());
    }




    public static boolean equalLists(List<Point> oCentroids, List<Point> nCentroids) {
        if(oCentroids.size() != nCentroids.size())
            return false;

        for (int i = 0; i < oCentroids.size(); i++) {
            Point point =  oCentroids.get(i);
            if( ! point.equals(nCentroids.get(i)))
                return false;
        }
        return true;
    };

    int dimensions = 4;
    int numClusters= 3;
    int maxIteration = 10;

    public void buildClassifier(List<double[]> pointsArray) {
        PointFactory pointFactory = new PointFactory(dimensions);
        PointCollector pointCollector = new PointCollector(dimensions);

//        List<double[]> pointsArray = getPointsDouble();

        List<Point> centroids = sample(pointsArray, numClusters).stream()
                .map(Point::of)
                .collect(toList());

        //point.v[]  distatnces[]
        List<PairArray> xyzDistances = pointsArray.stream()
                .map(e -> PairArray.of(e, Point.distancesSquared(centroids, e)))
                .collect(toList());


        Map<Integer, Point> newCentoids = xyzDistances.stream()
                .map(Point::of) // clusterIndex => Point
                .collect(groupingBy(Point::getClusterIndex, pointCollector));

        newCentoids.entrySet().stream()
                .forEach(System.out::println);

    }

    public static void main(String[] args) {
        System.out.println("Start");
        int dimension = 4;
        int numClusters = 3;

        PointFactory pointFactory = new PointFactory(dimension);
        PointCollector pointCollector = new PointCollector(dimension);

        List<double[]> pointsArray = BClusterTest.getPointsDouble();

        List<Point> centroids = sample(pointsArray, numClusters).stream()
                .map(Point::of)
                .collect(toList());
        //point.v[]  ~ distatnces[]
        List<PairArray> xyzDistances = pointsArray.stream()
                .map(e -> PairArray.of(e, Point.distancesSquared(centroids, e)))
                .collect(toList());


        Map<Integer, Point> newCentoids = xyzDistances.stream()
                .map(Point::of) // clusterIndex => Point
                .collect(groupingBy(Point::getClusterIndex, pointCollector));

        newCentoids.entrySet().stream()
                .forEach(System.out::println);


    }


}
