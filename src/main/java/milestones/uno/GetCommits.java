package milestones.uno;

import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static utility.ImportProperties.*;
import static milestones.uno.RetrieveClasses.logger;

public class GetCommits {

    private GetCommits(){}

    private static final String PROJECT1 = "BOOKKEEPER";
    private static final String PROJECT2 = "ZOOKEEPER";

    /*
    * Write in a csv file all the commits
    * */
    public static void writeCommit(String projName){

        try(FileWriter fileWriter = new FileWriter(getCsvCommit()); CSVWriter csvWriter = new CSVWriter(fileWriter)){

            Git git = Git.open(new File(getCompletePath()));

            Repository repository = FileRepositoryBuilder.create(new File(getCompletePath()));
            String repo = String.valueOf(repository);

            logger.info(repo);

            List<Ref> branches = git.branchList().call();

            for(Ref ref: branches){

                logger.info(ref.getName());

            }

            Iterable<RevCommit> commits = git.log().all().call();

            List<String[]> dataList = new ArrayList<>();

            for(RevCommit revCommit: commits){

                String pattern = "yyyy-MM-dd HH:mm";
                DateFormat df = new SimpleDateFormat(pattern);
                String date = df.format(revCommit.getAuthorIdent().getWhen());

                String fullMessage = revCommit.getFullMessage();
                String tree = revCommit.getTree().toString();

                String shortMessage;

                if((projName.equals(PROJECT1)) && (fullMessage.length() > 14) && (fullMessage.substring(0, 11).contains("BOOKKEEPER-"))){

                    shortMessage = fullMessage.substring(0, fullMessage.indexOf(" ") - 1);

                    dataList.add(new String[]{date, tree, shortMessage});

                }else if((projName.equals(PROJECT2)) && (fullMessage.length() > 14) && (fullMessage.substring(0, 10).contains("ZOOKEEPER-"))) {

                    shortMessage = fullMessage.substring(0, fullMessage.indexOf(" "));

                    if ((shortMessage.contains("."))) {

                        shortMessage = shortMessage.substring(0, shortMessage.indexOf("."));

                    }else if(shortMessage.contains(":")) {

                        shortMessage = shortMessage.substring(0, shortMessage.indexOf(":"));

                    }

                    dataList.add(new String[]{date, tree, shortMessage});

                }

            }

            //Write header
            csvWriter.writeNext(new String[]{"Date", "Tree", "Ticket"});

            csvWriter.writeAll(dataList);
            csvWriter.flush();

        } catch (IOException | GitAPIException e) {

            e.printStackTrace();

        }

    }

}
