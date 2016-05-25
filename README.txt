=================================================================
|                           Project 3                           |
|                                                               |
|Team Members: Vibhor Gaur (vg2376) and Joaquín Ruales (jar2262)|
=================================================================

-----------------------------------------
List of all files that we are submitting:
-----------------------------------------
- Code:
    
    - RuleMiningMain.java
    - InputParser.java
    - Dataset.java
    - data_preprocessing.ipynb

- Input file:

  - INTEGRATED-DATASET.csv

- Output file:

  - output.txt

- Output of Interesting sample run:
 
  - example-run.txt

- Documentation:

    - README.txt


-----------------------------------------------------------------------------
NYC Open Data data set(s) you used to generate the INTEGRATED-DATASET file
-----------------------------------------------------------------------------
We used the NYC Leading Causes of Death dataset available here:
https://data.cityofnewyork.us/Health/New-York-City-Leading-Causes-of-Death/jb7j-dtam

-----------------------------------------------------------------------------
High-level procedure you used to map the original NYC Open Data data set(s) into your INTEGRATED-DATASET file
-----------------------------------------------------------------------------
The original dataset had the columns "Year", "Ethnicity", "Sex", "Cause of Death", "Count", and "Percent".

Because the apriori algorithm computes counts and percentages for support based on how many times a row is repeated and not on explicit count values, we couldn't keep the "Count" or "Percent" column, but instead it was necessary to repeat each row as many times as stated in the row under "Count". Our final dataset has 714862 rows.

In summary, these are the steps we took:
  - Create an empty INTEGRATED-DATASET file and open it for writing
  - For each row:
     - read the "Count" value
     - Repeat "Count" number of times:
       - Write the "Year", "Ethnicity", "Sex", "Cause of Death" values of this row into a new row in the INTEGRATED-DATASET file, concatenating the column title before the value for clarity (e.g. if column "Year" has value "2010" we add "Year:2010").

For example, for the dataset:
  Year,Ethnicity,Sex,Cause of Death,Count,Percent
  2011,ASIAN & PACIFIC ISLANDER,FEMALE,ANEMIAS,5,0

We would output the INTEGRATED-DATASET
  Year:2011,Ethnicity:ASIAN & PACIFIC ISLANDER,Sex:FEMALE,Cause of Death:ANEMIAS
  Year:2011,Ethnicity:ASIAN & PACIFIC ISLANDER,Sex:FEMALE,Cause of Death:ANEMIAS
  Year:2011,Ethnicity:ASIAN & PACIFIC ISLANDER,Sex:FEMALE,Cause of Death:ANEMIAS
  Year:2011,Ethnicity:ASIAN & PACIFIC ISLANDER,Sex:FEMALE,Cause of Death:ANEMIAS
  Year:2011,Ethnicity:ASIAN & PACIFIC ISLANDER,Sex:FEMALE,Cause of Death:ANEMIAS


-----------------------------------------------------------------------------
Justification for choice of INTEGRATED-DATASET (why is it interesting)
-----------------------------------------------------------------------------
The dataset chosen by us to create the INTEGRATED_DATASET is interesting because we can use it to find trends or patterns in the causes of deaths among various ethnic groups and genders or whether a particular cause is predominantly present in a particular group/gender. Also, we can compare the results to analyze that a particular cause of death is more prominent among a particular ethnic group/gender over another group/gender.

These results can help policymakers determine where to allocate money for preventive measures to extend or improve the life of citizens. Similarly, these results can be integrated into a medical system to target treatment to a particular patient's needs and again implement preventive measures for likely causes of death.

Additionally, since our integrated dataset has 700000+ rows, the results become even more interesting, because even a support as low as 1% means a sample size of 7000+ people, which gives us more statistical power and helps so that our association rules can't be dismissed as outliers.


--------------------------------------------------------------------
A clear description of how to run our program (in the clic machines)
--------------------------------------------------------------------
To compile and run, use:
> bash run.sh <filename of INTEGRATED-DATASET> <min_sup> <min_conf>

For example:
> bash run.sh INTEGRATED-DATASET.csv 0.3 0.5 


-------------------------------------------------------
Command line specification of an interesting sample run
-------------------------------------------------------
> bash run.sh INTEGRATED-DATASET.csv 0.01 0.5

That is, the interesting sample run uses a min_support of 0.01 and a min_conf of 0.5. In the next section, we show why the results are interesting.


-----------------------------------------------------------------------------
Some interesting rules found from our INTEGRATED-DATASET
-----------------------------------------------------------------------------
We found several interesting rules from our example-run.txt (which, as mentioned, uses min_support=0.01 and min_conf=0.5). Some of them are (with a brief description of why they are interesting):

[Cause of Death:DISEASES OF HEART] => [Sex:FEMALE] (Conf: 54.04827407215157%, Supp: 21.84844326379534%)

This rule indicates that ~54% (or equivalently ~156K out of ~289K) of the recorded people with Diseases of Heart deaths are female. Given the large sample size (~289K records), we are certain that this condition is reported more often for females than for males. This could mean that females are more prone to heart diseases, but requires further investigation of possible causes for this association rule.

----

[Cause of Death:PSYCH. SUBSTANCE USE & ACCIDENTAL DRUG POISONING] => [Sex:MALE] (Conf: 72.23905260258883%, Supp: 1.100773437073781%)

This rule indicates that ~72% (or equivalently ~7.8K out of ~10.9K) of the recorded people with Psych Substance Use & Accidental Drug Poisoning deaths are male. Given the large sample size (~10.9K records), we are certain that this condition is reported more often for males than for females. This could mean that males are more prone to Psych Substance Use & Accidental Drug Poisoning, but requires further investigation of possible causes for this association rule.

----

[Cause of Death:HUMAN IMMUNODEFICIENCY VIRUS DISEASE] => [Ethnicity:NON-HISPANIC BLACK] (Conf: 56.45509499136442%, Supp: 1.0974161410400065%)

This rule indicates that ~56% (or equivalently ~7.8K out of ~13.9K) of people reported as dying with HIV were non-hispanic black, and this percentage is higher than any other ethnic group. Given the large sample size (~13.9K records), we are certain that this condition is reported more often for non-hispanic blacks than for other ethnic groups. This result is made even more surprising by the fact that support({Ethnicity:NON-HISPANIC BLACK}), 26.22%, which means that, even though this ethnicity constitutes a relatively moderate percentage of the recorded deaths overall, it constitutes a large percentage of the HIV deaths. This could mean that Non-Hispanic blacks are more prone to HIV than other ethnic groups, but requires further investigation of possible causes for this association rule.

-----------------------------------------------------------------------
A clear description of the internal design of our project for each part
-----------------------------------------------------------------------
Our project is designed as follows

>>>>Algorithm Design<<<<

The implemented algorithm is the one in Section 2.1 of Rakesh Agrawal and Ramakrishnan Srikant's 1994 paper "Fast Algorithms for Mining Association Rules in Large Databases" given in the Lecture schedules and readings as per the guidelines given in the project.

The only difference is that we don't use the "subset" function using hash trees described in section 2.1.2. Instead, for that part we simply use hashsets and still achieve a good performance even for out dataset of 70K+ rows.

>>>>Code Design<<<<

NOTE: The apriori algorithm is implemented in Dataset.java inside the method aprioriAlgorithm. This method gets called by the main program at RuleMiningMain.

RuleMiningMain.java
--------------
The main file which generates frequent itemsets with specified threshold value of support and listed in decreasing order of their support.

This file also generates high confidence association rules with their support and confidence, listed in decreasing order of confidence.

InputParser.java
----------
This class parses the dataset filename, minimum support and minimum confidence arguments,
saves them into variables, and loads the dataset from the provided
filename. It throws an error and exits the program if there's any parse error
or file not found error.

Dataset.Java 
-----------------
Contains implementation of the apriori algorithm.

run.sh
------
Bash commands to run the project.

----------------------------------------------------
Additional information that we consider significant.
----------------------------------------------------
N/A

