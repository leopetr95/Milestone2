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
import static utility.ImportProperties.getBlameJiraIntersectionOVFVOnly;


public class DefectiveClasses {

    private static final String STRINGFIXVERSION = "FixVersion";
    private static final String STRINGCLASS = "Class";
    private static final String STRINGOPENVERSION = "OpenVersion";
    private static final String STRINGTICKET = "Ticket";


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

            List<String[]> versions = csvReader.readAll();

            //rimuovo i record senza OV
            versions.removeIf(string -> string[6].equals(""));

            for(String[] strings: versions){

                fv = Double.parseDouble(strings[4]);
                iv = Double.parseDouble(strings[5]);
                ov = Double.parseDouble(strings[6]);

                p = p + ((fv - iv) / (fv - ov));

            }

            p = p /versions.size();

        }catch(IOException e){

            e.printStackTrace();

        }

        return p;

    }

    //La predictedIV va calcolata per quei ticket che non hanno affected version
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


    //Scrivo in un file csv le classi difettive
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

    //Aggiunge alle classi buggate quelle con predette con il metodo Proportion
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

    public static Map<Keys, String> contKey(Map<Keys, String> map, Keys keys){

        if(!map.containsKey(keys)){

            map.put(keys, "NO");

        }

        return map;

    }

    public static Map<Keys, String> contKey2(Map<Keys, String> map, Keys keys, int counter, int temp){

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


            for(String[] strings: sumBuggy){

                for(String[] strings1: classes){

                    if(strings1[1].equals(strings[1])){

                        for(int i = 1; i < versions2.size(); i++){

                            Keys keys = new Keys(String.valueOf(i), strings1[1]);

                            contKey(finalmap,keys);

                            /*if(!finalmap.containsKey(keys)){

                                finalmap.put(keys, "NO");

                            }*/

                        }

                    }


                    /*if(versions2.contains(strings[0])){

                        String tempString = strings[0];
                        int tempInt = Integer.parseInt(tempString);

                        for(int i = 1; i < versions2.size(); i++){

                            Keys keys = new Keys(String.valueOf(i), strings1[1]);

                            if(!finalmap.containsKey(keys) && i != tempInt) {

                                finalmap.put(keys, "NO");

                            }

                        }

                    }*/

                }

            }

            writePreFinal(finalmap, sumBuggy);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    public static Map<Keys, String> createPreFinalCSV2() {

        Map<Keys, String> map = new HashMap<>();

        try (FileReader fileReader = new FileReader(getSumBuggyPredicted()); CSVReader csvReader = new CSVReader(fileReader);
             FileReader fileReader1 = new FileReader(getCsvClassPath()); CSVReader csvReader1 = new CSVReader(fileReader1);
             FileReader fileReader2 = new FileReader(getVersionInfo()); CSVReader csvReader2 = new CSVReader(fileReader2);
        ) {

            //Salto gli header
            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> sumBuggy = csvReader.readAll();
            List<String[]> classes = csvReader1.readAll();
            List<String[]> versions = csvReader2.readAll();

            List<String> versions2 = new ArrayList<>();

            //Prendo solo la prima metà delle versioni
            for (int i = 0; i < versions.size() / 2; i++) {

                versions2.add(versions.get(i)[0]);

            }

            for(String[] strings:sumBuggy){

                for(String[] strings1: classes){

                    if(versions2.contains(strings[0])){

                        String tempString = strings[0];
                        int tempInt = Integer.parseInt(tempString);

                        for(int i = 1; i < versions2.size(); i++){

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

        public static void writePreFinal(Map<Keys, String> map, List<String[]> sumBuggy){

        List<String[]> finalList = new ArrayList<>();

        try(FileWriter fileWriter = new FileWriter(getPreFinalCsv()); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            for(Map.Entry<Keys, String> entry: map.entrySet()){

                finalList.add(new String[]{entry.getKey().getKey1().toString(), entry.getKey().getKey2().toString(), entry.getValue()});

            }

            csvWriter.writeAll(sumBuggy);
            csvWriter.writeAll(finalList);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args){

        importProp("BOOKKEEPER");

        getDefective();
        //ticket con Affected Version
        List<String[]> intervals = new ArrayList<>();

        try(FileReader fileReader = new FileReader(getVersionInfo()); CSVReader csvReader = new CSVReader(fileReader);){

            csvReader.readNext();
            List<String[]> list = csvReader.readAll();

            for(int i = 0; i < (list.size() / 2) ; i++){

                intervals.add(new String[]{list.get(i)[3].substring(0,10), list.get(i+1)[3].substring(0,10)});

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        determineOV(0, getBlameJiraIntersection(), getBlameJiraIntersectionOV(), intervals);
        //ticket senza Affected Version
        determineOV(1, getBlameJiraIntersectionFVOnly(), getBlameJiraIntersectionOVFVOnly(), intervals);
        double p = getProportion();
        calculatePredictedIV(p);
        sumBuggyPredicted();
        createPreFinalCSV1(createPreFinalCSV2());

    }

}
