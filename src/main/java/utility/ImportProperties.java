package utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ImportProperties {

    private ImportProperties(){
    }

    public static String getCsvClassPath() {
        return csvClassPath;
    }

    public static void setCsvClassPath(String csvClassPath) {
        ImportProperties.csvClassPath = csvClassPath;
    }

    public static String getcPath() {
        return cPath;
    }

    public static void setcPath(String cPath) {
        ImportProperties.cPath = cPath;
    }

    public static String getCsvBlame() {
        return csvBlame;
    }

    public static void setCsvBlame(String csvBlame) {
        ImportProperties.csvBlame = csvBlame;
    }

    public static String getStringToReplace() {
        return stringToReplace;
    }

    public static void setStringToReplace(String stringToReplace) {
        ImportProperties.stringToReplace = stringToReplace;
    }

    public static String getBlameCommitIntersection() {
        return blameCommitIntersection;
    }

    public static void setBlameCommitIntersection(String blameCommitIntersection) {
        ImportProperties.blameCommitIntersection = blameCommitIntersection;
    }

    public static String getBlameJiraIntersection() {
        return blameJiraIntersection;
    }

    public static void setBlameJiraIntersection(String blameJiraIntersection) {
        ImportProperties.blameJiraIntersection = blameJiraIntersection;
    }

    public static String getBlameJiraIntersectionFVOnly() {
        return blameJiraIntersectionFVOnly;
    }

    public static void setBlameJiraIntersectionFVOnly(String blameJiraIntersectionFVOnly) {
        ImportProperties.blameJiraIntersectionFVOnly = blameJiraIntersectionFVOnly;
    }

    public static String getBlameJiraIntersectionOV() {
        return blameJiraIntersectionOV;
    }

    public static void setBlameJiraIntersectionOV(String blameJiraIntersectionOV) {
        ImportProperties.blameJiraIntersectionOV = blameJiraIntersectionOV;
    }

    public static String getBlameJiraIntersectionOVFVOnly() {
        return blameJiraIntersectionOVFVOnly;
    }

    public static void setBlameJiraIntersectionOVFVOnly(String blameJiraIntersectionOVFVOnly) {
        ImportProperties.blameJiraIntersectionOVFVOnly = blameJiraIntersectionOVFVOnly;
    }

    public static String getPredictedIVCSV() {
        return predictedIVCSV;
    }

    public static void setPredictedIVCSV(String predictedIVCSV) {
        ImportProperties.predictedIVCSV = predictedIVCSV;
    }

    public static String getVersionInfo() {
        return versionInfo;
    }

    public static void setVersionInfo(String versionInfo) {
        ImportProperties.versionInfo = versionInfo;
    }

    public static String getBuggy() {
        return buggy;
    }

    public static void setBuggy(String buggy) {
        ImportProperties.buggy = buggy;
    }

    public static String getCsvCommit() {
        return csvCommit;
    }

    public static void setCsvCommit(String csvCommit) {
        ImportProperties.csvCommit = csvCommit;
    }

    public static String getCompletePath() {
        return completePath;
    }

    public static void setCompletePath(String completePath) {
        ImportProperties.completePath = completePath;
    }

    public static String getVersionsAV() {
        return versionsAV;
    }

    public static void setVersionsAV(String versionsAV) {
        ImportProperties.versionsAV = versionsAV;
    }

    public static String getVersionsFVOnly() {
        return versionsFVOnly;
    }

    public static void setVersionsFVOnly(String versionsFVOnly) {
        ImportProperties.versionsFVOnly = versionsFVOnly;
    }

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        ImportProperties.path = path;
    }

    public static String getUrlProject() {
        return urlProject;
    }

    public static void setUrlProject(String urlProject) {
        ImportProperties.urlProject = urlProject;
    }

    public static String getSumBuggyPredicted() {
        return sumBuggyPredicted;
    }

    public static void setSumBuggyPredicted(String sumBuggyPredicted) {
        ImportProperties.sumBuggyPredicted = sumBuggyPredicted;
    }

    public static String getPreFinalCsv() {
        return preFinalCsv;
    }

    public static void setPreFinalCsv(String preFinalCsv) {
        ImportProperties.preFinalCsv = preFinalCsv;
    }

    public static String getOutLoc() {
        return outLoc;
    }

    public static void setOutLoc(String outLoc) {
        ImportProperties.outLoc = outLoc;
    }

    public static String getMaxAverage() {
        return maxAverage;
    }

    public static void setMaxAverage(String maxAverage) {
        ImportProperties.maxAverage = maxAverage;
    }

    public static String getOutLocIndex() {
        return outLocIndex;
    }

    public static void setOutLocIndex(String outLocIndex) {
        ImportProperties.outLocIndex = outLocIndex;
    }

    public static String getSizeMetric() {
        return sizeMetric;
    }

    public static void setSizeMetric(String sizeMetric) {
        ImportProperties.sizeMetric = sizeMetric;
    }

    public static String getChgSetSize() {
        return chgSetSize;
    }

    public static void setChgSetSize(String chgSetSize) {
        ImportProperties.chgSetSize = chgSetSize;
    }

    public static String getMaxAvgChgSetSize() {
        return maxAvgChgSetSize;
    }

    public static void setMaxAvgChgSetSize(String maxAvgChgSetSize) {
        ImportProperties.maxAvgChgSetSize = maxAvgChgSetSize;
    }

    public static String getFinalCSV() {
        return finalCSV;
    }

    public static void setFinalCSV(String finalCSV) {
        ImportProperties.finalCSV = finalCSV;
    }

    private static String csvClassPath = "";
    private static String cPath = "";
    private static String csvBlame = "";
    private static String stringToReplace = "";
    private static String blameCommitIntersection = "";
    private static String blameJiraIntersection = "";
    private static String blameJiraIntersectionFVOnly = "";
    private static String blameJiraIntersectionOV = "";
    private static String blameJiraIntersectionOVFVOnly = "";
    private static String predictedIVCSV = "";
    private static String versionInfo = "";
    private static String buggy = "";
    private static String csvCommit = "";
    private static String completePath = "";
    private static String versionsAV = "";
    private static String versionsFVOnly = "";
    private static String path = "";
    private static String urlProject = "";
    private static String sumBuggyPredicted = "";
    private static String preFinalCsv = "";
    private static String outLoc = "";
    private static String maxAverage = "";
    private static String outLocIndex = "";
    private static String sizeMetric = "";
    private static String chgSetSize = "";
    private static String maxAvgChgSetSize = "";
    private static String finalCSV = "";
    private static String testDir = "";
    private static String trainDir = "";
    private static String testDirArff = "";
    private static String trainDirArff = "";
    private static String m2final = "";
    private static String anotherPath = "";

    public static String getAnotherPath() {
        return anotherPath;
    }

    public static void setAnotherPath(String anotherPath) {
        ImportProperties.anotherPath = anotherPath;
    }

    public static String getM2final() {
        return m2final;
    }

    public static void setM2final(String m2final) {
        ImportProperties.m2final = m2final;
    }

    public static String getTestDirArff() {
        return testDirArff;
    }

    public static void setTestDirArff(String testDirArff) {
        ImportProperties.testDirArff = testDirArff;
    }

    public static String getTrainDirArff() {
        return trainDirArff;
    }

    public static void setTrainDirArff(String trainDirArff) {
        ImportProperties.trainDirArff = trainDirArff;
    }

    public static String getTestDir() {
        return testDir;
    }

    public static void setTestDir(String testDir) {
        ImportProperties.testDir = testDir;
    }

    public static String getTrainDir() {
        return trainDir;
    }

    public static void setTrainDir(String trainDir) {
        ImportProperties.trainDir = trainDir;
    }

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
                testDir = properties.getProperty("testDir");
                trainDir = properties.getProperty("trainDir");
                testDirArff = properties.getProperty("testDirArff");
                trainDirArff = properties.getProperty("trainDirArff");
                m2final = properties.getProperty("m2final");
                anotherPath = properties.getProperty("anotherPath");

            } catch (IOException e) {

                e.printStackTrace();

            }

    }

}
