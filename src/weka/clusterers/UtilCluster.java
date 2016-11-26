package weka.clusterers;

import com.google.common.primitives.Ints;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suhel on 10/7/16.
 */
public class UtilCluster {

    /**
     * @param data
     * @return indexes of only numerical attributes
     */
    public static int[] numericalAttIndexes(Instances data) {
        List<Integer> numericalAttrs = new ArrayList<>(data.numAttributes());
        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).isNumeric()) {
                numericalAttrs.add(i);
            }
        }
        return Ints.toArray(numericalAttrs);
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
            for (int j = 0; j < attIndexes.length; j++) {
                //TODO use different policy to deal with missing data
                //skip this instance if any of its attributes were missing
                if (instance.isMissing(j)) continue OUTERCONTIUE;
                point[j] = instance.value(j);
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


}
