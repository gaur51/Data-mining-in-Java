import java.util.*;
import java.io.*;

/**
* A dataset implemented as an ArrayList of sets of Strings. It loads the data
* from a CSV file at construction time.
*/
public class Dataset extends ArrayList<Set<String>> {
    public LinkedHashMap<Set<String>, Integer> itemsetCounts = null;


    public Dataset(String datasetFilename,
                   boolean includeEmptyBaskets) throws Exception {
        loadDataset(datasetFilename, includeEmptyBaskets);
    }

    //**************************************************************************
    // PUBLIC METHODS
    //**************************************************************************


    /**
    * Runs the apriori algorithm and efficiently returns the frequent itemsets
    * with support at least minSupport.
    * We include the pseudocode from the paper as comments next to the
    * corresponding line of our code.
    */
    public Set<Set<String>> aprioriAlgorithm(double minSupport,
            double minConfidence) {
        Set<Set<String>> answer = new HashSet<Set<String>>();

        itemsetCounts = new LinkedHashMap<Set<String>, Integer>();

        // Large 1-itemsets.
        // "L_1 = {large 1-itemsets};"
        Set<Set<String>> large1Itemsets = getLarge1Itemsets(minSupport);
        answer.addAll(large1Itemsets);

        // Large (k-1)-itemsets.
        Set<Set<String>> largeKMinus1Itemsets = large1Itemsets;
        // "for (k=2; L_{k-1} \neq \emptyset; k++) do begin"
        while (!largeKMinus1Itemsets.isEmpty()) {
            // New candidate k-itemsets.
            // "C_k = apriori-gen(L_{k-1}); // New candidates"
            Set<Set<String>> candidateKItemsets
                = aprioriGen(largeKMinus1Itemsets, largeKMinus1Itemsets);
            for (Set<String> transaction : this) {
                // For each transaction (row) in the dataset.
                // "forall transactions t \in D do begin"

                // New candidate k-itemsets that are contained in this
                // transaction
                // "C_t = subset(C_k, t); // Candidates contained in t"
                Set<Set<String>> candidateKItemsetsContainedInT =
                    subset(candidateKItemsets, transaction);

                for (Set<String> candidate : candidateKItemsetsContainedInT) {
                    // "forall candidates c \in C_t do"

                    // "c.count++;"
                    Integer itemsetCount = itemsetCounts.get(candidate);
                    if (itemsetCount == null) {
                        itemsetCount = 0;
                    }
                    itemsetCounts.put(candidate, itemsetCount + 1);
                }
            }
            // Large k-itemsets.
            // "L_k = {c \in C_k | c.count \geq minsup}"
            Set<Set<String>> largeKItemsets = new HashSet<Set<String>>();
            for (Set<String> candidate : candidateKItemsets) {
                Integer itemsetCount = itemsetCounts.get(candidate);
                if (itemsetCount == null) {
                    itemsetCount = 0;
                }
                if (itemsetCount * 1.0 / this.size() >= minSupport) {
                    largeKItemsets.add(candidate);
                }
            }
            answer.addAll(largeKItemsets);
            largeKMinus1Itemsets = largeKItemsets;
        }
        return answer;
    }


    public Double getItemsetSupport(Set<String> itemset) {
        Integer itemCount = itemsetCounts.get(itemset);
        if (itemCount == null) {
            throw new RuntimeException("getItemsetSupport failed!");
        }
        return (double)itemCount / this.size();
    }

    //**************************************************************************
    // PRIVATE METHODS
    //**************************************************************************

    /*
    * Returns all candidates in candidateKItemsets that are contained in
    * transaction.
    */
    private Set<Set<String>> subset(Set<Set<String>> candidateKItemsets,
                                    Set<String> transaction) {
        Set<Set<String>> result = new HashSet<Set<String>>();
        for (Set<String> candidateKItemset : candidateKItemsets) {
            if (transaction.containsAll(candidateKItemset)) {
                result.add(candidateKItemset);
            }
        }
        return result;
    }

    /**
    * aprioriGen method used by the apriori algorithm to generate candidates of
    * length k based on candidates of length k-1.
    */
    private Set<Set<String>> aprioriGen(Set<Set<String>> cKMinus1, Set<Set<String>> lKMinus1) {
        Set<Set<String>> candidateKItemsets =
            joinStep(lKMinus1);
        candidateKItemsets = pruneStep(cKMinus1, candidateKItemsets);

        return candidateKItemsets;
    }


    private HashSet<Set<String>> joinStep(Set<Set<String>> lKMinus1) {
        HashSet<Set<String>> result = new HashSet<Set<String>>();
        for (Set<String> itemset1 : lKMinus1) {
            for (Set<String> itemset2 : lKMinus1) {
                boolean skip = false;
                Set<String> itemset2Copy = new TreeSet<String>(itemset2);
                String in1Only = null;
                String in2Only = null;
                for (String item : itemset1) {
                    if (itemset2.contains(item)) {
                        itemset2Copy.remove(item);
                        continue;
                    } else {
                        if (in1Only == null) {
                            in1Only = item;
                        } else {
                            // Skip this pair!
                            skip = true;
                            break;
                        }
                    }
                }
                for (String item : itemset2Copy) {
                    if (in2Only == null && in1Only.compareTo(item) < 0) {
                        in2Only = item;
                    } else {
                        // Skip this pair!
                        skip = true;
                        break;
                    }
                }
                if (!skip) {
                    itemset2Copy.addAll(itemset1);
                    result.add(itemset2Copy);
                }
            }
        }
        return result;
    }


    private Set<Set<String>> pruneStep(Set<Set<String>> cKMinus1, Set<Set<String>> cK) {
        for (Iterator<Set<String>> iterator = cK.iterator(); iterator.hasNext();) {
            Set<String> itemset = iterator.next();

            for (String item : itemset) {
                Set<String> subset = new HashSet<String>(itemset);
                subset.remove(item);
                if (!cKMinus1.contains(subset)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return cK;
    }


    private Set<Set<String>> getLarge1Itemsets(double minSupport) {
        for (Set<String> itemset : this) {
            for (String item : itemset) {
                Set<String> candidate = new TreeSet<String>();
                candidate.add(item);
                Integer itemsetCount = itemsetCounts.get(candidate);
                if (itemsetCount == null) {
                    itemsetCount = 0;
                }
                itemsetCounts.put(candidate, itemsetCount + 1);
            }
        }
        Set<Set<String>> large1Itemsets = new HashSet<Set<String>>();
        for (Map.Entry<Set<String>, Integer> itemsetAndCount : itemsetCounts.entrySet()) {
            Set<String> itemset = itemsetAndCount.getKey();
            Integer count = itemsetAndCount.getValue();
            if (count * 1.0 / this.size() > minSupport) {
                large1Itemsets.add(itemset);
            }
        }
        return large1Itemsets;
    }


    private void loadDataset(String datasetFilename,
                             boolean includeEmptyBaskets) throws Exception {

        File datasetFile = null;
        try {
            datasetFile = new File(datasetFilename);
        } catch (Exception e) {
            throw new Exception("Error: dataset file not found.");
        }
        Scanner csvScanner = new Scanner(datasetFile);
        while (csvScanner.hasNextLine()) {
            String basketString = csvScanner.nextLine();
            Set<String> basketSet = new LinkedHashSet<String>();
            // String[] itemStrings = basketString.split(",");
            // Regular Expression to match CSV lines with escaped quotes (http://stackoverflow.com/questions/15738918/splitting-a-csv-file-with-quotes-as-text-delimiter-using-string-split?answertab=votes#tab-top).
            String[] itemStrings = basketString.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String itemString : itemStrings) {
                String item = itemString;
                if (!item.isEmpty()) {
                    basketSet.add(item);
                }
            }
            if (!includeEmptyBaskets && basketSet.isEmpty()) {
                continue;
            }
            this.add(basketSet);
        }
        if (this.isEmpty()) {
            throw new Exception("Error: dataset file not found.");
        }
        if (this.size() < 1000) {
            System.out.println("Warning: dataset has " + this.size() + " " +
                               "rows, which is less than the minimum of 1000 " +
                               "rows");
            System.out.println("         required by the assignment's " +
                               "instructions.");
        }
    }
}
