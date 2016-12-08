/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    SimpleKMeans.java
 *    Copyright (C) 2000 University of Waikato, Hamilton, New Zealand
 *
 */
package weka.clusterers;

import weka.clusterers.BaadelDataStructures.PointDistance;
import weka.core.*;
import weka.core.Capabilities.Capability;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * @author
 * @author
 * @see
 */
public class OverlappingKMean extends RandomizableClusterer implements
        NumberOfClustersRequestable, WeightedInstancesHandler, UtilCluster {

    /**
     * for serialization
     */
    static final long serialVersionUID = -3235808600124455376L;

    /**
     * replace missing values in training instances
     */
    private ReplaceMissingValues m_ReplaceMissingFilter;

    /**
     * number of clusters to generate
     */
    private int m_NumClusters = 2;

    /**
     * holds the cluster centroids
     */
    private Instances m_ClusterCentroids;

    private List<double[]> m_dCentroids;

    private int[] numericalAttributes;

    private Instances data0 = null;
//    private List<Point> m_centroids;

    /**
     * Holds the standard deviations of the numeric attributes in each cluster
     */
    private Instances m_ClusterStdDevs;

    /**
     * For each cluster, holds the frequency counts for the values of each nominal
     * attribute
     */
    private int[][][] m_ClusterNominalCounts;
    private int[][] m_ClusterMissingCounts;

    /**
     * Stats on the full data set for comparison purposes In case the attribute is
     * numeric the value is the mean if is being used the Euclidian distance or
     * the median if Manhattan distance and if the attribute is nominal then it's
     * mode is saved
     */
    private double[] m_FullMeansOrMediansOrModes;
    private double[] m_FullStdDevs;
    private int[][] m_FullNominalCounts;
    private int[] m_FullMissingCounts;

    /**
     * Display standard deviations for numeric atts
     */
    private boolean m_displayStdDevs;

    /**
     * Replace missing values globally?
     */
    private boolean m_dontReplaceMissing = false;

    /**
     * The number of instances in each cluster
     */
    private int[] m_ClusterSizes;

    /**
     * Maximum number of iterations to be executed
     */
    private int m_MaxIterations = 500;

    /**
     * Keep track of the number of iterations completed before convergence
     */
    private int m_Iterations = 0;

    /**
     * Holds the squared errors for all clusters
     */
    private double[] m_squaredErrors;

    /**
     * the distance function used.
     */
    protected DistanceFunction m_DistanceFunction = new EuclideanDistance();

    /* Distance function, now either euclidean or manhattan*/
//    BiFunction<double[], double[], Double> m_distanceFun = Point::eDistanceSquared;

    /**
     * Preserve order of instances
     */
    private boolean m_PreserveOrder = false;

    /**
     * Assignments obtained
     */
    protected int[] m_Assignments = null;

    /**
     * the default constructor
     */
    public OverlappingKMean() {
        super();

        m_SeedDefault = 10;
        setSeed(m_SeedDefault);
    }

    /**
     * Returns a string describing this clusterer
     *
     * @return a description of the evaluator suitable for displaying in the
     * explorer/experimenter gui
     */
    public String globalInfo() {
        return "Cluster data using the p means algorithm. Can use either "
                + "the Euclidean distance (default) or the Manhattan distance."
                + " If the Manhattan distance is used, then centroids are computed "
                + "as the component-wise median rather than mean.";
    }

    /**
     * Returns default capabilities of the clusterer.
     *
     * @return the capabilities of this clusterer
     */
    @Override
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();
        result.enable(Capability.NO_CLASS);

        // attributes
        result.enable(Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capability.NUMERIC_ATTRIBUTES);
        result.enable(Capability.MISSING_VALUES);

        return result;
    }


    static double[] calcSquraredError(List<double[]> dPoints,
                                      List<double[]> centroids,
                                      BiFunction<double[], double[], Double> distanceFun) {
        double[] result = new double[centroids.size()];
        dPoints.stream()
                .forEach(p -> {
                    double[] distances = Point.distances(p, centroids, distanceFun);
                    int index = Point.minIndex(distances);
                    result[index] += distances[index];
                });
        return result;
    }

    private static boolean containsMissing(Instance instance) {
        int[] numericalAttIndexes = UtilCluster.numericalAttIndexes(instance.dataset());
        for (int index : numericalAttIndexes) {
            if (instance.isMissing(index)) return true;
        }
        return false;
    }

    private double[] mapOneInstance(Instance instance) {
        int[] numericalAttIndexes = UtilCluster.numericalAttIndexes(instance.dataset());
        double[] result = new double[numericalAttIndexes.length];
        for (int index : numericalAttIndexes) {
            if (instance.isMissing(index)) return null;
            result[index] = instance.value(index);
        }
        return result;
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        //TODO optimize the code
        if (containsMissing(instance))
            return null;

        int[] numericalAttIndexes = UtilCluster.numericalAttIndexes(instance.dataset());

        double[] dPoint = mapOneInstance(instance);
        assert dPoint != null;

        return Point.distances(dPoint,
                m_dCentroids,
                Point::eDistanceSquared);
    }

    void simpleKMean(Instances data) throws Exception {
        //TODO can cluster handle the data
        //TODO replace missing data, delete or replace with mean (numeric) or with most common (nominal)
        /* holds the header of dataset */
        data0 = new Instances(data, 0);

        numericalAttributes = UtilCluster.numericalAttIndexes(data);

        List<double[]> points = UtilCluster.mapInstancesToPoints(data, numericalAttributes);

        int dimension = points.get(0).length;

        PointCollector pointCollector = new PointCollector(dimension);

        int numClusterCentoids = m_NumClusters;

        /* choose initial centroids from dataset*/
        List<double[]> centroids = UtilCluster.sample(points, numClusterCentoids);
//        centroids.stream().map(Point::formated).forEach(System.out::println);

        /* cluster assignments*/
        List<Integer> clusterAssignments = null;

        /* update number of centroids based on the available sampled points*/
        numClusterCentoids = centroids.size(); //update numClusterCentroids if needed

        /* Distance function, now either euclidean or manhattan*/
        BiFunction<double[], double[], Double> distanceFun = Point::eDistanceSquared;

        int iteration = 0;
        boolean isConverged = false;

        while (!isConverged) {
            iteration++;
            List<double[]> tmpCentroids = centroids;

            //calc distances to all centroids from each point
            List<PointDistance> pointDistance = points.stream()
                    .map(dPoint -> PointDistance.of(dPoint,
                            Point.distances(dPoint, tmpCentroids, distanceFun)))
                    .collect(Collectors.toList());


            //inject Said new modification here

            //choose assigned clusters to points
            List<Integer> newClusterAssignments = pointDistance.stream()
                    .map(e -> Point.minIndex(e.d))
                    .collect(Collectors.toList());
            assert newClusterAssignments.size() == points.size();



            //group points in each cluster
            Map<Integer, List<double[]>> clusters = pointDistance.stream()
                    .collect(groupingBy(e -> Point.minIndex(e.d),
                            mapping(e -> e.p, toList())));

            //count points in each cluster
            Map<Integer, Integer> clustersCount = clusters.entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, e -> e.getValue().size()));

            //calculate new centroid in each cluster
            Map<Integer, double[]> clustersCentroids = clusters.entrySet().stream()
                    .collect(toMap(e -> e.getKey(),
                            e -> Point.sumDPoints(e.getValue(), dimension) ));

            //Normalize centroid
            //TODO implement it in streams
            for (Map.Entry<Integer, double[]> e : clustersCentroids.entrySet()) {
                Integer centroidIndex = e.getKey();
                int count = clustersCount.get(centroidIndex);
                double[] pnts = e.getValue();
                for (int i = 0; i < pnts.length; i++) {
                    pnts[i] /= count;
                }
            }

            //update global list of centroids
            centroids = clustersCentroids.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .map(e -> e.getValue())
                    .collect(toList());

            boolean isSameAssignments = BCluster.isSame(newClusterAssignments, clusterAssignments);

            isConverged = iteration >= m_MaxIterations || isSameAssignments;

            clusterAssignments = newClusterAssignments;

        }
        //update square errors
//        m_ClusterStdDevs;
        if (m_displayStdDevs) {
//            m_ClusterStdDevs = new Instances(instances, m_NumClusters);
//            List<Integer> clusterCounts = clusterCentroids.values().stream()
//                    .sorted()
//                    .map(e -> (int) e.getWeight())
//                    .collect(toList());

            m_squaredErrors = calcSquraredError(points, centroids, distanceFun);
        }

        m_dCentroids = centroids;
        m_Iterations = iteration;
    }



    /**
     * Generates a clusterer. Has to initialize all fields of the clusterer that
     * are not being set via options.
     *
     * @param data set of instances serving as training data
     * @throws Exception if the clusterer has not been generated successfully
     */
    @Override
    public void buildClusterer(Instances data) throws Exception {
        simpleKMean(data);

    }



    /**
     * Move the centroid to it's new coordinates. Generate the centroid
     * coordinates based on it's members (objects assigned to the cluster of the
     * centroid) and the distance function being used.
     *
     * @param centroidIndex     index of the centroid which the coordinates will be
     *                          computed
     * @param members           the objects that are assigned to the cluster of this
     *                          centroid
     * @param updateClusterInfo if the method is supposed to update the m_Cluster
     *                          arrays
     * @return the centroid coordinates
     */
    protected double[] moveCentroid(int centroidIndex, Instances members,
                                    boolean updateClusterInfo) {
        double[] vals = new double[members.numAttributes()];

        // used only for Manhattan Distance
        Instances sortedMembers = null;
        int middle = 0;
        boolean dataIsEven = false;

        if (m_DistanceFunction instanceof ManhattanDistance) {
            middle = (members.numInstances() - 1) / 2;
            dataIsEven = ((members.numInstances() % 2) == 0);
            if (m_PreserveOrder) {
                sortedMembers = members;
            } else {
                sortedMembers = new Instances(members);
            }
        }

        for (int j = 0; j < members.numAttributes(); j++) {

            // in case of Euclidian distance the centroid is the mean point
            // in case of Manhattan distance the centroid is the median point
            // in both cases, if the attribute is nominal, the centroid is the mode
            if (m_DistanceFunction instanceof EuclideanDistance
                    || members.attribute(j).isNominal()) {
                vals[j] = members.meanOrMode(j);
            } else if (m_DistanceFunction instanceof ManhattanDistance) {
                // singleton special case
                if (members.numInstances() == 1) {
                    vals[j] = members.instance(0).value(j);
                } else {
                    vals[j] = sortedMembers.kthSmallestValue(j, middle + 1);
                    if (dataIsEven) {
                        vals[j] = (vals[j] + sortedMembers.kthSmallestValue(j, middle + 2)) / 2;
                    }
                }
            }

            if (updateClusterInfo) {
                m_ClusterMissingCounts[centroidIndex][j] = members.attributeStats(j).missingCount;
                m_ClusterNominalCounts[centroidIndex][j] = members.attributeStats(j).nominalCounts;
                if (members.attribute(j).isNominal()) {
                    if (m_ClusterMissingCounts[centroidIndex][j] > m_ClusterNominalCounts[centroidIndex][j][Utils
                            .maxIndex(m_ClusterNominalCounts[centroidIndex][j])]) {
                        vals[j] = Instance.missingValue(); // mark mode as missing
                    }
                } else {
                    if (m_ClusterMissingCounts[centroidIndex][j] == members
                            .numInstances()) {
                        vals[j] = Instance.missingValue(); // mark mean as missing
                    }
                }
            }
        }
        if (updateClusterInfo) {
            m_ClusterCentroids.add(new Instance(1.0, vals));
        }
        return vals;
    }

    /**
     * clusters an instance that has been through the filters
     *
     * @param instance     the instance to assign a cluster to
     * @param updateErrors if true, update the within clusters sum of errors
     * @return a cluster number
     */
    private int clusterProcessedInstance(Instance instance, boolean updateErrors) {
        double minDist = Integer.MAX_VALUE;
        int bestCluster = 0;
        for (int i = 0; i < m_NumClusters; i++) {
            double dist = m_DistanceFunction.distance(instance,
                    m_ClusterCentroids.instance(i));
            if (dist < minDist) {
                minDist = dist;
                bestCluster = i;
            }
        }
        if (updateErrors) {
            if (m_DistanceFunction instanceof EuclideanDistance) {
                // Euclidean distance to Squared Euclidean distance
                minDist *= minDist;
            }
            m_squaredErrors[bestCluster] += minDist;
        }
        return bestCluster;
    }

    /**
     * If any missing data return empty double[]
     *
     * @param instance
     * @return
     */
    private double[] mapInstance(Instance instance) {
        double[] result = new double[numericalAttributes.length];
        for (int attIndex : numericalAttributes) {
            if (instance.isMissing(attIndex)) return new double[0];
            result[attIndex] = instance.value(attIndex);
        }
        return result;
    }

    /**
     * Classifies a given instance.
     *
     * @param instance the instance to be assigned to a cluster
     * @return the number of the assigned cluster as an interger if the class is
     * enumerated, otherwise the predicted value
     * @throws Exception if instance could not be classified successfully
     */
    @Override
    public int clusterInstance(Instance instance) throws Exception {
        if (containsMissing(instance))
            return -1;
        double[] distances = distributionForInstance(instance);
        if (distances == null)
            return -1;
        return Point.minIndex(distances);
    }

    /**
     * Returns the number of clusters.
     *
     * @return the number of clusters generated for a training dataset.
     * @throws Exception if number of clusters could not be returned successfully
     */
    @Override
    public int numberOfClusters() throws Exception {
        return m_NumClusters;
    }

    /**
     * Returns an enumeration describing the available options.
     *
     * @return an enumeration of all the available options.
     */
    @Override
    public Enumeration listOptions() {
        Vector result = new Vector();

        result.addElement(new Option("\tnumber of clusters.\n" + "\t(default 2).",
                "N", 1, "-N <num>"));
        result.addElement(new Option("\tDisplay std. deviations for centroids.\n",
                "V", 0, "-V"));

        result.add(new Option("\tMaximum number of iterations.\n", "I", 1,
                "-I <num>"));


        Enumeration en = super.listOptions();
        while (en.hasMoreElements()) {
            result.addElement(en.nextElement());
        }

        return result.elements();
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String numClustersTipText() {
        return "set number of clusters";
    }

    /**
     * set the number of clusters to generate
     *
     * @param n the number of clusters to generate
     * @throws Exception if number of clusters is negative
     */
    @Override
    public void setNumClusters(int n) throws Exception {
        if (n <= 0) {
            throw new Exception("Number of clusters must be > 0");
        }
        m_NumClusters = n;
    }

    /**
     * gets the number of clusters to generate
     *
     * @return the number of clusters to generate
     */
    public int getNumClusters() {
        return m_NumClusters;
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String maxIterationsTipText() {
        return "set maximum number of iterations";
    }

    /**
     * set the maximum number of iterations to be executed
     *
     * @param n the maximum number of iterations
     * @throws Exception if maximum number of iteration is smaller than 1
     */
    public void setMaxIterations(int n) throws Exception {
        if (n <= 0) {
            throw new Exception("Maximum number of iterations must be > 0");
        }
        m_MaxIterations = n;
    }

    /**
     * gets the number of maximum iterations to be executed
     *
     * @return the number of clusters to generate
     */
    public int getMaxIterations() {
        return m_MaxIterations;
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String displayStdDevsTipText() {
        return "Display std deviations of numeric attributes "
                + "and counts of nominal attributes.";
    }

    /**
     * Sets whether standard deviations and nominal count Should be displayed in
     * the clustering output
     *
     * @param stdD true if std. devs and counts should be displayed
     */
    public void setDisplayStdDevs(boolean stdD) {
        m_displayStdDevs = stdD;
    }

    /**
     * Gets whether standard deviations and nominal count Should be displayed in
     * the clustering output
     *
     * @return true if std. devs and counts should be displayed
     */
    public boolean getDisplayStdDevs() {
        return m_displayStdDevs;
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String dontReplaceMissingValuesTipText() {
        return "Replace missing values globally with mean/mode.";
    }

    /**
     * Sets whether missing values are to be replaced
     *
     * @param r true if missing values are to be replaced
     */
    public void setDontReplaceMissingValues(boolean r) {
        m_dontReplaceMissing = r;
    }

    /**
     * Gets whether missing values are to be replaced
     *
     * @return true if missing values are to be replaced
     */
    public boolean getDontReplaceMissingValues() {
        return m_dontReplaceMissing;
    }

    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String distanceFunctionTipText() {
        return "The distance function to use for instances comparison "
                + "(default: weka.core.EuclideanDistance). ";
    }

    /**
     * returns the distance function currently in use.
     *
     * @return the distance function
     */
    public DistanceFunction getDistanceFunction() {
        return m_DistanceFunction;
    }

    /**
     * sets the distance function to use for instance comparison.
     *
     * @param df the new distance function to use
     * @throws Exception if instances cannot be processed
     */
    public void setDistanceFunction(DistanceFunction df) throws Exception {
        if (!(df instanceof EuclideanDistance)
                && !(df instanceof ManhattanDistance)) {
            throw new Exception(
                    "SimpleKMeans currently only supports the Euclidean and Manhattan distances.");
        }
        m_DistanceFunction = df;
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String preserveInstancesOrderTipText() {
        return "Preserve order of instances.";
    }

    /**
     * Sets whether order of instances must be preserved
     *
     * @param r true if missing values are to be replaced
     */
    public void setPreserveInstancesOrder(boolean r) {
        m_PreserveOrder = r;
    }

    /**
     * Gets whether order of instances must be preserved
     *
     * @return true if missing values are to be replaced
     */
    public boolean getPreserveInstancesOrder() {
        return m_PreserveOrder;
    }

    /**
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    @Override
    public void setOptions(String[] options) throws Exception {

        m_displayStdDevs = Utils.getFlag("V", options);

        String optionString = Utils.getOption('N', options);

        if (optionString.length() != 0) {
            setNumClusters(Integer.parseInt(optionString));
        }

        optionString = Utils.getOption("I", options);
        if (optionString.length() != 0) {
            setMaxIterations(Integer.parseInt(optionString));
        }

        super.setOptions(options);
    }

    /**
     * Gets the current settings of SimpleKMeans
     *
     * @return an array of strings suitable for passing to setOptions()
     */
    @Override
    public String[] getOptions() {
        int i;
        Vector result;
        String[] options;

        result = new Vector();

        if (m_displayStdDevs) {
            result.add("-V");
        }

        result.add("-N");
        result.add("" + getNumClusters());

        result.add("-I");
        result.add("" + getMaxIterations());

        options = super.getOptions();
        for (i = 0; i < options.length; i++) {
            result.add(options[i]);
        }

        return (String[]) result.toArray(new String[result.size()]);
    }

       /**
     * return a string describing this clusterer
     *
     * @return a description of the clusterer as a string
     */
    @Override
    public String toString() {
        StringJoiner result = new StringJoiner("\n");
        result.add(String.format("Number of iterations: %d", m_Iterations));

        String sCentroids = m_dCentroids.stream().map(e -> Point.formated(e).toString())
                .collect(Collectors.joining("\n"));
        result.add(sCentroids);

        return result.toString();
    }

    private String pad(String source, String padChar, int length, boolean leftPad) {
        StringBuffer temp = new StringBuffer();

        if (leftPad) {
            for (int i = 0; i < length; i++) {
                temp.append(padChar);
            }
            temp.append(source);
        } else {
            temp.append(source);
            for (int i = 0; i < length; i++) {
                temp.append(padChar);
            }
        }
        return temp.toString();
    }

    /**
     * Gets the the cluster centroids
     *
     * @return the cluster centroids
     */
    public Instances getClusterCentroids() {
        return m_ClusterCentroids;
    }

    /**
     * Gets the standard deviations of the numeric attributes in each cluster
     *
     * @return the standard deviations of the numeric attributes in each cluster
     */
    public Instances getClusterStandardDevs() {
        return m_ClusterStdDevs;
    }

    /**
     * Returns for each cluster the frequency counts for the values of each
     * nominal attribute
     *
     * @return the counts
     */
    public int[][][] getClusterNominalCounts() {
        return m_ClusterNominalCounts;
    }

    /**
     * Gets the squared error for all clusters
     *
     * @return the squared error
     */
    public double getSquaredError() {
        return Utils.sum(m_squaredErrors);
    }

    /**
     * Gets the number of instances in each cluster
     *
     * @return The number of instances in each cluster
     */
    public int[] getClusterSizes() {
        return m_ClusterSizes;
    }

    /**
     * Gets the assignments for each instance
     *
     * @return Array of indexes of the centroid assigned to each instance
     * @throws Exception if order of instances wasn't preserved or no assignments
     *                   were made
     */
    public int[] getAssignments() throws Exception {
        if (!m_PreserveOrder) {
            throw new Exception(
                    "The assignments are only available when order of instances is preserved (-O)");
        }
        if (m_Assignments == null) {
            throw new Exception("No assignments made.");
        }
        return m_Assignments;
    }

    /**
     * Returns the revision string.
     *
     * @return the revision
     */
    @Override
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 10537 $");
    }

    /**
     * Main method for testing this class.
     *
     * @param argv should contain the following arguments:
     *             <p>
     *             -t training file [-N number of clusters]
     */
    public static void main(String[] argv) {
        runClusterer(new OverlappingKMean(), argv);
    }
}
