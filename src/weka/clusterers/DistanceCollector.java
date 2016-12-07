package weka.clusterers;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Created by suhel on 12/7/16.
 */
public class DistanceCollector implements Collector<Point, Point, Point> {
    final PointFactory pointFactory;

    public DistanceCollector(int dimension) {
        pointFactory = new PointFactory(dimension);
    }


    @Override
    public Supplier<Point> supplier() {
        return pointFactory;
    }

    @Override
    public BiConsumer<Point, Point> accumulator() {
        return DistanceCollector::accumulate;
    }

    public static void accumulate(Point p1, Point p2) {
        //TODO
    }

    @Override
    public BinaryOperator<Point> combiner() {
        return null;
    }

    @Override
    public Function<Point, Point> finisher() {
        return null;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return null;
    }
}
