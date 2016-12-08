package testing;

import org.junit.Test;
import weka.clusterers.OverlappingKMean;
import weka.clusterers.Point;
import weka.clusterers.UtilCluster;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by suhel on 11/25/16.
 */
public class PointTest {
    @Test
    public void sumDPoints() throws Exception {
        List<double[]> points = Arrays.asList(new double[]{1, 2},
                new double[]{1, 2},
                new double[]{1, 2},
                new double[]{1, 2});

        double[] sum = Point.sumDPoints(points, 2);
        assertTrue(Arrays.equals(new double[]{4,8}, sum  ));

    }

    @Test
    public void eDistanceSquared() throws Exception {
        Instances data = new Instances(new FileReader("data/iris.arff"));
        Instance inst0 = data.instance(0);
        Instance inst1 = data.instance(1);

        EuclideanDistance ed = new EuclideanDistance();
        ed.setDontNormalize(true);
        ed.setInstances(data);
        double d1 = ed.distance(inst0, inst1);
        double d2 = Point.eDistanceSquared(mapInstance(inst0), mapInstance(inst1));
        System.out.println(String.format("simpleKMean: %4.4f, Point.distanceSqurared: %4.4f", d1, d2));
        assertEquals(d2, d1, 1e-5);

    }

    private static double[] mapInstance(Instance instance) {
        int[] numericAttributes = UtilCluster.numericalAttIndexes(instance.dataset());
        double[] result = new double[numericAttributes.length];
        for (int attIndex : numericAttributes) {
            if (instance.isMissing(attIndex)) return new double[0];
            result[attIndex] = instance.value(attIndex);
        }
        return result;
    }

    @Test
    public void hashCodeTest() throws Exception {
        Point p1 = Point.of(2, 3, 5);
        Point p2 = Point.of(2, 3, 5);
        assertEquals(p1.hashCode(), p2.hashCode());

    }

    @Test
    public void equalsTest() throws Exception {
        Point p1 = Point.of(2, 3, 5);
        Point p2 = Point.of(2, 3, 5);
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));

    }

    @Test
    public void accumulate() throws Exception {
        double[] p1 = new double[]{2, 3, 4, 5};
        Point point1 = new Point(p1, 1);

        double[] p2 = new double[]{2, 3, 4, 5};
        Point point2 = new Point(p1, 2);

        point1.accumulate(point2);
        System.out.println("point2 = " + point2);
        assertEquals(3, point1.getWeight(), 1e-6);
        assertEquals(10, point1.getV()[3], 1e-6);

    }

    @Test
    public void combine() throws Exception {
        double[] p1 = new double[]{2, 3, 4, 5};
        Point point1 = new Point(p1, 1);

        double[] p2 = new double[]{2, 3, 4, 5};
        Point point2 = new Point(p1, 2);

        Point comPoint = Point.combine(point1, point2);
        System.out.println("comPoint = " + comPoint);
        assertEquals(3, comPoint.getWeight(), 1e-6);
        assertEquals(10, comPoint.getV()[3], 1e-6);
    }

    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    @org.junit.Test
    public void toStringTest() throws Exception {
        double[] v = new double[]{2, 3, 4, 5};
        Point point = new Point(v, 1);
        System.out.println("point = " + point);
        assertEquals("a", "a");

    }

}