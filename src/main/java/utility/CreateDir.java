package utility;

import java.io.File;

import static utility.ImportProperties.getAnotherPath;

public class CreateDir {

    static String project1 = "BOOKKEEPER";
    static String project2 = "ZOOKEEPER";

    public static void createDirectory(String projectName){

        new File(getAnotherPath() + projectName + "files\\").mkdirs();
        new File(getAnotherPath() + projectName + "files\\metricsFiles").mkdirs();
        new File(getAnotherPath() + projectName + "files\\csvTrain").mkdirs();
        new File(getAnotherPath() + projectName + "files\\csvTest").mkdirs();
        new File(getAnotherPath() + projectName + "files\\arffTrain").mkdirs();
        new File(getAnotherPath() + projectName + "files\\arffTest").mkdirs();

    }

    public static void main(String[] args){

        createDirectory(project1);


    }

}
