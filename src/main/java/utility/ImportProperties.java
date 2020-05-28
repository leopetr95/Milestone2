package utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ImportProperties {

    private ImportProperties(){
    }

    public static String getStringadiprova() {
        return stringadiprova;
    }

    public static void setStringadiprova(String stringadiprova) {
        ImportProperties.stringadiprova = stringadiprova;
    }

    private static String stringadiprova = "";

    public static String csvClassPath = "";
    public static String cPath = "";
    public static String csvBlame = "";
    public static String stringToReplace = "";
    public static String blameCommitIntersection = "";
    public static String blameJiraIntersection = "";
    public static String blameJiraIntersectionFVOnly = "";
    public static String blameJiraIntersectionOV = "";
    public static String blameJiraIntersectionOVFVOnly = "";
    public static String predictedIVCSV = "";
    public static String versionInfo = "";
    public static String buggy = "";
    public static String csvCommit = "";
    public static String completePath = "";
    public static String versionsAV = "";
    public static String versionsFVOnly = "";
    public static String path = "";
    public static String urlProject = "";
    public static String sumBuggyPredicted = "";
    public static String preFinalCsv = "";
    public static String outLoc = "";
    public static String maxAverage = "";
    public static String outLocIndex = "";
    public static String sizeMetric = "";
    public static String chgSetSize = "";
    public static String maxAvgChgSetSize = "";
    public static String finalCSV = "";

    public static void importProp(String projName) {

        String inputProp = "";

        if(projName.equals("BOOKKEEPER")){

            inputProp = "C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\stringPathBook.properties";

        }else if(projName.equals("ZOOKEEPER")){

            inputProp = "C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\stringPathZook.properties";

        }

            try (InputStream inputStream = new FileInputStream(inputProp)) {

                Properties properties = new Properties();
                properties.load(inputStream);

                csvClassPath = properties.getProperty("csvClassPath");
                cPath = properties.getProperty("cPath");
                csvBlame = properties.getProperty("csvBlame");
                stringToReplace = properties.getProperty("stringToReplace");
                blameJiraIntersection = properties.getProperty("blameJiraIntersection");
                blameJiraIntersectionFVOnly = properties.getProperty("blameJiraIntersectionFVOnly");
                blameJiraIntersectionOV = properties.getProperty("blameJiraIntersectionOV");
                blameJiraIntersectionOVFVOnly = properties.getProperty("blameJiraIntersectionOVFVOnly");
                predictedIVCSV = properties.getProperty("predictedIVCSV");
                versionInfo = properties.getProperty("versionInfo");
                buggy = properties.getProperty("buggy");
                csvCommit = properties.getProperty("csvCommit");
                completePath = properties.getProperty("completePath");
                versionsAV = properties.getProperty("versionsAV");
                versionsFVOnly = properties.getProperty("versionsFVOnly");
                path = properties.getProperty("path");
                blameCommitIntersection = properties.getProperty("blameCommitIntersection");
                urlProject= properties.getProperty("urlProject");
                sumBuggyPredicted = properties.getProperty("sumBuggyPredicted");
                preFinalCsv = properties.getProperty("preFinalCsv");
                outLoc = properties.getProperty("outLoc");
                maxAverage = properties.getProperty("maxAverage");
                outLocIndex = properties.getProperty("outLocIndex");
                sizeMetric = properties.getProperty("sizeMetric");
                chgSetSize = properties.getProperty("chgSetSize");
                maxAvgChgSetSize = properties.getProperty("maxAvgChgSetSize");
                finalCSV = properties.getProperty("finalCSV");
                stringadiprova = properties.getProperty("finalCSV");

            } catch (IOException e) {

                e.printStackTrace();

            }

    }

}
