package weka.clusterers;

import weka.core.Instance;
import weka.core.Instances;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by suhel on 10/7/16.
 */
public interface UtilCluster {

    /**
     * @param data
     * @return indexes of only numerical attributes
     */
    public static int[] numericalAttIndexes(Instances data) {
        List<Integer> numericalAttrs = new ArrayList<>(data.numAttributes());
        return IntStream.range(0, data.numAttributes())
                .filter( i -> data.attribute(i).isNumeric())
                .toArray();
    }

    public static List<double[]> mapInstancesToPoints(Instances data) {
        int[] numericalAttIndexes = numericalAttIndexes(data);
        return mapInstancesToPoints(data, numericalAttIndexes);
    }

        /**
         * @param data
         * @param attIndexes indexes of numerical attributed to be mapped
         * @return list of points, each point is an int array of numerical attributes
         */
    public static List<double[]> mapInstancesToPoints(Instances data, int[] attIndexes) {
        List<double[]> result = new ArrayList<>(data.numInstances());
        OUTERCONTIUE:
        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            double[] point = new double[attIndexes.length];
            for (int attIndex : attIndexes) {
                //TODO use different policy to deal with missing data
                //skip this instance if any of its attributes were missing
                if (instance.isMissing(attIndex)) continue OUTERCONTIUE;
                point[attIndex] = instance.value(attIndex);
            }
            result.add(point);
        }
        return result;
    }


    public static Instances getInstances(String filename) {
        Instances result = null;
        try {

            result = new Instances(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    ;

    public static List<double[]> getPoints(Instances data) {
        int[] attrs = numericalAttIndexes(data);
//        System.out.println("attrs.length = " + attrs.length);
        return mapInstancesToPoints(data, attrs);

    }

    /**
     * Used to choose initial number of centroids out of list of points
     *
     * @param points
     * @param numCentroids
     * @return sampled points as centroids, if numCentroids required is greater than points return points
     */
    public static <T> List<T> sample(List<T> points, int numCentroids) {
        // TODO add random sample capability
        return points.stream()
                .distinct()
                .limit(numCentroids)
                .collect(toList());
    }



}
