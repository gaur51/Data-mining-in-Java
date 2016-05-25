rm -f *.class;
javac RuleMiningMain.java;
DATASET_FILENAME="$1";
MIN_SUP=$2;
MIN_CONF=$3;
java RuleMiningMain "${DATASET_FILENAME}" $MIN_SUP $MIN_CONF;
rm -f *.class;