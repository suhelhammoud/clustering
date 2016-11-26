package testing;

import org.junit.Test;
import weka.clusterers.Point;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by suhel on 11/25/16.
 */
public class PointTest {
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