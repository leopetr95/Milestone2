package milestones.due;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utility.ImportProperties.*;

public class ConvertToArff{

    private static final String PROJECT1 = "BOOKKEEPER";
    private static final String PROJECT2 = "ZOOKEEPER";


    public static void convert(String pathCsv, String pathArff){

        CSVLoader csvLoader = new CSVLoader();
        try {
            csvLoader.setSource(new File(pathCsv));
            Instances data = csvLoader.getDataSet();

            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(pathArff));
            saver.writeBatch();

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    public static void main(String[] args){

        for(int i = 1; i < 3; i++){

            if (i == 1){

                importProp(PROJECT1);

            }else if(i == 2){

                importProp(PROJECT2);

            }

            try (Stream<Path> walk = Files.walk(Paths.get(getTestDir().substring(0, getTestDir().length()-4)));
                 Stream<Path> walk1 = Files.walk(Paths.get(getTrainDir().substring(0, getTrainDir().length()-5)))
            ) {

                List<String> result = walk.filter(Files::isRegularFile)
                        .map(Path::toString).collect(Collectors.toList());

                for(String string: result){

                    String string1 = string.replace("csvTest", "arffTest");
                    String string2 = string1.replace("csv", "arff");
                    convert(string, string2);

                }

                List<String> result1 = walk1.filter(Files::isRegularFile)
                        .map(Path::toString).collect(Collectors.toList());

                for(String string: result1){

                    String string1 = string.replace("csvTrain", "arffTrain");
                    String string2 = string1.replace("csv", "arff");
                    convert(string, string2);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

}
