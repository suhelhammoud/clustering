package testing;

import org.junit.Test;
import weka.clusterers.UtilCluster;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by suhel on 11/26/16.
 */
public class UtilClusterTest {

    @Test
    public void sample() throws Exception {

        List<double[]> points = IntStream.range(0, 100)
                .mapToObj(e -> new double[4])
                .collect(Collectors.toList());
        assertEquals(4, UtilCluster.sample(points, 4).size());
        assertEquals(100, UtilCluster.sample(points, 4).size());

    }


}