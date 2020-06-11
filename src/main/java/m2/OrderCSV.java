package m2;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import entity.SubValues;
import entity.Values;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import static utility.ImportProperties.*;
import static utility.ImportProperties.getFinalCSV;

public class OrderCSV {

    private static String string;
    private static final String PROJECT1 = "BOOKKEEPER";

    public static void divideForTraining(String pathFinal, int countRun, String path){
        
        try(FileReader fileReader = new FileReader(pathFinal);
            CSVReader csvReader = new CSVReader(fileReader);
            FileWriter fileWriter = new FileWriter(path + countRun + ".csv");
            CSVWriter csvWriter = new CSVWriter(fileWriter);){

            String[] header = csvReader.readNext();
            List<String[]> listFinal = csvReader.readAll();
            List<String[]> listWrite = new ArrayList<>();

            for(String[] strings: listFinal){

                if(Integer.parseInt(strings[0]) < countRun){

                    listWrite.add(strings);

                }

            }

            List<Values> valuesList = new ArrayList<>();
            List<String[]> finalListWrite = new ArrayList<>();

            for(String[] strings: listWrite){

                SubValues subValues = new SubValues(strings[5], strings[6], strings[7], strings[8], strings[9], strings[10], strings[11]);
                Values values = new Values( (Integer.parseInt(strings[0])), strings[1], strings[2], strings[3], strings[4], subValues );
                valuesList.add(values);

            }

            valuesList.sort((o1, o2) -> Integer.valueOf(o1.getCounter()).compareTo(o2.getCounter()));


            for(Values values: valuesList){

                finalListWrite.add(new String[]{ String.valueOf(values.getCounter()), values.getString1(), values.getString2(), values.getString3(), values.getString4(),
                values.getSubValues().getSubString1(), values.getSubValues().getSubString2(), values.getSubValues().getSubString3(),values.getSubValues().getSubString4(),
                        values.getSubValues().getSubString5(), values.getSubValues().getSubString6(), values.getSubValues().getSubString7()});

            }

            csvWriter.writeNext(header);
            csvWriter.writeAll(finalListWrite);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    public static void divideForTesting(String pathFinal, int countRun, String path){

        try(FileReader fileReader = new FileReader(pathFinal);
            CSVReader csvReader = new CSVReader(fileReader);
            FileWriter fileWriter = new FileWriter(path + countRun + ".csv");
            CSVWriter csvWriter = new CSVWriter(fileWriter);){

            String[] header = csvReader.readNext();
            List<String[]> listFinal = csvReader.readAll();
            List<String[]> listWrite = new ArrayList<>();

            for(String[] strings: listFinal){

                if(strings[0].equals(String.valueOf(countRun))){

                    listWrite.add(strings);

                }

            }

            listWrite.sort(Comparator.comparing(o -> Integer.valueOf(o[0])));

            csvWriter.writeNext(header);
            csvWriter.writeAll(listWrite);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    public static void main(String[] args){


        importProp(PROJECT1);

        try(FileReader fileReader = new FileReader(getVersionInfo());
            CSVReader csvReader = new CSVReader(fileReader)
            ){

            csvReader.readNext();
            List<String[]> listVersion = csvReader.readAll();
            int halfSize = (listVersion.size()/2) + 1;

            for(int i = 1; i < halfSize; i++){

                divideForTesting(getFinalCSV(), i, getTestDir());
                divideForTraining(getFinalCSV(), i, getTrainDir());

            }

        }catch(IOException e){

            e.printStackTrace();

        }


    }

}
