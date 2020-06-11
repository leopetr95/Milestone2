package m1.logic;

import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utility.ImportProperties.*;

public class RetrieveClasses {

    static final Logger logger = Logger.getLogger(String.valueOf(JiraVersion.class));

    /*
    * Clone the repository in local directory
    * */
    public static void cloneRepository() throws GitAPIException {

        File dir = new File(getPath());

        if (!dir.exists()) {

            logger.info("Cloning repository");

            if (dir.mkdir()) {

                Git.cloneRepository().setURI(getUrlProject()).setDirectory(dir).call();
                logger.info("Successful");

            } else {

                logger.info("Directory creation failed");

            }

        }

    }

    /*
    * Create a csv file containing the path of all the files .java
    * */
    public static void createCsvClass(){

        try(Stream<Path> walk = Files.walk(Paths.get(getPath()));
             FileWriter fileWriter = new FileWriter(getCsvClassPath());
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            List<String> result = walk.map(Path::toString).filter(f -> f.endsWith(".java")).collect(Collectors.toList());

            String string1;

            for(String string: result){

                string = string.replace("\\", "/");

                string1 = new StringBuilder(string).reverse().toString();
                string1 = string1.substring(0, string1.indexOf("/"));
                string1 = new StringBuilder(string1).reverse().toString();

                csvWriter.writeNext(new String[]{string, string1});

            }

            csvWriter.flush();

        }catch (IOException e){

            e.printStackTrace();

        }

    }

}
