package testing;

import org.junit.Test;
import weka.clusterers.BCluster;
import weka.clusterers.UtilCluster;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by suhel on 11/25/16.
 */
public class BClusterTest {

    public static List<double[]> getPointsDouble() {
        try {

            Instances data = new Instances(new FileReader("data/iris.arff"));
            return UtilCluster.getPoints(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void sample() throws Exception {

    }



    @Test
    public void getClusterPair() throws Exception {
        List<double[]> list = getPointsDouble();
//        list.forEach(e -> System.out.println(Arrays.toString(e)));
        assertEquals(150, list.size());
        assertEquals(4, list.get(0).length);
    }

    @Test
    public void main() throws Exception {

    }

    @Test
    public void testOrerInStreams() {
        int capacity = 100000;
        Random rnd = new Random();
        List<Integer> points = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            points.add(rnd.nextInt(capacity));
        }

        List<Integer> collected = points.parallelStream().map(e -> e).collect(Collectors.toList());
        assertEquals(points, collected);
    }

}