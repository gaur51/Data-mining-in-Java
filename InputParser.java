import java.util.*;
import java.io.*;

/*
* Based on just the commandline arguments, this class parses these arguments,
* saves them into variables, and loads the dataset from the provided
* filename. It throws an error end exits the program if there's any parse error
* or file not found error.
*/
public class InputParser {
    private String[] args;
    private String datasetFilename = null;
    private Dataset dataset = null;
    private double minSupport = -1;
    private double minConfidence = -1;
    private boolean argsInitialized = false;
    private boolean datasetLoaded = false;


    //**************************************************************************
    // CONSTRUCTOR
    //**************************************************************************

    public InputParser(String[] args) {
        this.args = args;
    }

    //**************************************************************************
    // PUBLIC METHODS
    //**************************************************************************

    public Dataset getDataset() {
        assertDatasetLoaded();
        return dataset;
    }


    public String getDatasetFilename() {
        assertArgsInitialized();
        return datasetFilename;
    }


    public double getMinConfidence() {
        assertArgsInitialized();
        return minConfidence;
    }


    public double getMinSupport() {
        assertArgsInitialized();
        return minSupport;
    }


    public void validateAndParse(boolean includeEmptyBaskets) {
        try {
            validateArgLength();
            parseArgs();
            argsInitialized = true;
            dataset = new Dataset(getDatasetFilename(), includeEmptyBaskets);
            datasetLoaded = true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    public void printCommandLineArgs() {
        System.out.println("==========================================");
        System.out.println("Dataset filename: " + getDatasetFilename());
        System.out.println("Minimum support: " + getMinSupport());
        System.out.println("Minimum confidence: " + getMinConfidence());
        System.out.println("==========================================");
    }

    //**************************************************************************
    // PRIVATE METHODS
    //**************************************************************************

    private void assertArgsInitialized() {
        if (!argsInitialized) {
            System.err.println("Error: InputParser not initialized before " +
                               "trying to get some data from it.");
            System.exit(1);
        }
    }


    private void assertDatasetLoaded() {
        if (!datasetLoaded) {
            System.err.println("Error: InputParser not initialized before " +
                               "trying to get some data from it.");
            System.exit(1);
        }
    }


    private void validateArgLength() throws Exception {
        if (args.length != 3) {
            // throw new Exception(
            //     "Usage: java RuleMining <INTEGRATED-DATASET filename> " +
            //     "<min_sup> <min_conf>");
            throw new Exception(
                "Usage: bash run.sh <INTEGRATED-DATASET filename> " +
                "<min_sup> <min_conf>");
        }
    }


    private void parseArgs() throws Exception {
        datasetFilename = args[0];
        try {
            minSupport = Double.parseDouble(args[1]);
            minConfidence = Double.parseDouble(args[2]);
        } catch (Exception e) {
            throw new Exception("Error parsing min_sup or min_conf. " +
                                "Please make sure that they are numbers.");
        }
        if (minSupport < 0 || minSupport > 1) {
            throw new Exception("Error: min_sup must be in the range [0, 1]");
        }
        if (minConfidence < 0 || minConfidence > 1) {
            throw new Exception("Error: min_conf must be in the range [0, 1]");
        }
    }
}