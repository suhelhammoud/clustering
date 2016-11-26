package weka.clusterers;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;


/**
 * Created by suhel on 11/25/16.
 */
public class PointCollector implements Collector<Point, Point, Point> {

    final PointFactory pointFactory;

    public PointCollector(int dimension) {
        pointFactory = new PointFactory(dimension);
    }

    @Override
    public Supplier<Point> supplier() {
        System.out.println("PointCollector.supplier");
        return pointFactory;
    }

    @Override
    public BiConsumer<Point, Point> accumulator() {
        return Point::accumulate;
    }

    @Override
    public BinaryOperator<Point> combiner() {
        return Point::combine;
    }

    @Override
    public Function<Point, Point> finisher() {
        return Point::getVNormalized;
    }

    @Override
    public Set<Characteristics> characteristics() {

        return new HashSet<>();
    }
}
