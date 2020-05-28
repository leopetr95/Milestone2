package logic;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import entity.Keys;
import entity.Values;
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

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static logic.BlameClasses.executeBlame;
import static logic.DefectiveClasses.*;
import static logic.GetCommits.writeCommit;
import static logic.GetReleaseInfo.getVersionInfo;
import static logic.JiraBlameCommitIntersection.*;
import static logic.JiraVersion.retrieveTicketAvFVFromJira;
import static logic.JiraVersion.retrieveTicketOnlyFVFromJira;
import static logic.RetrieveClasses.cloneRepository;
import static logic.RetrieveClasses.createCsvClass;
import static utility.CreateDir.createDirectory;
import static utility.ImportProperties.*;

public class CalculateMetrics {


    /*
    * Compute MAX ChgSet and AVG ChgSet
    * */
    private static void retrieveMaxAndAverageChgSetSize() throws IOException {

        try(FileReader fileReader = new FileReader(chgSetSize); CSVReader csvReader = new CSVReader(fileReader);
        FileWriter fileWriter = new FileWriter(maxAvgChgSetSize); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

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

                    outList.add(new String[]{entry.getKey().key1.toString(), entry.getKey().key2.toString(), entry.getValue().getString1(), String.valueOf(entry.getValue().getTemp())});

                }else{

                    outList.add(new String[]{entry.getKey().key1.toString(), entry.getKey().key2.toString(), entry.getValue().getString1(), String.valueOf(entry.getValue().getTemp()/entry.getValue().getCounter())});

                }

            }

            csvWriter.writeNext(new String[]{"Class", "Index", "Max", "Avg"});
            csvWriter.writeAll(outList);

        }

    }


    /*
    * Compute LOC added, deleted modified
    * */
    private static void retrieveLOCFromTrees() throws IOException {

        List<String[]> trees;
        List<String[]> out = new ArrayList<>();

        int linesAdded = 0;
        int linesDeleted = 0;
        int linesTouched = 0;

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        Git git = new Git(new FileRepository(cPath));
        Repository repository = git.getRepository();

        try(FileReader fileReader = new FileReader(blameCommitIntersection);
        CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(versionInfo);
        CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(outLoc);
        CSVWriter csvWriter = new CSVWriter(fileWriter)){

            csvReader1.readNext();

            csvReader.readNext();

            trees = csvReader.readAll();

            for(int i = 0; i < trees.size(); i++){

                for(int j = 0; j < trees.size(); j++){

                    Date dateTreeI = format.parse(trees.get(i)[2]);
                    Date dateTreeJ = format.parse(trees.get(j)[2]);

                    if((trees.get(i)[0].equals(trees.get(j)[0])) && dateTreeJ.after(dateTreeI)){

                        ObjectReader reader = repository.newObjectReader();
                        CanonicalTreeParser oldTree = new CanonicalTreeParser();
                        ObjectId oldCommit = ObjectId.fromString(trees.get(i)[3].substring(5, trees.get(i)[3].indexOf("-") - 1));
                        oldTree.reset(reader, oldCommit);

                        ObjectId newCommit = ObjectId.fromString(trees.get(j)[3].substring(5, trees.get(j)[3].indexOf("-") - 1));
                        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                        newTreeIter.reset(reader, newCommit);

                        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                        diffFormatter.setRepository(git.getRepository());
                        diffFormatter.setContext(0);
                        List<DiffEntry> entries = diffFormatter.scan(newTreeIter, oldTree);

                        for(DiffEntry entry: entries){

                            for(Edit edit: diffFormatter.toFileHeader(entry).toEditList()){

                                linesDeleted = linesDeleted + edit.getEndA() - edit.getBeginA();
                                linesAdded = linesAdded + edit.getEndB() -edit.getBeginB();
                                linesTouched = linesAdded + linesDeleted;

                            }

                        }

                        out.add(new String[]{trees.get(j)[2], trees.get(i)[0], Integer.toString(linesAdded), Integer.toString(linesDeleted), Integer.toString(linesTouched)});

                        linesAdded = 0;
                        linesDeleted = 0;
                        linesTouched = 0;

                    }

                }

            }

            csvWriter.writeAll(out);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /*
    * Compute LOC added and add index to trees
    * */
    private static void retrieveLOCFromTreesWithIndexAndSum(){

        try(FileReader fileReader = new FileReader(outLoc);CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(versionInfo); CSVReader csvReader1 = new CSVReader(fileReader1);
            FileWriter fileWriter = new FileWriter(outLocIndex); CSVWriter csvWriter = new CSVWriter(fileWriter)
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

            csvWriter.writeNext(new String[]{"Date", "Class", "LinesAdded", "LinesDeleted", "LinesTouched", "Index", "Sum"});
            csvWriter.writeAll(outLIndex);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    /*
    * Compute MAX LOC added, AVG LOC added
    * */
    private static void retrieveMaxAndAverageLoc(){

        try(FileReader fileReader = new FileReader(outLocIndex); CSVReader csvReader = new CSVReader(fileReader);
            FileWriter fileWriter = new FileWriter(maxAverage); CSVWriter csvWriter = new CSVWriter(fileWriter)

        ){

            int max;
            int first;
            int second;
            int temp = 0;
            int count = 0;

            Map<Keys, Values> finalmap = new HashMap<>();

            csvReader.readNext();
            List<String[]> outLIndx = csvReader.readAll();
            List<String[]> maxAvg = new ArrayList<>();

            for(String[] strings: outLIndx){

                for(String[] strings1: outLIndx){

                    if((strings[1].equals(strings1[1])) && (strings[5].equals(strings1[5]))){

                        //temp = temp + Integer.parseInt(strings[2]);

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

            for(Map.Entry<Keys, Values> entry: finalmap.entrySet()){

                if(entry.getValue().getCounter() == 0){

                    maxAvg.add(new String[]{entry.getValue().getString1(), entry.getKey().key2.toString(), entry.getValue().getString2(), String.valueOf(entry.getValue().getTemp()), entry.getKey().key1.toString()});

                }else{

                    maxAvg.add(new String[]{entry.getValue().getString1(), entry.getKey().key2.toString(), entry.getValue().getString2(), String.valueOf((entry.getValue().getTemp()/entry.getValue().getCounter())), entry.getKey().key1.toString()});

                }


            }

            csvWriter.writeNext(new String[]{"Date", "Class", "MAXLOC", "AVGLOC", "Index"});
            csvWriter.writeAll(maxAvg);


        }catch(IOException e){
            e.printStackTrace();
        }

    }

    /*
    * Compute ChgSetSize
    * */
    private static void calculateChgSetSize(){

        try(FileReader fileReader = new FileReader(blameCommitIntersection); CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(versionInfo); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(chgSetSize); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

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

            csvWriter.writeNext(new String[]{"Class", "Index", "ChgSetSize"});
            csvWriter.writeAll(listChgSetSize);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    /*
    * Compute Size and Age
    * */
    private static void calculateSizeAgeAuthors() throws IOException {

        Git git = new Git(new FileRepository(cPath));

        Date commitTime;
        Date verTime;
        int size;
        int weeks;
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        try(FileReader fileReader = new FileReader(csvClassPath);CSVReader csvReader = new CSVReader(fileReader);
        FileReader fileReader1 = new FileReader(versionInfo); CSVReader csvReader1 = new CSVReader(fileReader1);
        FileWriter fileWriter = new FileWriter(sizeMetric); CSVWriter csvWriter = new CSVWriter(fileWriter)){

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

                    String key = strings1[0].replace(stringToReplace, "");

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
            csvWriter.writeNext(new String[]{"Index", "Class", "Size", "Age", "NumberOfAuthors"});
            csvWriter.writeAll(listAge);

        } catch (ParseException | GitAPIException e) {
            e.printStackTrace();
        }

    }

    /*
    * Compute final csv with all metrics
    * */
    private static void createFinalCSV(){

        try(FileReader fileReader = new FileReader(chgSetSize);
            CSVReader csvReader = new CSVReader(fileReader);
            FileReader fileReader1 = new FileReader(maxAverage);
            CSVReader csvReader1 = new CSVReader(fileReader1);
            FileReader fileReader2 = new FileReader(maxAvgChgSetSize);
            CSVReader csvReader2 = new CSVReader(fileReader2);
            FileReader fileReader3 = new FileReader(outLocIndex);
            CSVReader csvReader3 = new CSVReader(fileReader3);
            FileReader fileReader4 = new FileReader(preFinalCsv);
            CSVReader csvReader4 = new CSVReader(fileReader4);
            FileReader fileReader5 = new FileReader(sizeMetric);
            CSVReader csvReader5 = new CSVReader(fileReader5);
            FileWriter fileWriter = new FileWriter(finalCSV);
            CSVWriter csvWriter = new CSVWriter(fileWriter)){

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
            List<String[]> listPreFinalCsv = csvReader4.readAll();


            //Index, Class, Size, Age, NumberOfAuthors, Sum, MaxChg, AvgChg, MaxLoc, AvgLoc, ChgSetSize
            Map<Keys, Values> map = new HashMap<>();

            List<String[]> finalList = new ArrayList<>();

            for(String[] stringSizeMetric: listSizeMetric){

                Keys keys = new Keys(stringSizeMetric[0], stringSizeMetric[1]);
                Values values = new Values(stringSizeMetric[2], stringSizeMetric[3], stringSizeMetric[4], "0", "0", "0", "0", "0", "0");

                map.put(keys, values);

            }

            for(String[] stringOutLocIndex: listOutLocIndex){

                Keys keys = new Keys(stringOutLocIndex[5], stringOutLocIndex[1]);

                if(map.containsKey(keys)){

                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), map.get(keys).getString3(), stringOutLocIndex[6], map.get(keys).getString5(), map.get(keys).getString6(), map.get(keys).getString7(), map.get(keys).getString8(), map.get(keys).getString9());

                    map.replace(keys, values);

                }

            }

            for(String[] stringMaxAvgChg: listMaxAvgChgSetSize){

                Keys keys = new Keys(stringMaxAvgChg[1], stringMaxAvgChg[0]);

                if(map.containsKey(keys)){

                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), map.get(keys).getString3(), map.get(keys).getString4(), stringMaxAvgChg[2], stringMaxAvgChg[3], map.get(keys).getString7(), map.get(keys).getString8(), map.get(keys).getString9());

                    map.replace(keys, values);

                }

            }

            for(String[] stringMaxAverage: listMaxAverage){

                Keys keys = new Keys(stringMaxAverage[4], stringMaxAverage[1]);

                if(map.containsKey(keys)){

                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), map.get(keys).getString3(), map.get(keys).getString4(), map.get(keys).getString5(), map.get(keys).getString6(), stringMaxAverage[2], stringMaxAverage[3], map.get(keys).getString9());
                    map.replace(keys, values);

                }

            }

            for(String[] stringChgSetSize: listChgSetSize){

                Keys keys = new Keys(stringChgSetSize[1], stringChgSetSize[0]);

                if(map.containsKey(keys)){

                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), map.get(keys).getString3(), map.get(keys).getString4(), map.get(keys).getString5(), map.get(keys).getString6(), map.get(keys).getString7(), map.get(keys).getString8(), stringChgSetSize[2]);
                    map.replace(keys, values);

                }

            }

            for(String[] stringPreFinal: listPreFinalCsv){

                Keys keys = new Keys(stringPreFinal[0], stringPreFinal[1]);

                if(map.containsKey(keys)){

                    Values values = new Values(map.get(keys).getString1(), map.get(keys).getString2(), map.get(keys).getString3(), map.get(keys).getString4(), map.get(keys).getString5(), map.get(keys).getString6(), map.get(keys).getString7(), map.get(keys).getString8(), map.get(keys).getString9(), stringPreFinal[2]);

                    map.replace(keys, values);

                }

            }

           for(Map.Entry<Keys, Values> entry: map.entrySet()){

               //finalList.add(new String[]{entry.getKey().key1.toString(), entry.getKey().key2.toString(), entry.getValue().getString1(), entry.getValue().getString2(), entry.getValue().getString3(), entry.getValue().getString4()});


               if(entry.getValue().getString10() != null){

                   finalList.add(new String[]{entry.getKey().key1.toString(), entry.getKey().key2.toString(), entry.getValue().getString1(), entry.getValue().getString2(), entry.getValue().getString3(), entry.getValue().getString4()
                           , entry.getValue().getString5(), entry.getValue().getString6(), entry.getValue().getString7(), entry.getValue().getString8(), entry.getValue().getString9(), entry.getValue().getString10()});

               }

            }

            //Index, Class, Size, Age, NumberOfAuthors, Sum, MaxChg, AvgChg, MaxLoc, AvgLoc, ChgSetSize

            csvWriter.writeNext(new String[]{"Index", "Class", "Size", "Age", "NumberOfAuthors", "Sum", "MaxChg", "AvgChg", "MaxLoc", "AvgLoc", "ChgSetSize"});
            csvWriter.writeAll(finalList);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, GitAPIException {

        //Project BOOKKEEPER

        /*importProp("BOOKKEEPER");

        createDirectory("BOOKKEEPER");
        cloneRepository();
        createCsvClass();
        executeBlame();
        getVersionInfo("BOOKKEEPER");
        writeCommit("BOOKKEEPER");
        retrieveTicketAvFVFromJira("BOOKKEEPER");
        retrieveTicketOnlyFVFromJira("BOOKKEEPER");
        blameCommit();
        blameJira();
        blameJiraFVOnly();
        getDefective();
        //ticket with Affected Version
        determineOV(0, blameJiraIntersection, blameJiraIntersectionOV);
        //ticket without Affected Version
        determineOV(1, blameJiraIntersectionFVOnly, blameJiraIntersectionOVFVOnly);
        double P = getProportion();
        calculatePredictedIV(P);
        sumBuggyPredicted();
        createPrefinalCSV();
        retrieveLOCFromTrees();
        retrieveLOCFromTreesWithIndexAndSum();
        retrieveMaxAndAverageLoc();
        calculateSizeAgeAuthors();
        calculateChgSetSize();
        retrieveMaxAndAverageChgSetSize();
        createFinalCSV();

*/

        //Project ZOOKEEPER

        importProp("ZOOKEEPER");

        createDirectory("ZOOKEEPER");
        cloneRepository();
        createCsvClass();
        System.out.println("fatto");

        executeBlame();
        getVersionInfo("ZOOKEEPER");
        writeCommit("ZOOKEEPER");
        retrieveTicketAvFVFromJira("ZOOKEEPER");
        retrieveTicketOnlyFVFromJira("ZOOKEEPER");
        blameCommit();
        blameJira();
        blameJiraFVOnly();
        getDefective();
        //ticket with Affected Version
        determineOV(0, blameJiraIntersection, blameJiraIntersectionOV);
        //ticket without Affected Version
        determineOV(1, blameJiraIntersectionFVOnly, blameJiraIntersectionOVFVOnly);
        double P = getProportion();
        calculatePredictedIV(P);
        sumBuggyPredicted();
        createPrefinalCSV();
        retrieveLOCFromTrees();
        retrieveLOCFromTreesWithIndexAndSum();
        retrieveMaxAndAverageLoc();
        calculateSizeAgeAuthors();
        calculateChgSetSize();
        retrieveMaxAndAverageChgSetSize();
        createFinalCSV();

    }

}
