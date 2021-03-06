package milestones.uno;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import entity.*;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import utility.DateUtil;
import utility.ImportProperties;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static milestones.uno.BlameClasses.executeBlame;
import static milestones.uno.DefectiveClasses.*;
import static milestones.uno.GetCommits.writeCommit;
import static milestones.uno.GetReleaseInfo.getVersionInf;
import static milestones.uno.JiraBlameCommitIntersection.*;
import static milestones.uno.JiraVersion.retrieveTicketAvFVFromJira;
import static milestones.uno.JiraVersion.retrieveTicketOnlyFVFromJira;
import static milestones.uno.RetrieveClasses.cloneRepository;
import static milestones.uno.RetrieveClasses.createCsvClass;
import static utility.CreateDir.createDirectory;
import static utility.ImportProperties.*;

public class CalculateMetrics {

    private static final String INDEX = "Index";
    private static final String STRINGCLASS = "Class";
    private static final String PROJECT1 = "BOOKKEEPER";
    private static final String PROJECT2 = "ZOOKEEPER";

    /*
    * Compute MAX ChgSet and AVG ChgSet
    * */
    private static void retrieveMaxAndAverageChgSetSize() throws IOException {

        try(FileReader fileReader = new FileReader(getChgSetSize()); CSVReader csvReader = new CSVReader(fileReader);
        FileWriter fileWriter = new FileWriter(getMaxAvgChgSetSize()); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            csvReader.readNext();

            List<String[]> listChg = csvReader.readAll();
            List<String[]> outList = new ArrayList<>();

            Map<Keys, Values> map = new HashMap<>();

            int first;
            int second;
            int max;
            int count = 0;
            int temp = 0;

            for(String[] strings: listChg){

                Keys keys = new Keys(strings[0], strings[1]);

                if(!map.containsKey(keys)){

                    temp = Integer.parseInt(strings[2]);

                    Values values = new Values(strings[2], temp, count);

                    map.put(keys, values);

                }else{

                    count++;

                    temp = temp + Integer.parseInt(strings[2]);

                    first = Integer.parseInt(strings[2]);
                    second = Integer.parseInt(map.get(keys).getString1());
                    max = Integer.max(first, second);

                    Values values = new Values(String.valueOf(max), temp, count);

                    map.replace(keys, values);

                }

            }

            for(Map.Entry<Keys, Values> entry: map.entrySet()){

                if(entry.getValue().getCounter() == 0){

                    outList.add(new String[]{entry.getKey().getKey1().toString(), entry.getKey().getKey2().toString(), entry.getValue().getString1(), String.valueOf(entry.getValue().getTemp())});

                }else{

                    outList.add(new String[]{entry.getKey().getKey1().toString(), entry.getKey().getKey2().toString(), entry.getValue().getString1(), String.valueOf(entry.getValue().getTemp()/entry.getValue().getCounter())});

                }

            }

            csvWriter.writeNext(new String[]{STRINGCLASS, INDEX, "Max", "Avg"});
            csvWriter.writeAll(outList);

        }

    }

    /*
    * Compute LOC added, deleted and modified
    * */
    private static void retrieveLOCFromTrees() throws IOException {

        List<String[]> listBlameCommit;
        List<String[]> out = new ArrayList<>();

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Git git = new Git(new FileRepository(getcPath()));
        Repository repository = git.getRepository();

        try(FileReader fileReader = new FileReader(getBlameCommitIntersection());
            CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(ImportProperties.getVersionInfo());
            CSVReader csvReader1 = new CSVReader(fileReader1);
            FileWriter fileWriter = new FileWriter(getOutLoc());
            CSVWriter csvWriter = new CSVWriter(fileWriter)){

            csvReader1.readNext();

            csvReader.readNext();

            listBlameCommit = csvReader.readAll();

            int[] array;

            for(int i = 0; i < listBlameCommit.size(); i++){

                for (String[] tree : listBlameCommit) {

                    Date dateTreeI = format.parse(listBlameCommit.get(i)[2]);
                    Date dateTreeJ = format.parse(tree[2]);

                    if ((listBlameCommit.get(i)[0].equals(tree[0])) && dateTreeJ.after(dateTreeI)) {


                        ObjectReader reader = repository.newObjectReader();
                        CanonicalTreeParser oldTree = new CanonicalTreeParser();
                        ObjectId oldCommit = ObjectId.fromString(listBlameCommit.get(i)[3].substring(5, listBlameCommit.get(i)[3].indexOf("-") - 1));
                        oldTree.reset(reader, oldCommit);

                        ObjectId newCommit = ObjectId.fromString(tree[3].substring(5, tree[3].indexOf("-") - 1));
                        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                        newTreeIter.reset(reader, newCommit);

                        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                        diffFormatter.setRepository(git.getRepository());
                        diffFormatter.setContext(0);
                        List<DiffEntry> entries = diffFormatter.scan(newTreeIter, oldTree);

                        array = sumLines(entries, diffFormatter);

                        out.add(new String[]{tree[2], listBlameCommit.get(i)[0], Integer.toString(array[0]), Integer.toString(array[1]), Integer.toString(array[2])});

                    }

                }

            }

            csvWriter.writeAll(out);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /*
    *Compute in order linesAdded, linesDeleted and linesTouched
    */
    private static int[] sumLines(List<DiffEntry> list, DiffFormatter diffFormatter){

        int[] array = new int[3];

        try {

            for(DiffEntry diffEntry: list){

                for(Edit edit: diffFormatter.toFileHeader(diffEntry).toEditList()){

                    array[0] = array[0] + edit.getEndA() - edit.getBeginA();
                    array[1] = array[1] + edit.getEndB() - edit.getBeginB();
                    array[2] = array[0] + array[1];

                }

            }

        }catch (Exception e){

            e.printStackTrace();

        }

        return array;

    }

    /*
    * Compute LOC added and add index to trees
    * */
    private static void retrieveLOCFromTreesWithIndexAndSum(){

        try(FileReader fileReader = new FileReader(getOutLoc());CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(ImportProperties.getVersionInfo()); CSVReader csvReader1 = new CSVReader(fileReader1);
            FileWriter fileWriter = new FileWriter(getOutLocIndex()); CSVWriter csvWriter = new CSVWriter(fileWriter)
        ){

            List<String[]> outL = csvReader.readAll();
            csvReader1.readNext();
            List<String[]> versions = csvReader1.readAll();
            List<String[]> outLIndex = new ArrayList<>();
            int sum;

            DateUtil dateUtil = new DateUtil();
            List<String[]> intervals = new ArrayList<>();

            for(int i = 0; i < (versions.size() / 2) + 1 ; i++){

                intervals.add(new String[]{versions.get(i)[3].substring(0,10), versions.get(i+1)[3].substring(0,10)});

            }

            String result;
            String realResult = "";

            for(String[] strings: outL){

                result = dateUtil.betweenInterval(intervals, dateUtil.stringToDate(strings[0]));
                result = dateUtil.convertDate(result);

                for(String[] strings1: versions){

                    if(result.equals(strings1[3].substring(0,10))){

                        realResult = strings1[0];

                    }

                }

                sum = Integer.parseInt(strings[2]) + Integer.parseInt(strings[3]) + Integer.parseInt(strings[4]);

                outLIndex.add(new String[]{strings[0], strings[1], strings[2], strings[3], strings[4], realResult, String.valueOf(sum)});
                realResult = "";

            }

            csvWriter.writeNext(new String[]{"Date", STRINGCLASS, "LinesAdded", "LinesDeleted", "LinesTouched", INDEX, "Sum"});
            csvWriter.writeAll(outLIndex);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    /*
    * Compute MAX LOC added, AVG LOC added
    * */
    private static Map<Keys, Values> retrieveMaxAndAverageLoc(){

        Map<Keys, Values> finalmap = new HashMap<>();

        try(FileReader fileReader = new FileReader(getOutLocIndex()); CSVReader csvReader = new CSVReader(fileReader);


        ){

            int max;
            int first;
            int second;
            int temp = 0;
            int count = 0;

            csvReader.readNext();
            List<String[]> outLIndx = csvReader.readAll();

            for(String[] strings: outLIndx){

                for(String[] strings1: outLIndx){

                    if((strings[1].equals(strings1[1])) && (strings[5].equals(strings1[5]))){

                        Keys keys = new Keys(strings[5], strings[1]);

                        if(!finalmap.containsKey(keys)){

                            temp = Integer.parseInt(strings[2]);

                            Values values = new Values(strings[0], strings[2], strings[3], strings[4], temp, count);

                            finalmap.put(keys,values);

                        }else{

                            count++;

                            temp = temp + Integer.parseInt(strings[2]);

                            first = Integer.parseInt(strings[2]);
                            second = Integer.parseInt(finalmap.get(keys).getString2());
                            max = Integer.max(first, second);

                            Values values = new Values(strings[0], String.valueOf(max), strings[3], strings[4], temp, count);
                            finalmap.replace(keys, values);

                        }

                    }

                }

            }


        }catch(IOException e){
            e.printStackTrace();
        }
        return finalmap;

    }

    private static void writeMaxAvgLoc(Map<Keys, Values> finalmap){

        List<String[]> maxAvg = new ArrayList<>();

        try(FileWriter fileWriter = new FileWriter(getMaxAverage()); CSVWriter csvWriter = new CSVWriter(fileWriter);){

            for(Map.Entry<Keys, Values> entry: finalmap.entrySet()){

                if(entry.getValue().getCounter() == 0){

                    maxAvg.add(new String[]{entry.getValue().getString1(), entry.getKey().getKey2().toString(), entry.getValue().getString2(), String.valueOf(entry.getValue().getTemp()), entry.getKey().getKey1().toString()});

                }else{

                    maxAvg.add(new String[]{entry.getValue().getString1(), entry.getKey().getKey2().toString(), entry.getValue().getString2(), String.valueOf((entry.getValue().getTemp()/entry.getValue().getCounter())), entry.getKey().getKey1().toString()});

                }

            }

            csvWriter.writeNext(new String[]{"Date", STRINGCLASS, "MAXLOC", "AVGLOC", INDEX});
            csvWriter.writeAll(maxAvg);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    * Compute ChgSetSize
    * */
    private static void calculateChgSetSize(){

        try(FileReader fileReader = new FileReader(getBlameCommitIntersection()); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(ImportProperties.getVersionInfo()); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(getChgSetSize()); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            csvReader.readNext();
            csvReader1.readNext();

            List<String[]> versions = csvReader1.readAll();
            List<String[]> listChgSetSize = new ArrayList<>();

            DateUtil dateUtil = new DateUtil();
            List<String[]> intervals = new ArrayList<>();


            for(int i = 0; i < (versions.size() / 2) ; i++){

                intervals.add(new String[]{versions.get(i)[3].substring(0,10), versions.get(i+1)[3].substring(0,10)});

            }

            String result;
            String realResult = "";

            List<String[]> list = csvReader.readAll();
            Map<String, Values> map = new HashMap<>();

            for(String[] strings: list){

                result = dateUtil.betweenInterval(intervals, dateUtil.stringToDate(strings[2]));
                result = dateUtil.convertDate(result);

                for(String[] strings1: versions){

                    if(result.equals(strings1[3].substring(0,10))){

                        realResult = strings1[0];

                    }

                }

                if(!map.containsKey(strings[1])){

                    int counter = 0;

                    Values values = new Values(realResult, counter, strings[0]);

                    map.put(strings[1], values);

                }else{

                    Values values = new Values(realResult, (map.get(strings[1]).getTemp() +1), strings[0]);

                    map.replace(strings[1], values);

                }

            }

            for (Map.Entry<String, Values> entry : map.entrySet()) {

                listChgSetSize.add(new String[]{entry.getValue().getString2(), entry.getValue().getString1(), String.valueOf(entry.getValue().getTemp())});

            }

            csvWriter.writeNext(new String[]{STRINGCLASS, INDEX, "ChgSetSize"});
            csvWriter.writeAll(listChgSetSize);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    /*
    * Compute Size and Age
    * */
    private static void calculateSizeAgeAuthors() throws IOException {

        Git git = new Git(new FileRepository(getcPath()));

        Date commitTime;
        Date verTime;
        int size;
        int weeks;
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        try(FileReader fileReader = new FileReader(getCsvClassPath());CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(ImportProperties.getVersionInfo()); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(getSizeMetric()); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            List<String[]> classes = csvReader.readAll();
            //Salto l'header
            csvReader1.readNext();
            List<String[]> versions = csvReader1.readAll();
            List<String[]> versions2 = versions.subList(0, versions.size()/2);
            List<String[]> listAge = new ArrayList<>();

            for(String[] strings: versions2){

                verTime = format.parse(strings[3]);
                DateTime versionTime = new DateTime(verTime);

                for(String[] strings1: classes){

                    List<String> authors = new ArrayList<>();
                    int numberOfAuthors;

                    String key = strings1[0].replace(getStringToReplace(), "");

                    BlameCommand blameCommand = git.blame().setStartCommit(git.getRepository().resolve("HEAD")).setFilePath(key);

                    BlameResult blameResult = blameCommand.call();

                    size = blameResult.getResultContents().size();
                    commitTime = blameResult.getSourceCommitter(0).getWhen();

                    DateTime cmtTime = new DateTime(commitTime);
                    weeks = Weeks.weeksBetween(versionTime, cmtTime).getWeeks();

                    if(weeks < 0){

                        weeks = 0;

                    }

                    for(int i = 0; i < size; i++){

                        String string = blameResult.getSourceAuthor(i).getName();

                        if(!authors.contains(string)){

                            authors.add(string);

                        }

                    }

                    numberOfAuthors = authors.size();

                    listAge.add(new String[]{strings[0], strings1[1], Integer.toString(size), Integer.toString(weeks), String.valueOf(numberOfAuthors)});

                }

            }

            //Aggiungo l'header
            csvWriter.writeNext(new String[]{INDEX, STRINGCLASS, "Size", "Age", "NumberOfAuthors"});
            csvWriter.writeAll(listAge);

        } catch (ParseException | GitAPIException e) {
            e.printStackTrace();
        }

    }

    /*
    * Compute final csv with all metrics
    * */
    private static Map<Keys, Values> createFinalCSV(){

        Map<Keys, Values> map = new HashMap<>();
        try(FileReader fileReader = new FileReader(getChgSetSize());
            CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(getMaxAverage());
            CSVReader csvReader1 = new CSVReader(fileReader1);
            FileReader fileReader2 = new FileReader(getMaxAvgChgSetSize());
            CSVReader csvReader2 = new CSVReader(fileReader2);
            FileReader fileReader3 = new FileReader(getOutLocIndex());
            CSVReader csvReader3 = new CSVReader(fileReader3);
            FileReader fileReader5 = new FileReader(getSizeMetric());
            CSVReader csvReader5 = new CSVReader(fileReader5);
            ){

            //Salto gli header
            csvReader.readNext();
            csvReader1.readNext();
            csvReader2.readNext();
            csvReader3.readNext();
            csvReader5.readNext();

            List<String[]> listChgSetSize = csvReader.readAll();
            List<String[]> listMaxAverage = csvReader1.readAll();
            List<String[]> listMaxAvgChgSetSize = csvReader2.readAll();
            List<String[]> listOutLocIndex = csvReader3.readAll();
            List<String[]> listSizeMetric = csvReader5.readAll();

            //Index, Class, Size, Age, NumberOfAuthors, Sum, MaxChg, AvgChg, MaxLoc, AvgLoc, ChgSetSize

            for(String[] stringSizeMetric: listSizeMetric){

                //Index, Class
                Keys keys = new Keys(stringSizeMetric[0], stringSizeMetric[1]);

                //NumberOfAuthors, Sum, MaxChg, AvgChg, MaxLoc, AvgLoc, ChgSetsize
                SubValues subValues = new SubValues(stringSizeMetric[4], "0", "0", "0", "0", "0", "0");

                //Size, Age, Subvalues
                Values values = new Values(stringSizeMetric[2], stringSizeMetric[3], subValues);

                map.put(keys, values);

            }


            for(String[] stringOutLocIndex: listOutLocIndex){

                Keys keys = new Keys(stringOutLocIndex[5], stringOutLocIndex[1]);

                if(map.containsKey(keys)){

                    SubValues subValues = new SubValues(map.get(keys).getSubValues().getSubString1(), stringOutLocIndex[6], map.get(keys).getSubValues().getSubString3(), map.get(keys).getSubValues().getSubString4(), map.get(keys).getSubValues().getSubString5(), map.get(keys).getSubValues().getSubString6(), map.get(keys).getSubValues().getSubString7());

                    //Size, Age, SubValues
                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), subValues);

                    map.replace(keys, values);

                }

            }



            for(String[] stringMaxAvgChg: listMaxAvgChgSetSize){

                Keys keys = new Keys(stringMaxAvgChg[1], stringMaxAvgChg[0]);

                if(map.containsKey(keys)){

                    SubValues subValues = new SubValues(map.get(keys).getSubValues().getSubString1(), map.get(keys).getSubValues().getSubString2(), stringMaxAvgChg[2], stringMaxAvgChg[3], map.get(keys).getSubValues().getSubString5(), map.get(keys).getSubValues().getSubString6(), map.get(keys).getSubValues().getSubString7());

                    //Size, Age, SubValues
                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(),subValues);

                    map.replace(keys, values);

                }

            }



            for(String[] stringMaxAverage: listMaxAverage){

                Keys keys = new Keys(stringMaxAverage[4], stringMaxAverage[1]);

                if(map.containsKey(keys)){

                    SubValues subValues = new SubValues( map.get(keys).getSubValues().getSubString1(), map.get(keys).getSubValues().getSubString2(), map.get(keys).getSubValues().getSubString3(), map.get(keys).getSubValues().getSubString4(), stringMaxAverage[2], stringMaxAverage[3], map.get(keys).getSubValues().getSubString7());
                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(),subValues);
                    map.replace(keys, values);

                }

            }

            for(String[] stringChgSetSize: listChgSetSize){

                Keys keys = new Keys(stringChgSetSize[1], stringChgSetSize[0]);

                if(map.containsKey(keys)){

                    SubValues subValues = new SubValues(map.get(keys).getSubValues().getSubString1(), map.get(keys).getSubValues().getSubString2(),map.get(keys).getSubValues().getSubString3(), map.get(keys).getSubValues().getSubString4(), map.get(keys).getSubValues().getSubString5(), map.get(keys).getSubValues().getSubString6(), stringChgSetSize[2]);
                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), subValues);
                    map.replace(keys, values);

                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;

    }

    private static Map<Keys, Values> writePreFinal(Map<Keys, Values> map){

        try(FileReader fileReader4 = new FileReader(getPreFinalCsv());
            CSVReader csvReader4 = new CSVReader(fileReader4)){

            List<String[]> listPreFinalCsv = csvReader4.readAll();

            for(String[] stringPreFinal: listPreFinalCsv){

                Keys keys = new Keys(stringPreFinal[0], stringPreFinal[1]);

                if(map.containsKey(keys)){

                    SubValues subValues = new SubValues(map.get(keys).getSubValues().getSubString2(), map.get(keys).getSubValues().getSubString3(), map.get(keys).getSubValues().getSubString4(), map.get(keys).getSubValues().getSubString5(), map.get(keys).getSubValues().getSubString6(), map.get(keys).getSubValues().getSubString7(), stringPreFinal[2]);
                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), map.get(keys).getSubValues().getSubString1(), subValues);

                    map.replace(keys, values);

                }

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        return map;

    }

    public static void writeFinal(Map<Keys, Values> map){

        try(FileWriter fileWriter = new FileWriter(getFinalCSV());
            CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            List<String[]> finalList = new ArrayList<>();


            for(Map.Entry<Keys, Values> entry: map.entrySet()){

                if(entry.getValue().getSubValues().getSubString7()!= null){

                    finalList.add(new String[]{
                            entry.getKey().getKey1().toString(),
                            entry.getKey().getKey2().toString(),
                            entry.getValue().getString1(),
                            entry.getValue().getString2(),
                            entry.getValue().getString3(),
                            entry.getValue().getSubValues().getSubString1(),
                            entry.getValue().getSubValues().getSubString2(),
                            entry.getValue().getSubValues().getSubString3(),
                            entry.getValue().getSubValues().getSubString4(),
                            entry.getValue().getSubValues().getSubString5(),
                            entry.getValue().getSubValues().getSubString6(),
                            entry.getValue().getSubValues().getSubString7()});

                }

            }
            //Index, Class, Size, Age, NumberOfAuthors, Sum, MaxChg, AvgChg, MaxLoc, AvgLoc, ChgSetSize
            csvWriter.writeNext(new String[]{INDEX, STRINGCLASS, "Size", "Age", "NumberOfAuthors", "Sum", "MaxChg", "AvgChg", "MaxLoc", "AvgLoc", "ChgSetSize", "Bugginess"});

            finalList.removeIf(strings -> strings[11].equals("0"));

            csvWriter.writeAll(finalList);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, GitAPIException {

        //Project BOOKKEEPER
        importProp(PROJECT1);

        createDirectory(PROJECT1);
        cloneRepository();
        createCsvClass();
        executeBlame();
        getVersionInf(PROJECT1);
        writeCommit(PROJECT1);
        retrieveTicketAvFVFromJira(PROJECT1);
        retrieveTicketOnlyFVFromJira(PROJECT1);
        blameCommit();
        blameJira();
        blameJiraFVOnly();
        getDefective();

        List<String[]> intervals = new ArrayList<>();

        try(FileReader fileReader = new FileReader(ImportProperties.getVersionInfo()); CSVReader csvReader = new CSVReader(fileReader);){

            csvReader.readNext();
            List<String[]> list = csvReader.readAll();

            for(int i = 0; i < (list.size() / 2) ; i++){

                intervals.add(new String[]{list.get(i)[3].substring(0,10), list.get(i+1)[3].substring(0,10)});

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        //ticket with Affected Version
        determineOV(0, getBlameJiraIntersection(), getBlameJiraIntersectionOV(), intervals);
        //ticket without Affected Version
        determineOV(1, getBlameJiraIntersectionFVOnly(), getBlameJiraIntersectionOVFVOnly(), intervals);
        double p = getProportion();
        calculatePredictedIV(p);
        sumBuggyPredicted();
        createPreFinalCSV1(createPreFinalCSV2());

        retrieveLOCFromTrees();

        retrieveLOCFromTreesWithIndexAndSum();
        writeMaxAvgLoc(retrieveMaxAndAverageLoc());
        calculateSizeAgeAuthors();
        calculateChgSetSize();
        retrieveMaxAndAverageChgSetSize();
        writeFinal(writePreFinal(createFinalCSV()));


        //Project ZOOKEEPER

        importProp(PROJECT2);

        createDirectory(PROJECT2);
        cloneRepository();
        createCsvClass();
        executeBlame();
        getVersionInf(PROJECT2);
        writeCommit(PROJECT2);
        retrieveTicketAvFVFromJira(PROJECT2);
        retrieveTicketOnlyFVFromJira(PROJECT2);
        blameCommit();
        blameJira();
        blameJiraFVOnly();
        getDefective();

        List<String[]> intervals1 = new ArrayList<>();

        try(FileReader fileReader = new FileReader(ImportProperties.getVersionInfo()); CSVReader csvReader = new CSVReader(fileReader);){

            csvReader.readNext();
            List<String[]> list = csvReader.readAll();

            for(int i = 0; i < (list.size() / 2) ; i++){

                intervals1.add(new String[]{list.get(i)[3].substring(0,10), list.get(i+1)[3].substring(0,10)});

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        //ticket with Affected Version
        determineOV(0, getBlameJiraIntersection(), getBlameJiraIntersectionOV(), intervals1);
        //ticket without Affected Version
        determineOV(1, getBlameJiraIntersectionFVOnly(), getBlameJiraIntersectionOVFVOnly(), intervals1);
        double p1 = getProportion();
        calculatePredictedIV(p1);
        sumBuggyPredicted();
        createPreFinalCSV1(createPreFinalCSV2());
        retrieveLOCFromTrees();
        retrieveLOCFromTreesWithIndexAndSum();
        writeMaxAvgLoc(retrieveMaxAndAverageLoc());
        calculateSizeAgeAuthors();
        calculateChgSetSize();
        retrieveMaxAndAverageChgSetSize();
        writeFinal(writePreFinal(createFinalCSV()));

    }

}
