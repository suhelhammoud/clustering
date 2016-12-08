package testing;

import org.junit.Test;
import weka.clusterers.UtilCluster;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by suhel on 11/26/16.
 */
public class UtilClusterTest {
    @Test
    public void numericalAttIndexesTest()  {

        try {
            String[] files = new String[]{
                    "data/iris.arff",
                    "data/airline.arff",
                    "data/cl.arff"};

            int[] attLengths = new int[]{4, 2, 0};
            for (int i = 0; i < files.length; i++) {
                String file = files[i];
                Instances data = new Instances(new FileReader(file));
                int[] numericalAttIndexes = UtilCluster.numericalAttIndexes(data);
                assertEquals(attLengths[i], numericalAttIndexes.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sample() throws Exception {

        List<double[]> points = IntStream.range(0, 100)
                .mapToObj(e -> new double[4])
                .collect(Collectors.toList());
        assertEquals(4, UtilCluster.sample(points, 4).size());
        assertEquals(100, UtilCluster.sample(points, 4).size());

    }

    @Test
    public void generateDataSet() {
        List<double[]> centroids = new ArrayList<>();
        centroids.add(new double[]{0, 0, 0, 0});
        centroids.add(new double[]{2, 2, 2, 2});
        centroids.add(new double[]{5, 5, 0, 0});

        List<Double> vars = Arrays.asList(1.0, 1.0, 1.0);

        List<String> result = new ArrayList<>();

        Random rnd = new Random();
        for (int i = 0; i < 3; i++) {
            double var = vars.get(i);

            for (int j = 0; j < 50; j++) {
                StringJoiner tmp = new StringJoiner(", ");
                for (int k = 0; k < 4; k++) {
                    double v = rnd.nextGaussian()* var + centroids.get(i)[k];
                    tmp.add(String.format("%4.4f", v));
                }
                result.add(tmp.toString());
            }
        }
        for (String point : result) {
            System.out.println(point);
        }
        ;
    }

}