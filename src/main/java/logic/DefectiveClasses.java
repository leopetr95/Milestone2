package logic;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import entity.Keys;
import utility.DateUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utility.ImportProperties.*;


public class DefectiveClasses {

    public static void determineOV(int flag, String blameJiraIntersectionPath, String blameJiraIntersectionOVPath){

        try(FileReader fileReader = new FileReader(blameJiraIntersectionPath);
            CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(versionInfo);
            CSVReader csvReader1 = new CSVReader(fileReader1);
            FileWriter fileWriter = new FileWriter(blameJiraIntersectionOVPath);
            CSVWriter csvWriter = new CSVWriter(fileWriter)){

            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> jira = csvReader.readAll();
            List<String[]> versions = csvReader1.readAll();
            List<String[]> jiraOV = new ArrayList<>();

            List<String[]> intervals = new ArrayList<>();

            DateUtil dateUtil = new DateUtil();

            for(int i = 0; i < (versions.size() / 2) -1; i++){

                intervals.add(new String[]{versions.get(i)[3].substring(0,10), versions.get(i+1)[3].substring(0,10)});

            }

            String result;
            String realResult = "";

            String fixVersionSupportString = "";

            //per i ticket con affectedversion

            String affectedVersionSupportString = "";

            for(String[] strings: jira){

                result = dateUtil.betweenInterval(intervals, dateUtil.stringToDate(strings[2]));
                result = dateUtil.convertDate(result);

                for(String[] string: versions){

                    //converte la data dell'OV nel suo index
                    if(result.equals(string[3].substring(0,10))){

                        realResult = string[0];

                    }

                    //converte la fixVersion nel suo index
                    if(strings[4].equals(string[2])){

                        fixVersionSupportString = string[0];

                    }

                    if(flag == 0){

                        //converte la affectedVersion nel suo index
                        if(strings[5].equals(string[2])){

                            affectedVersionSupportString = string[0];

                        }

                    }

                }

                if(flag == 0){

                    jiraOV.add(new String[]{strings[0], strings[1], strings[2], strings[3], fixVersionSupportString, affectedVersionSupportString, realResult});

                }else{

                    jiraOV.add(new String[]{strings[0], strings[1], strings[2], strings[3], fixVersionSupportString, realResult});

                }

            }

            if(flag == 0){

                csvWriter.writeNext(new String[]{"Class", "Ticket", "Date", "Tree", "FixVersion", "AffectedVersion", "OpenVersion"});

            }else{

                csvWriter.writeNext(new String[]{"Class", "Ticket", "Date", "Tree", "FixVersion", "OpenVersion"});

            }

            csvWriter.writeAll(jiraOV);

        }catch (IOException e){

            e.printStackTrace();

        }

    }

    public static double getProportion(){

        double P = 0;
        double IV;
        double OV;
        double FV;

        try(FileReader fileReader = new FileReader(blameJiraIntersectionOV); CSVReader csvReader = new CSVReader(fileReader)){

            //salto l'header
            csvReader.readNext();

            List<String[]> versions = csvReader.readAll();

            //rimuovo i record senza OV
            versions.removeIf(string -> string[6].equals(""));

            for(String[] strings: versions){

                FV = Double.parseDouble(strings[4]);
                IV = Double.parseDouble(strings[5]);
                OV = Double.parseDouble(strings[6]);

                P = P + ((FV - IV) / (FV - OV));

            }

            P = P /versions.size();

        }catch(IOException e){

            e.printStackTrace();

        }

        return P;

    }

    //La predictedIV va calcolata per quei ticket che non hanno affected version
    public static void calculatePredictedIV(Double P){

        double PredictedIV;
        double OV;
        double FV;

        try(FileReader fileReader = new FileReader(blameJiraIntersectionOVFVOnly); CSVReader csvReader = new CSVReader(fileReader);
        FileWriter fileWriter = new FileWriter(predictedIVCSV); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            //Salto l'header
            csvReader.readNext();

            List<String[]> versions = csvReader.readAll();
            List<String[]> predictedIVList = new ArrayList<>();

            versions.removeIf(string -> string[5].equals(""));

            for(String[] strings: versions){

                FV = Double.parseDouble(strings[4]);
                OV = Double.parseDouble(strings[5]);

                PredictedIV = FV - (FV - OV) * P;
                predictedIVList.add(new String[]{strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], String.valueOf(PredictedIV)});

            }

            //Scrivo l'header
            csvWriter.writeNext(new String[]{"Class", "Ticket", "Date", "Tree", "FixVersion", "OpenVersion", "PredictedIV"});

            csvWriter.writeAll(predictedIVList);

        }catch(IOException e){

            e.printStackTrace();

        }

    }


    //Scrivo in un file csv le classi difettive
    public static void getDefective(){

        try(FileReader fileReader = new FileReader(blameJiraIntersection); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(versionInfo); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(buggy); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            csvReader1.readNext();

            csvReader.readNext();
            List<String[]> jira = csvReader.readAll();
            List<String[]> dataList2 = new ArrayList<>();

            List<String[]> versions = csvReader1.readAll();
            List<String[]> versions2 = new ArrayList<>();

            //Prendo solo la prima metà delle versioni
            for(int i = 0; i < versions.size()/2; i++){

                versions2.add(versions.get(i));

            }

            for(String[] s: jira){

                for(String[] r: versions2){

                    if(r[2].equals(s[5])){

                        dataList2.add(new String[]{r[0], r[1], r[2], s[5] , s[0], s[1], "YES"});

                    }

                }

            }

            csvWriter.writeNext(new String[]{"index", "VersionID", "VersionName", "AffectedVersion", "Class", "Ticket", "Bugginess"});

            csvWriter.writeAll(dataList2);
            csvWriter.flush();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    //Aggiunge alle classi buggate quelle con predette con il metodo Proportion
    public static void sumBuggyPredicted(){

        try(
        FileReader fileReader = new FileReader(buggy); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(predictedIVCSV); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(sumBuggyPredicted); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            //Salto gli header
            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> buggyList = csvReader.readAll();
            List<String[]> predictedIVList = csvReader1.readAll();
            List<String[]> sumList = new ArrayList<>();

            for(String[] strings: buggyList){

                sumList.add(new String[]{strings[0], strings[4],strings[6]});

            }

            for(String[] strings: predictedIVList){

                double temp = Double.parseDouble(strings[6]);
                int temp1 = (int) temp;

                sumList.add(new String[]{String.valueOf(temp1), strings[0], "YES"});

            }

            csvWriter.writeAll(sumList);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    public static void createPrefinalCSV(){

        try(FileReader fileReader = new FileReader(sumBuggyPredicted); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(csvClassPath); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileReader fileReader2 = new FileReader(versionInfo); CSVReader csvReader2 = new CSVReader(fileReader2);
        FileWriter fileWriter = new FileWriter(preFinalCsv); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            //Salto gli header
            csvReader.readNext();
            csvReader1.readNext();


            List<String[]> sumBuggy = csvReader.readAll();
            List<String[]> classes = csvReader1.readAll();
            List<String[]> versions = csvReader2.readAll();

            List<String> versions2 = new ArrayList<>();

            //Prendo solo la prima metà delle versioni
            for(int i = 0; i < versions.size()/2; i++){

                versions2.add(versions.get(i)[0]);

            }

            Map<Keys, String> finalmap = new HashMap<>();

            List<String[]> finalList = new ArrayList<>();

            for(String[] strings: sumBuggy){

                for(String[] strings1: classes){

                    if(strings1[1].equals(strings[1])){

                        for(int i = 1; i < versions2.size(); i++){

                            Keys keys = new Keys(String.valueOf(i), strings1[1]);

                            if(!finalmap.containsKey(keys)){

                                finalmap.put(keys, "NO");
                                //finalList.add(new String[]{String.valueOf(i), strings1[1],"NO"});

                            }

                        }

                    }

                    if(versions2.contains(strings[0])){

                        String tempString = strings[0];
                        int tempInt = Integer.parseInt(tempString);

                        for(int i = 1; i < versions2.size(); i++){

                            Keys keys = new Keys(String.valueOf(i), strings1[1]);

                            if(!finalmap.containsKey(keys)) {

                                if (i != tempInt) {

                                    finalmap.put(keys, "NO");
                                    //finalList.add(new String[]{String.valueOf(i), strings[1], "NO"});

                                }

                            }

                        }

                    }

                }

            }

            for(Map.Entry<Keys, String> entry: finalmap.entrySet()){

                finalList.add(new String[]{entry.getKey().key1.toString(), entry.getKey().key2.toString(), entry.getValue()});

            }

            csvWriter.writeAll(sumBuggy);
            csvWriter.writeAll(finalList);

        }catch(IOException e){

            e.printStackTrace();

        }

    }


    public static void main(String[] args){

        importProp("BOOKKEEPER");

        getDefective();
        //ticket con Affected Version
        determineOV(0, blameJiraIntersection, blameJiraIntersectionOV);
        //ticket senza Affected Version
        determineOV(1, blameJiraIntersectionFVOnly, blameJiraIntersectionOVFVOnly);
        double P = getProportion();
        calculatePredictedIV(P);
        sumBuggyPredicted();
        createPrefinalCSV();

    }

}
