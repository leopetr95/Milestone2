package milestone1;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import entity.*;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static utility.ImportProperties.*;


public class JiraBlameCommitIntersection {

    private static final String STRINGTICKET = "Ticket";
    private static final String STRINGCLASS = "Class";

    /*
    * Intersect the file containing blame
    * with the one containing commits through the tree,
    * the only element in common
    * */
    public static void blameCommit(){

        try(FileReader commitReader = new FileReader(getCsvCommit());
            CSVReader csvReader = new CSVReader(commitReader);
            FileReader blameReader = new FileReader(getCsvBlame());
            CSVReader csvReader1 = new CSVReader(blameReader);
            FileWriter fileWriter = new FileWriter(getBlameCommitIntersection());
            CSVWriter csvWriter = new CSVWriter(fileWriter)){

            List<String[]> listCommit =  csvReader.readAll();

            //Salto gli header
            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> listBlame = csvReader1.readAll();
            List<String[]> listIntersection = new ArrayList<>();

            Map<Keys, Values> map = new HashMap<>();

            for(String[] r: listCommit){

                for(String[] s: listBlame){

                    //controllo l'uguaglianza tra i tree
                    if(r[1].equals(s[1])){

                        Keys key = new Keys(s[2], r[2]);
                        if(!map.containsKey(key)){

                            Values values = new Values(r[0], s[1]);
                            map.put(key, values);

                        }

                    }

                }

            }

            for(Map.Entry<Keys, Values> entry: map.entrySet()){

                listIntersection.add(new String[]{entry.getKey().getKey1().toString(), entry.getKey().getKey2().toString(), entry.getValue().getString1(), entry.getValue().getString2()});

            }

            csvWriter.writeNext(new String[]{STRINGCLASS, STRINGTICKET, "Date", "Tree"});

            csvWriter.writeAll(listIntersection);

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    /*
    *Intersect the file containing blame and commit with the
    * one containing ticket with only the Fixed Version
    * */
    public static void blameJiraFVOnly(){

        try(FileReader fileReader = new FileReader(getBlameCommitIntersection());
            CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(getVersionsFVOnly());
            CSVReader csvReader1 = new CSVReader(fileReader1);
            FileWriter fileWriter = new FileWriter(getBlameJiraIntersectionFVOnly());
            CSVWriter csvWriter = new CSVWriter(fileWriter)){

            //Salto l'header
            csvReader.readNext();

            List<String[]> listBlameCommit =  csvReader.readAll();
            List<String[]> listJiraTicket =  csvReader1.readAll();
            List<String[]> listJiraBlame = new ArrayList<>();

            for(String[] str: listJiraTicket){

                for(String[] str1: listBlameCommit){

                    if(str[1].equals(str1[1])){

                        listJiraBlame.add(new String[]{str1[0], str1[1], str1[2], str1[3], str[2]});

                    }

                }

            }

            csvWriter.writeNext(new String[]{STRINGCLASS, STRINGTICKET, "Date", "Tree", "FixVersion"});

            csvWriter.writeAll(listJiraBlame);

        }catch (IOException e){

            e.printStackTrace();

        }

    }

    /*
     *Intersect the file containing blame and commit with the
     * one containing ticket with AV and FV
     * */
    public static void blameJira(){

        try(FileReader fileReader = new FileReader(getBlameCommitIntersection());
        CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(getVersionsAV());
        CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(getBlameJiraIntersection());
        CSVWriter csvWriter = new CSVWriter(fileWriter)){

            csvReader.readNext();

            List<String[]> listBlameCommit =  csvReader.readAll();
            List<String[]> listJiraTicket =  csvReader1.readAll();
            List<String[]> listJiraBlame = new ArrayList<>();

            for(String[] str: listJiraTicket){

                for(String[] str1: listBlameCommit){

                    if(str[1].equals(str1[1])){

                        listJiraBlame.add(new String[]{str1[0], str1[1], str1[2], str1[3], str[2], str[3]});

                    }

                }

            }

            csvWriter.writeNext(new String[]{STRINGCLASS, STRINGTICKET, "Date", "Tree", "FixVersion", "AffectedVersion"});

            csvWriter.writeAll(listJiraBlame);

        }catch (IOException e){

            e.printStackTrace();

        }

    }

}
