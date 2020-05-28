package logic;

import com.opencsv.CSVWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static logic.GetReleaseInfo.readJsonFromUrl;
import static utility.ImportProperties.*;


public class JiraVersion {

    private JiraVersion(){}

    private static final String FIELDS = "fields";

    public static void retrieveTicketOnlyFVFromJira(String projName) throws IOException {

        List<String[]> list = new ArrayList<>();

        String urlFixedVersionOnly = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projName+ "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%20" +
                "fixVersion%20!=%20null%20AND%20affectedVersion%20=%20null%20ORDER%20BY%20fixVersion,affectedVersion%20ASC"
                +",created&startAt=0&maxResults=1000";

        JSONObject json = readJsonFromUrl(urlFixedVersionOnly);
        JSONArray issues = json.getJSONArray("issues");

        int i;
        int j;
        Integer counter = 0;

        list.add(new String[]{"Index", "Ticket", "FixVersion"});

        for (i = 0; i < issues.length(); i++) {

            String ticket = issues.getJSONObject(i).get("key").toString();

            /**
             * Questi ulteriri array JSON sono stati necessari per accedere alle sottoliste presenti all'interno
             * di issues, cioè filed, che contiene sia FixVersions che version per determinare AV e FV.
             */
            JSONArray fixedVersions = issues.getJSONObject(i).getJSONObject(FIELDS).getJSONArray("fixVersions");

            String fixVersion = "";

            /**
             * Il ciclo è necessario in quanto alcuni ticket possiedono molteplici fixed-version. In questo
             * modo li includiamo all'interno del file e manteniamo l'index corretto sfruttando la variabile
             * counter.
             */

            for (j = 0; j < fixedVersions.length(); j++) {

                counter++;
                fixVersion = fixedVersions.getJSONObject(j).get("name").toString();
                list.add(new String[]{counter.toString(), ticket, fixVersion});

            }

        }

        try (FileWriter fileWriter = new FileWriter(getVersionsFVOnly()); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            csvWriter.writeAll(list);

        }

    }


    //Creo un file CSV in cui per ogni ticket di Jira ho l'affected version, la fixed version e la Injected version

    public static void retrieveTicketAvFVFromJira(String projName) throws IOException {

        List<String[]> list = new ArrayList<>();

        String urlAffectedVersion = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projName+ "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%20" +
                "fixVersion%20!=%20null%20AND%20affectedVersion%20!=%20null%20ORDER%20BY%20fixVersion,affectedVersion%20ASC"
                +",created&startAt=0&maxResults=1000";

        JSONObject json = readJsonFromUrl(urlAffectedVersion);
        JSONArray issues = json.getJSONArray("issues");


        int i;
        int j;
        Integer counter = 0;

        list.add(new String[]{"Index", "Ticket", "FixVersion", "AffectedVersion"});

        for (i = 0; i < issues.length(); i++) {

            String ticket = issues.getJSONObject(i).get("key").toString();
            /**
             * Questi ulteriri array JSON sono stati necessari per accedere alle sottoliste presenti all'interno
             * di issues, cioè filed, che contiene sia FixVersions che version per determinare AV e FV.
             */
            JSONArray fixedVersions = issues.getJSONObject(i).getJSONObject(FIELDS).getJSONArray("fixVersions");
            JSONArray affectedVersions = issues.getJSONObject(i).getJSONObject(FIELDS).getJSONArray("versions");

            String fixVersion = "";
            String affectedVersion = "";


            //prendo la prima perchè la più vecchia
            affectedVersion = affectedVersions.getJSONObject(0).get("name").toString();

            /**
             * Il ciclo è necessario in quanto alcuni ticket possiedono molteplici fixed-version. In questo
             * modo li includiamo all'interno del file e manteniamo l'index corretto sfruttando la variabile
             * counter.
             */

            for (j = 0; j < fixedVersions.length(); j++) {

                counter++;
                fixVersion = fixedVersions.getJSONObject(j).get("name").toString();
                list.add(new String[]{counter.toString(), ticket, fixVersion, affectedVersion});

            }

        }

        try (FileWriter fileWriter = new FileWriter(getVersionsAV()); CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            csvWriter.writeAll(list);

        }

    }


}
