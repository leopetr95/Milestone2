package utility;

import java.io.File;

public class CreateDir {

    static String project1 = "BOOKKEEPER";
    static String project2 = "ZOOKEEPER";
    final private static String PATH = "C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\";

    public static void createDirectory(String projectName){

        new File(PATH + projectName + "files\\").mkdirs();
        new File(PATH + projectName + "files\\metricsFiles").mkdirs();
        new File(PATH + projectName + "files\\csvTrain").mkdirs();
        new File(PATH + projectName + "files\\csvTest").mkdirs();
        new File(PATH + projectName + "files\\arffTrain").mkdirs();
        new File(PATH + projectName + "files\\arffTest").mkdirs();

    }

    public static void main(String[] args){

        createDirectory(project1);


    }

}
