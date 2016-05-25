import java.util.*;
import java.io.*;

/*
* Our main class for rule mining.
*/
public class RuleMiningMain {
    public static final boolean INCLUDE_EMPTY_BASKETS = true;
    public static final boolean PRINT_RULES_TO_COMMANDLINE = false;
    public static final boolean PRINT_RULES_TO_OUTPUT_FILE = true;
    public static ArrayList<Rule_confidence> rules_list =
        new ArrayList<Rule_confidence>();
    public static InputParser inputParser;

    public static void main(String[] args) {
        printNBlankLines(10);

        // Parse input and read in data set.
        System.out.println("Parsing input and reading data CSV file...");
        inputParser = new InputParser(args);
        inputParser.validateAndParse(INCLUDE_EMPTY_BASKETS);
        System.out.println("Parsing input and reading data CSV file...Done.");
        inputParser.printCommandLineArgs();
        Dataset dataset = inputParser.getDataset();
        double minSupport = inputParser.getMinSupport();
        double minConfidence = inputParser.getMinConfidence();

        // Print out dataset for debugging.
        // System.out.println("Dataset:");
        // System.out.println(dataset);

        // Open output.txt for writing to it later.
        PrintWriter outputWriter = null;
        try {
            outputWriter = new PrintWriter("output.txt", "UTF-8");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Run the apriori algorithm to efficiently find the frequent itemsets.
        System.out.println("Getting frequent itemsets...");
        Set<Set<String>> frequentItemsets =
            inputParser.getDataset().aprioriAlgorithm(
                inputParser.getMinSupport(),
                inputParser.getMinConfidence());
        printFrequentItemsets(dataset, minSupport, frequentItemsets, outputWriter);
        System.out.println("Getting frequent itemsets...Done.");

        if (PRINT_RULES_TO_COMMANDLINE) {
            System.out.println();
        }
        if (PRINT_RULES_TO_OUTPUT_FILE) {
            outputWriter.println();
        }

        // Find high confidence rules.
        System.out.println("Getting high-confidence rules...");
        outputRules(dataset, frequentItemsets, minConfidence, outputWriter);
        System.out.println("Getting high-confidence rules...Done.");

        // Print frequent itemsets and high-confidence rules to output.txt.
        if (PRINT_RULES_TO_OUTPUT_FILE) {
            System.out.println("Writing frequent items to Output file.");
        }
        outputWriter.close();
    }


    public static void printNBlankLines(int n) {
        for (int i = 0; i < n; i++) {
            System.out.println();
        }
    }


    public static void printFrequentItemsets(Dataset dataset, double minSupport,
            Set<Set<String>> frequentItemsets, PrintWriter outputWriter) {
        if (PRINT_RULES_TO_COMMANDLINE) {
            System.out.println("==Frequent itemsets (min_sup=" +
                               (minSupport * 100) + "%)");
        }
        if (PRINT_RULES_TO_OUTPUT_FILE) {
            outputWriter.println("==Frequent itemsets (min_sup=" +
                                 (minSupport * 100) + "%)");
        }

        ArrayList<WordsetSupportPair> wordsetAndSupportList =
            new ArrayList<WordsetSupportPair>();
        for (Set<String> frequentItemset : frequentItemsets) {
            wordsetAndSupportList.add(
                new WordsetSupportPair(frequentItemset,
                                       dataset.getItemsetSupport(
                                           frequentItemset)));
        }

        Collections.sort(wordsetAndSupportList);
        for (WordsetSupportPair wordsetAndSupport : wordsetAndSupportList) {
            if (PRINT_RULES_TO_COMMANDLINE) {
                System.out.println(wordsetAndSupport);
            }
            if (PRINT_RULES_TO_OUTPUT_FILE) {
                outputWriter.println(wordsetAndSupport);
            }
        }
    }


    public static void outputRules(Dataset dataset, Set<Set<String>> set,
                                   double min_conf, PrintWriter outputWriter) {
        if (PRINT_RULES_TO_COMMANDLINE) {
            System.out.println("==High-confidence association rules " +
                               "(min_conf=" + min_conf * 100 + "%)");
        }
        if (PRINT_RULES_TO_OUTPUT_FILE) {
            outputWriter.println("==High-confidence association rules " +
                                 "(min_conf=" + min_conf * 100 + "%)");
        }

        //Take as input or read from global table

        for (Set<String> st : set) {
            check_association_rules(st, min_conf, dataset);
        }
        Collections.sort(rules_list);
        for (Rule_confidence rules : rules_list) {
            if (PRINT_RULES_TO_COMMANDLINE) {
                System.out.println(rules.formatted_assosciation_rule);
            }
            if (PRINT_RULES_TO_OUTPUT_FILE) {
                outputWriter.println(rules.formatted_assosciation_rule);
            }
        }
    }


    public  static void check_association_rules(Set<String> st, double min_conf, Dataset dataset ) {
        for (String s : st) {
            generate_left_and_check(st, s, min_conf, dataset);
        }
    }


    public static void generate_left_and_check(Set<String> st, String right, double min_conf, Dataset dataset) {
        Set<String> left = new HashSet<String>() ;
        Set<String> leftuniright = new HashSet<String>();

        for (String s : st) {
            if (!s.equals(right)) {
                left.add(s);
                leftuniright.add(s);
            }
        }
        Set<String> rightSet = new HashSet<String>();
        rightSet.add(right);
        double LHS, RHS;
        if (!left.isEmpty()) {
            LHS = dataset.getItemsetSupport(left);
            RHS = dataset.getItemsetSupport(rightSet);

            leftuniright.add(right);
            double LHSuniRHS = dataset.getItemsetSupport(leftuniright);

            double conf = 100 * (LHSuniRHS / LHS);
            double supp = 100 * LHSuniRHS;
            double interestingness = 100 * LHSuniRHS / (LHS * RHS);
            if (conf > 100 * min_conf) {
                // [diary] => [pen] (Conf: 100.0%, Supp: 75%)
                String rule = new String();
                rule += "[";
                int index = 1;
                for (String str : left) {
                    if (index != 1) {
                        rule += ",";
                    }
                    rule += str;
                    index++;
                }
                rule += "] => [" + right + "] ";
                String temp = "(Conf: " + conf + "%, Supp: " + supp + "%)";
                rule += temp;
                // System.out.println(rule);

                Rule_confidence obj = new Rule_confidence(rule, left, right, supp, conf, interestingness);
                rules_list.add(obj);
            }
        }
    }
}


class Rule_confidence implements Comparable<Rule_confidence> {
    String formatted_assosciation_rule;
    Set<String> left;
    String right;
    Double support;
    Double confidence;
    Double interestingness;

    public Rule_confidence(String formatted_rule, Set<String> left_side, String right_side, double supp, double conf, double interestingness) {
        formatted_assosciation_rule = formatted_rule;
        left = left_side;
        right = right_side;
        support = supp;
        confidence = conf;
        this.interestingness = interestingness;
    }

    public int compareTo(Rule_confidence rule) {
        return (rule.confidence).compareTo(this.confidence);
        // return (rule.interestingness).compareTo(this.interestingness);
    }
}


class WordsetSupportPair implements Comparable<WordsetSupportPair> {
    Set<String> wordset;
    Double support;

    public WordsetSupportPair(Set<String> wordset, Double support) {
        this.wordset = wordset;
        this.support = support;
    }


    @Override
    public int compareTo(WordsetSupportPair other) {
        if (other.support.compareTo(support) != 0) {
            return other.support.compareTo(support);
        } else {
            Iterator<String> thisIterator = wordset.iterator();
            Iterator<String> otherIterator = other.wordset.iterator();
            while (thisIterator.hasNext() || otherIterator.hasNext()) {
                if (thisIterator.hasNext() && !otherIterator.hasNext()) {
                    return 1;
                } else if (!thisIterator.hasNext() && otherIterator.hasNext()) {
                    return -1;
                } else if (thisIterator.hasNext() && otherIterator.hasNext()) {
                    String thisString = thisIterator.next();
                    String otherString = otherIterator.next();
                    if (thisString.equals(otherString)) {
                        continue;
                    } else {
                        return thisString.compareTo(otherString);
                    }
                }
            }
            return 0;
        }
    }


    // Very pretty compareTo method that sorts things by support, then # items,
    // then string length, then alphabetical order.
    /*
    @Override
    public int compareTo(WordsetSupportPair other) {
        if (other.support.compareTo(support) != 0) {
            return other.support.compareTo(support);
        } else if (wordset.size() != other.wordset.size()) {
            return wordset.size() - other.wordset.size();
        } else if (wordset.toString().length() !=
                   other.wordset.toString().length()) {
            return wordset.toString().length() -
                   other.wordset.toString().length();
        } else {
            return wordset.toString().compareTo(other.wordset.toString());
        }
    }
    */


    public String toString() {
        String wordsetString = "[";
        boolean firstItem = true;
        for (String word : wordset) {
            if (firstItem) {
                wordsetString += word;
                firstItem = false;
            } else {
                wordsetString += "," + word;
            }
        }
        wordsetString += "]";
        return wordsetString + ", " + (support * 100) + "%";
    }
}