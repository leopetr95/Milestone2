package utility;

import java.io.File;

public class CreateDir {

    static String project1 = "BOOKKEEPER";
    static String project2 = "ZOOKEEPER";
    private static String path = "C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\";

    public static void createDirectory(String projectName){

        new File(path + projectName + "files\\").mkdirs();
        new File(path + projectName + "files\\metricsFiles").mkdirs();
        new File(path + projectName + "files\\csvTrain").mkdirs();
        new File(path + projectName + "files\\csvTest").mkdirs();
        new File(path + projectName + "files\\arffTrain").mkdirs();
        new File(path + projectName + "files\\arffTest").mkdirs();

    }

    public static void main(String[] args){

        createDirectory(project1);


    }

}
