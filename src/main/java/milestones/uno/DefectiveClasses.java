package milestones.uno;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import entity.*;
import utility.DateUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utility.ImportProperties.*;
import static utility.ImportProperties.getBlameJiraIntersectionOVFVOnly;


public class DefectiveClasses {

    private static final String STRINGFIXVERSION = "FixVersion";
    private static final String STRINGCLASS = "Class";
    private static final String STRINGOPENVERSION = "OpenVersion";
    private static final String STRINGTICKET = "Ticket";

    private DefectiveClasses(){}

    public static void determineOV(int flag, String blameJiraIntersectionPath, String blameJiraIntersectionOVPath, List<String[]> intervals){

        try(FileReader fileReader = new FileReader(blameJiraIntersectionPath);
            CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(getVersionInfo());
            CSVReader csvReader1 = new CSVReader(fileReader1);
            FileWriter fileWriter = new FileWriter(blameJiraIntersectionOVPath);
            CSVWriter csvWriter = new CSVWriter(fileWriter)){

            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> jira = csvReader.readAll();
            List<String[]> versions = csvReader1.readAll();
            List<String[]> jiraOV = new ArrayList<>();

            DateUtil dateUtil = new DateUtil();

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

                    if(flag == 0 && strings[5].equals(string[2])){

                        //converte la affectedVersion nel suo index
                        affectedVersionSupportString = string[0];

                    }

                }

                jiraOVAdd(flag, jiraOV, strings, fixVersionSupportString, affectedVersionSupportString, realResult);

            }

            csvWriter.writeAll(jiraOV);

        }catch (IOException e){

            e.printStackTrace();

        }

    }

    public static List<String[]> jiraOVAdd(int flag, List<String[]> list, String[] strings, String fixVersionSupportString, String affectedVersionSupportString, String realResult){

        if(flag == 0){

            list.add(new String[]{strings[0], strings[1], strings[2], strings[3], fixVersionSupportString, affectedVersionSupportString, realResult});

        }else{

            list.add(new String[]{strings[0], strings[1], strings[2], strings[3], fixVersionSupportString, realResult});

        }

        return list;

    }

    public static double getProportion(){

        double p = 0;
        double iv;
        double ov;
        double fv;

        try(FileReader fileReader = new FileReader(getBlameJiraIntersectionOV()); CSVReader csvReader = new CSVReader(fileReader)){

            //salto l'header
            csvReader.readNext();
            int counter = 0;

            List<String[]> versions = csvReader.readAll();

            //rimuovo i record senza OV
            versions.removeIf(string -> string[6].equals(""));

            for(String[] strings: versions){

                fv = Double.parseDouble(strings[4]);
                iv = Double.parseDouble(strings[5]);
                ov = Double.parseDouble(strings[6]);

                if(fv > ov){

                    counter++;

                    p = p + ((fv - iv) / (fv - ov));

                }

            }


            if(counter != 0){

                p = p / counter;

            }

        }catch(IOException e){

            e.printStackTrace();

        }

        return p;

    }

    /*
    * Predict the IV for ticket without Affected Version
    * */
    public static void calculatePredictedIV(Double p){

        double predictedIv;
        double ov;
        double fv;
        try(FileReader fileReader = new FileReader(getBlameJiraIntersectionOVFVOnly()); CSVReader csvReader = new CSVReader(fileReader);
        FileWriter fileWriter = new FileWriter(getPredictedIVCSV()); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            //Salto l'header
            csvReader.readNext();

            List<String[]> versions = csvReader.readAll();
            List<String[]> predictedIVList = new ArrayList<>();

            versions.removeIf(string -> string[5].equals(""));

            for(String[] strings: versions){

                fv = Double.parseDouble(strings[4]);
                ov = Double.parseDouble(strings[5]);

                predictedIv = fv - (fv - ov) * p;
                predictedIVList.add(new String[]{strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], String.valueOf(predictedIv)});

            }

            //Scrivo l'header
            csvWriter.writeNext(new String[]{STRINGCLASS, STRINGTICKET, "Date", "Tree", STRINGFIXVERSION, STRINGOPENVERSION, "PredictedIV"});

            csvWriter.writeAll(predictedIVList);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    /*
    * Write in a csv file defective classes
    * */
    public static void getDefective(){

        try(FileReader fileReader = new FileReader(getBlameJiraIntersection()); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(getVersionInfo()); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(getBuggy()); CSVWriter csvWriter = new CSVWriter(fileWriter)){

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

            csvWriter.writeNext(new String[]{"index", "VersionID", "VersionName", "AffectedVersion", STRINGCLASS, STRINGTICKET, "Bugginess"});

            csvWriter.writeAll(dataList2);
            csvWriter.flush();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    /*
    * Add predicted classes to defective ones
    * */
    public static void sumBuggyPredicted(){

        try(
        FileReader fileReader = new FileReader(getBuggy()); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(getPredictedIVCSV()); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(getSumBuggyPredicted()); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            //Salto gli header
            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> buggyList = csvReader.readAll();
            List<String[]> predictedIVList = csvReader1.readAll();
            List<String[]> sumList = new ArrayList<>();

            Map<Keys, String> map = new HashMap<>();

            for(String[] strings: buggyList){

                Keys keys = new Keys(strings[0], strings[4]);

                map.computeIfAbsent(keys, k-> map.put(keys, strings[6]));

            }

            for(String[] strings: predictedIVList){

                double temp = Double.parseDouble(strings[6]);
                int temp1 = (int) temp;

                Keys keys = new Keys(String.valueOf(temp1), strings[0]);

                map.computeIfAbsent(keys, k-> map.put(keys, "YES"));

            }

            for(Map.Entry<Keys, String> entry: map.entrySet()){

                sumList.add(new String[]{entry.getKey().getKey1().toString(), entry.getKey().getKey2().toString(), entry.getValue()});

            }

            csvWriter.writeAll(sumList);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    private static Map<Keys, String> contKey(Map<Keys, String> map, Keys keys){

        map.computeIfAbsent(keys, k-> map.put(keys, "NO"));

        return map;

    }

    private static Map<Keys, String> contKey2(Map<Keys, String> map, Keys keys, int counter, int temp){

        if(!map.containsKey(keys) && counter != temp){

            map.put(keys, "NO");

        }

        return map;

    }

    public static void createPreFinalCSV1(Map<Keys, String> finalmap){

        try(FileReader fileReader = new FileReader(getSumBuggyPredicted()); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(getCsvClassPath()); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileReader fileReader2 = new FileReader(getVersionInfo()); CSVReader csvReader2 = new CSVReader(fileReader2);
        ){

            //Skip headers
            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> sumBuggy = csvReader.readAll();
            List<String[]> classes = csvReader1.readAll();
            List<String[]> versions = csvReader2.readAll();

            List<String> versions2 = new ArrayList<>();

            //Take only the first half of the release
            for(int i = 1; i < versions.size()/2 + 1; i++){

                versions2.add(versions.get(i)[0]);

            }

            for(String[] strings: sumBuggy){

                for(String[] strings1: classes){

                    if(strings1[1].equals(strings[1])){

                        for(int i = 1; i < versions2.size() + 1; i++){

                            Keys keys = new Keys(String.valueOf(i), strings1[1]);

                            contKey(finalmap,keys);

                        }

                    }

                }

            }

            writePreFinal(finalmap, sumBuggy);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    public static Map<Keys, String> createPreFinalCSV2() {

        Map<Keys, String> map = new HashMap<>();

        try (FileReader fR = new FileReader(getSumBuggyPredicted()); CSVReader csvR = new CSVReader(fR);
             FileReader fR1 = new FileReader(getCsvClassPath()); CSVReader csvR1 = new CSVReader(fR1);
             FileReader fR2 = new FileReader(getVersionInfo()); CSVReader csvR2 = new CSVReader(fR2);
        ) {

            List<String[]> versions = csvR2.readAll();

            List<String> versions2 = new ArrayList<>();

            //Prendo solo la prima metà delle versioni
            for (int i = 1; i < (versions.size() / 2) +1; i++) {

                versions2.add(versions.get(i)[0]);

            }

            //Salto gli header
            csvR.readNext();
            csvR1.readNext();

            List<String[]> sumBuggy = csvR.readAll();
            List<String[]> classes = csvR1.readAll();

            for(String[] strings:sumBuggy){

                for(String[] strings1: classes){

                    if(versions2.contains(strings[0])){

                        String tempString = strings[0];
                        int tempInt = Integer.parseInt(tempString);

                        for(int i = 1; i < versions2.size() +1; i++){

                            Keys keys = new Keys(String.valueOf(i), strings1[1]);

                            contKey2(map, keys, i, tempInt);

                        }

                    }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;

    }

    /*
    * Write in a csv file defective and non defective classes
    * */
    private static void writePreFinal(Map<Keys, String> map, List<String[]> sumBuggy){

        List<String[]> finalList = new ArrayList<>();

        Map<Keys, String> map1 = new HashMap<>();

        for(String[] strings: sumBuggy){

            Keys keys = new Keys(strings[0], strings[1]);
            map1.put(keys, strings[2]);

        }

        try(FileWriter fileWriter = new FileWriter(getPreFinalCsv()); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            for(Map.Entry<Keys, String> entry: map.entrySet()){

                Keys keys = new Keys(entry.getKey().getKey1().toString(), entry.getKey().getKey2().toString());

                if(!map1.containsKey(keys)){

                    finalList.add(new String[]{entry.getKey().getKey1().toString(), entry.getKey().getKey2().toString(), entry.getValue()});

                }

            }

            csvWriter.writeAll(sumBuggy);
            csvWriter.writeAll(finalList);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
