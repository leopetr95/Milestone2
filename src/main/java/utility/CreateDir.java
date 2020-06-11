package utility;

import java.io.File;

public class CreateDir {

    static String project1 = "BOOKKEEPER";
    static String project2 = "ZOOKEEPER";

    public static void createDirectory(String projectName){

        new File("C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\" + projectName + "files\\").mkdirs();
        new File("C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\" + projectName + "files\\metricsFiles").mkdirs();
        new File("C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\" + projectName + "files\\csvTrain").mkdirs();
        new File("C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\" + projectName + "files\\csvTest").mkdirs();
        new File("C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\" + projectName + "files\\arffTrain").mkdirs();
        new File("C:\\Users\\leona\\Desktop\\Project2\\src\\main\\resources\\" + projectName + "files\\arffTest").mkdirs();

    }

    public static void main(String[] args){

        createDirectory(project1);

    }

}
