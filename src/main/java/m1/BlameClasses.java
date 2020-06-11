package m1;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static utility.ImportProperties.*;


public class BlameClasses {

    public static void executeBlame(){

        try(FileReader fileReader = new FileReader(getCsvClassPath());
            CSVReader csvReader = new CSVReader(fileReader);
            FileWriter fileWriter = new FileWriter(getCsvBlame());
            CSVWriter csvWriter = new CSVWriter(fileWriter)){

            List<String[]> list = csvReader.readAll();
            List<String[]> list1 = new ArrayList<>();

            FileRepository fileRepository = new FileRepository(getcPath());
            Git git = new Git(fileRepository);

            for(String[] string: list){

                String key = string[0].replace(getStringToReplace(), "");

                BlameCommand command = git.blame().setStartCommit(git.getRepository().resolve("HEAD")).setFilePath(key);

                BlameResult result = command.call();

                int size = result.getResultContents().size();

                String pattern = "yyyy-MM-dd HH:mm";
                DateFormat df = new SimpleDateFormat(pattern);

                for(int i = 0; i < size; i++){

                    list1.add(new String[] {df.format(result.getSourceAuthor(i).getWhen()), result.getSourceCommit(i).getTree().toString(), string[1]});

                }

            }

            csvWriter.writeNext(new String[]{"Date", "Tree", "Class"});

            csvWriter.writeAll(list1);
            csvWriter.flush();

        } catch (IOException | GitAPIException e) {

            e.printStackTrace();

        }

    }

}
