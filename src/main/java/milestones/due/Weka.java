package milestones.due;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.supervised.instance.Resample;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import static utility.ImportProperties.*;

public class Weka {

    private static final String PROJECT1 = "BOOKKEEPER";
    private static final String PROJECT2 = "ZOOKEEPER";
    private static final String NOSELECTION = "No Selection";
    private static final String BESTFIRST = "Best First";

    static List<String[]> out = new ArrayList<>();

    public static void sampling(String train, String test, Classifier classifier, int mode, String project, double[] arrayDouble, int[] arrayInt) throws Exception {

        double defectiveInTraining = arrayDouble[0];
        double defectiveInTesting = arrayDouble[1];

        int counter = arrayInt[0];
        int sizeFinalCSV = arrayInt[1];

        DataSource source1 = new DataSource(train);
        DataSource source2 = new DataSource(test);

        Instances training = source1.getDataSet();
        Instances testing = source2.getDataSet();

        int numAttr = training.numAttributes();

        int[] intArray = new int[3];
        intArray[0] = counter;
        intArray[1] = sizeFinalCSV;
        intArray[2] = numAttr;

        if (mode == 0) { //NoSampling

            noSampling(classifier, training, testing, defectiveInTraining, defectiveInTesting, project, intArray);

        } else if (mode == 1) { //UnderSampling

            underSampling(classifier, training, testing, defectiveInTraining, defectiveInTesting, project, intArray);

        } else if (mode == 2) { //OverSampling

            overSampling(classifier, training, testing, defectiveInTraining, defectiveInTesting, project, intArray);

        } else { //Smote

            smote(classifier, training, testing, defectiveInTraining, defectiveInTesting, project, intArray);

        }

    }

    public static Evaluation withFilter(Classifier classifier, Instances training, Instances testing, Evaluation evaluation) throws Exception {

        AttributeSelection filter = new AttributeSelection();
        CfsSubsetEval cfsSubsetEval = new CfsSubsetEval();
        GreedyStepwise greedyStepwise = new GreedyStepwise();
        greedyStepwise.setSearchBackwards(true);
        filter.setEvaluator(cfsSubsetEval);
        filter.setSearch(greedyStepwise);
        filter.setInputFormat(training);

        Instances filtTrain = Filter.useFilter(training, filter);
        int numAttr = filtTrain.numAttributes();
        filtTrain.setClassIndex(numAttr - 1);
        Instances filtTest = Filter.useFilter(testing, filter);
        filtTest.setClassIndex(numAttr - 1);
        classifier.buildClassifier(filtTrain);
        evaluation.evaluateModel(classifier, filtTest);

        return evaluation;

    }

    public static void noSampling(Classifier classifier, Instances training, Instances testing, double defectiveInTraining, double defectiveInTesting, String project, int[] intArray) throws Exception {

        int counter = intArray[0];
        int sizeFinalCSV = intArray[1];
        int numAttr = intArray[2];

        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        classifier.buildClassifier(training);

        Evaluation eval = new Evaluation(testing);

        eval.evaluateModel(classifier, testing);

        String stringClassifier = determineClassifier(classifier);

        double division = (double) training.size() / (double) sizeFinalCSV;

        out.add(new String[]{project, String.valueOf(counter), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "No Sampling",
                NOSELECTION, String.valueOf(eval.truePositiveRate(1)), String.valueOf(eval.falsePositiveRate(1)), String.valueOf(eval.trueNegativeRate(1)),
                String.valueOf(eval.falseNegativeRate(1)), String.valueOf(eval.precision(1)), String.valueOf(eval.recall(1)), String.valueOf(eval.areaUnderROC(1)), String.valueOf(eval.kappa())});

        Evaluation evaluationWithFilter = withFilter(classifier, training, testing, eval);

        out.add(new String[]{project, String.valueOf(counter), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "No Sampling",
                BESTFIRST, String.valueOf(evaluationWithFilter.truePositiveRate(1)), String.valueOf(evaluationWithFilter.falsePositiveRate(1)), String.valueOf(evaluationWithFilter.trueNegativeRate(1)),
                String.valueOf(evaluationWithFilter.falseNegativeRate(1)), String.valueOf(evaluationWithFilter.precision(1)), String.valueOf(evaluationWithFilter.recall(1)), String.valueOf(evaluationWithFilter.areaUnderROC(1)), String.valueOf(evaluationWithFilter.kappa())});


    }

    public static void underSampling(Classifier classifier, Instances training, Instances testing, double defectiveInTraining, double defectiveInTesting, String project, int[] intArray1) throws Exception {

        int sizeFinalCSV = intArray1[1];
        int numAttr = intArray1[2];

        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        int count = intArray1[0];

        classifier.buildClassifier(training);

        String stringClassifier = determineClassifier(classifier);

        Resample resample = new Resample();
        resample.setInputFormat(training);

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(classifier);

        SpreadSubsample spreadSubsample = new SpreadSubsample();
        String[] opts = new String[]{"-M", "1.0"};
        spreadSubsample.setOptions(opts);
        fc.setFilter(spreadSubsample);

        fc.buildClassifier(training);
        Evaluation eval = new Evaluation(testing);
        eval.evaluateModel(fc, testing);
        double division = (double) training.size() / (double) sizeFinalCSV;


        out.add(new String[]{project, String.valueOf(count), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "Under Sampling",
                NOSELECTION, String.valueOf(eval.truePositiveRate(1)), String.valueOf(eval.falsePositiveRate(1)), String.valueOf(eval.trueNegativeRate(1)),
                String.valueOf(eval.falseNegativeRate(1)), String.valueOf(eval.precision(1)), String.valueOf(eval.recall(1)), String.valueOf(eval.areaUnderROC(1)), String.valueOf(eval.kappa())});

        Evaluation evaluationWithFilter = withFilter(classifier, training, testing, eval);

        out.add(new String[]{project, String.valueOf(count), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "Under Sampling",
                BESTFIRST, String.valueOf(evaluationWithFilter.truePositiveRate(1)), String.valueOf(evaluationWithFilter.falsePositiveRate(1)), String.valueOf(evaluationWithFilter.trueNegativeRate(1)),
                String.valueOf(evaluationWithFilter.falseNegativeRate(1)), String.valueOf(evaluationWithFilter.precision(1)), String.valueOf(evaluationWithFilter.recall(1)), String.valueOf(evaluationWithFilter.areaUnderROC(1)), String.valueOf(evaluationWithFilter.kappa())});


    }

    public static void overSampling(Classifier classifier, Instances training, Instances testing, double defectiveInTraining, double defectiveInTesting, String project, int[] intArray) throws Exception {

        int counter = intArray[0];
        int sizeFinalCSV = intArray[1];
        int numAttr = intArray[2];

        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        classifier.buildClassifier(training);

        String stringClassifier = determineClassifier(classifier);

        Resample resample = new Resample();
        resample.setInputFormat(training);

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(classifier);

        double percent = (1 - defectiveInTraining)*100;

        String[] options = new String[4];
        options[0] = "-B";
        options[1] = "1.0";
        options[2] = "-Z";
        options[3] = String.valueOf(percent);

        resample.setOptions(options);
        fc.setFilter(resample);
        fc.buildClassifier(training);

        Evaluation evaluation = new Evaluation(testing);
        evaluation.evaluateModel(fc, testing);

        double division = (double) training.size() / (double) sizeFinalCSV;

        out.add(new String[]{project, String.valueOf(counter), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "Over Sampling",
                NOSELECTION, String.valueOf(evaluation.truePositiveRate(1)), String.valueOf(evaluation.falsePositiveRate(1)), String.valueOf(evaluation.trueNegativeRate(1)),
                String.valueOf(evaluation.falseNegativeRate(1)), String.valueOf(evaluation.precision(1)), String.valueOf(evaluation.recall(1)), String.valueOf(evaluation.areaUnderROC(1)), String.valueOf(evaluation.kappa())});

        Evaluation evaluationWithFilter = withFilter(classifier, training, testing, evaluation);

        out.add(new String[]{project, String.valueOf(counter), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "Over Sampling",
                BESTFIRST, String.valueOf(evaluationWithFilter.truePositiveRate(1)), String.valueOf(evaluationWithFilter.falsePositiveRate(1)), String.valueOf(evaluationWithFilter.trueNegativeRate(1)),
                String.valueOf(evaluationWithFilter.falseNegativeRate(1)), String.valueOf(evaluationWithFilter.precision(1)), String.valueOf(evaluationWithFilter.recall(1)), String.valueOf(evaluationWithFilter.areaUnderROC(1)), String.valueOf(evaluationWithFilter.kappa())});


    }

    public static void smote(Classifier classifier, Instances training, Instances testing, double defectiveInTraining, double defectiveInTesting, String project, int[] intArray) throws Exception {

        int counter = intArray[0];
        int sizeFinalCSV = intArray[1];
        int numAttr = intArray[2];

        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        String stringClassifier = determineClassifier(classifier);

        Resample resample = new Resample();
        resample.setInputFormat(training);
        FilteredClassifier filteredClassifier = new FilteredClassifier();

        SMOTE smote = new SMOTE();
        smote.setInputFormat(training);
        filteredClassifier.setFilter(smote);
        filteredClassifier.setClassifier(classifier);
        filteredClassifier.buildClassifier(training);

        Evaluation evaluation = new Evaluation(testing);
        evaluation.evaluateModel(filteredClassifier, testing);


        double division = (double) training.size() / (double) sizeFinalCSV;

        out.add(new String[]{project, String.valueOf(counter), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "Smote",
                NOSELECTION, String.valueOf(evaluation.truePositiveRate(1)), String.valueOf(evaluation.falsePositiveRate(1)), String.valueOf(evaluation.trueNegativeRate(1)),
                String.valueOf(evaluation.falseNegativeRate(1)), String.valueOf(evaluation.precision(1)), String.valueOf(evaluation.recall(1)), String.valueOf(evaluation.areaUnderROC(1)), String.valueOf(evaluation.kappa())});

        Evaluation evaluationWithFilter = withFilter(classifier, training, testing, evaluation);

        out.add(new String[]{project, String.valueOf(counter), String.valueOf((division) * 100), String.valueOf(defectiveInTraining), String.valueOf(defectiveInTesting), stringClassifier, "Smote",
                BESTFIRST, String.valueOf(evaluationWithFilter.truePositiveRate(1)), String.valueOf(evaluationWithFilter.falsePositiveRate(1)), String.valueOf(evaluationWithFilter.trueNegativeRate(1)),
                String.valueOf(evaluationWithFilter.falseNegativeRate(1)), String.valueOf(evaluationWithFilter.precision(1)), String.valueOf(evaluationWithFilter.recall(1)), String.valueOf(evaluationWithFilter.areaUnderROC(1)), String.valueOf(evaluationWithFilter.kappa())});

    }

    public static double yesPercentage(CSVReader csvReader) throws IOException {

        double numberOfYes = 0;
        double sizeList;

        csvReader.readNext();
        List<String[]> list = csvReader.readAll();
        sizeList = list.size();
        for (String[] strings : list) {

            if (strings[11].equals("YES")) {

                numberOfYes++;

            }

        }

        return numberOfYes / sizeList;

    }

    private static String determineClassifier(Classifier classifier) {

        String stringClassifier = null;

        if (classifier.getClass() == NaiveBayes.class) {

            stringClassifier = "Naive Bayes";

        } else if (classifier.getClass() == RandomForest.class) {

            stringClassifier = "Random Forest";

        } else if (classifier.getClass() == IBk.class) {

            stringClassifier = "IBK";

        }

        return stringClassifier;
    }

    public static void executeWalkForward(int numOfRuns, String project) throws Exception {

        int sizeFinalCSV;

        try (FileReader fileReader = new FileReader(getFinalCSV()); CSVReader csvReader = new CSVReader(fileReader)) {

            sizeFinalCSV = csvReader.readAll().size();

        }

        String test;
        String train;

        String testCSV;
        String trainCSV;

        double defectiveInTraining = 0;
        double defectiveInTesting = 0;

        //Writing the header
        out.add(new String[]{"Dataset", "#TrainingRelease", "%Training", "%Defective in Training", "%Defective in Testing", "Classifier", "Balancing", "Feature Selection",
                "True Positive", "False Positive", "True Negative", "False Negative", "Precision", "Recall",
                "ROC Area", "Kappa"});

        for (int i = 2; i < numOfRuns + 1; i++) {

            test = getTestDirArff() + i + ".arff";
            train = getTrainDirArff() + i + ".arff";
            testCSV = getTestDir() + i + ".csv";
            trainCSV = getTrainDir() + i + ".csv";

            try (FileReader fileReader = new FileReader(testCSV); CSVReader csvReader = new CSVReader(fileReader);
                 FileReader fileReader1 = new FileReader(trainCSV); CSVReader csvReader1 = new CSVReader(fileReader1);) {

                defectiveInTraining = yesPercentage(csvReader);
                defectiveInTesting = yesPercentage(csvReader1);

                double[] arrayDouble = new double[2];
                arrayDouble[0] = defectiveInTraining;
                arrayDouble[1] = defectiveInTesting;

                int[] arrayInt = new int[2];
                arrayInt[0] = i;
                arrayInt[1] = sizeFinalCSV;

                Classifier[] classifiers = new Classifier[3];
                classifiers[0] = new NaiveBayes();
                classifiers[1] = new IBk();
                classifiers[2] = new RandomForest();

                for (int j = 0; j < classifiers.length; j++) {

                            sampling(train, test, classifiers[j], 0, project, arrayDouble, arrayInt); //noSampling
                            sampling(train, test, classifiers[j], 1, project, arrayDouble, arrayInt); //underSampling
                            sampling(train, test, classifiers[j], 2, project, arrayDouble, arrayInt); //overSampling
                            sampling(train, test, classifiers[j], 3, project, arrayDouble, arrayInt); //Smote

                }

            }

        }

    }

    public static void writeFinalCSV() {

        try (FileWriter fileWriter = new FileWriter(getM2final()); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            csvWriter.writeAll(out);
            out.clear();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }


    public static void main(String[] args) throws Exception {

        importProp(PROJECT1);

        int numOfRuns1 = 0;

        try(FileReader fileReader = new FileReader(getVersionInfo());
            CSVReader csvReader = new CSVReader(fileReader);
        ){

            csvReader.readNext();
            List<String[]> listVersion = csvReader.readAll();
            numOfRuns1 = listVersion.size()/2;

        }catch(IOException e){

            e.printStackTrace();

        }

        executeWalkForward(numOfRuns1, PROJECT1);
        writeFinalCSV();

        importProp(PROJECT2);

        int numOfRuns2 = 0;

        try (FileReader fileReader = new FileReader(getVersionInfo());
             CSVReader csvReader = new CSVReader(fileReader)
        ) {

            csvReader.readNext();
            List<String[]> listVersion = csvReader.readAll();
            numOfRuns2 = listVersion.size() / 2;

        } catch (IOException e) {

            e.printStackTrace();

        }

        executeWalkForward(numOfRuns2, PROJECT2);
        writeFinalCSV();

    }

}
