/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.norberg.automatter.jackson.AutoMatterModule;
import se.carlengstrom.internetonastick.http.payload.*;
import se.carlengstrom.internetonastick.job.AppendLineFileToMarkovJob;
import se.carlengstrom.internetonastick.job.Job;
import se.carlengstrom.internetonastick.job.JobRunner;
import se.carlengstrom.internetonastick.model.Markov;
import se.carlengstrom.internetonastick.model.properties.feeder.NewLineDelimitedFeeder;
import se.carlengstrom.internetonastick.model.properties.joiner.BasicJoiners;
import se.carlengstrom.internetonastick.model.properties.splitter.BasicSplitters;
import spark.Request;
import spark.Response;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class HttpServer {

    private final Map<String, Map<String, Markov>> markovs = new HashMap<>();
    private final Map<String, Job> jobs = new HashMap<>();
    private final JobRunner runner;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new AutoMatterModule());

    public HttpServer(final JobRunner runner) {
        this.runner = runner;
        get("/", (req, res) -> UsageApi());
        get("/:user/createMarkov", (req, res) -> getCreateMarkov(req,res));
        post("/:user/createMarkov", (req, res) -> postCreateMarkov(req,res));

        get("/:user/:markov/appendLineFile", (req,res) -> getAppendLineFile(req,res));
        post("/:user/:markov/appendLineFile", (req,res) -> postAppendLineFile(req,res));

        get("/jobs/:jobName", (req,res) -> getJob(req,res));
        get("/jobs", (req,res) -> getJobs(req,res));

        get("/:user/:markov", (req,res) -> getMarkov(req,res));
    }

    private String UsageApi() {
        return "Hi there. GET an endpoint to see what you can do with it. Available enpoints are (names starting with ':' are variables):</br>" +
                "<ul>" + 
                "<li>/:user/createMarkov/</li>" +
                "<li>/:user/:markov/appendLineInFile</li>" +
                "<li>/:user/:markov</li>" +
                "<li>/jobs/:jobName/</li>" +
                "<li>/jobs</li>" +
                "</ul>";
    }

    private String getCreateMarkov(final Request req, final Response res) {
        return "<p>POST here with JSON payload of a single name parameter to create or overwrite a markov object.</p>" + 
                "<p>Response is JSON with the name</p>";
    }

    private String postCreateMarkov(final Request req, final Response res) throws IOException {
        final String user = req.params("user");
        final CreateMarkovRequest createRequest = mapper.readValue(req.body(), CreateMarkovRequest.class);

        if(!markovs.containsKey(user)) {
            markovs.put(user, new HashMap<>());
        }
        markovs.get(user).put(createRequest.name(), new Markov(
            BasicSplitters.SPACE_SPLITTER,
            3,
            BasicJoiners.SPACE_JOINER));

        final String path = "data/"+user+"/"+createRequest.name()+"/";
        final File directory = new File(path);
        directory.mkdirs();
        final File file = new File(path+"markov.json");
        if(!file.exists()) {
            file.createNewFile();
        }
        try (final FileOutputStream stream = new FileOutputStream(file, false)) {
            stream.write(req.bodyAsBytes());
        }

        res.status(202);
        return req.body();
    }

    private String getAppendLineFile(final Request req, final Response res) {
        return "<p>POST here with JSON payload of a single data parameter ( { data:\"BASE64\" } ). data should be the base64 of the data you wish to append."+
                "Each line will be interpreted as a single sentence, regardless of content.</p>" + 
                "<p>Response is JSON with a job which you can query for status later on at /jobs/:jobname</p>" +
                "<p>Please only post stuff that you have, you know, the <i>legal right</i> to upload and whatnot</p>";
    }

    private String postAppendLineFile(final Request req, final Response res) throws IOException {
        final String user = req.params("user");
        final String markovName = req.params("markov");
        final String asdasd = req.body();
        final AppendLineFileRequest request = mapper.readValue(req.body(), AppendLineFileRequest.class);

        final String path = "data/"+user+"/"+markovName+"/source.txt";
        final byte[] markovData = DatatypeConverter.parseBase64Binary(request.data());

        final File folder = new File("data/"+user+"/"+markovName+"/");
        folder.mkdirs();

        File source = new File(path);
        if(!source.exists()) {
            source.createNewFile();
        }
        try (FileOutputStream stream = new FileOutputStream(path, true)) {
            stream.write(markovData);
        }

        final File temp = File.createTempFile("markov-input-partial", "txt");
        try (FileOutputStream stream = new FileOutputStream(temp, true)) {
            stream.write(markovData);
        }

        final Markov markov = markovs.get(user).get(markovName);
        final AppendLineFileToMarkovJob job = new AppendLineFileToMarkovJob(
            markov,
            new NewLineDelimitedFeeder(new FileInputStream(folder.getAbsolutePath() + "/source.txt")));

        runner.scheduleJob(job);

        final String jobName = user+System.currentTimeMillis();
        jobs.put(jobName, job);
        return mapper.writeValueAsString(new CreateMarkovResponseBuilder()
                .jobName(jobName)
                .build());
    }

    private String getJob(final Request req, final Response res) throws JsonProcessingException {
        final String jobname = req.params("jobName");
        final Job job = jobs.get(jobname);
        if(job == null) {
            return "GET here with a valid job name to see its status";
        } else {
            return mapper.writeValueAsString(new GetJobResponseBuilder()
                    .jobName(jobname)
                    .sample(job.getSample())
                    .status(job.getStaus().toString())
                    .stautsString(job.getStatusString())
                    .durationMills(job.getDuration())
                    .build());
        }
    }

    private String getMarkov(final Request req, final Response res) throws JsonProcessingException {
        final String user = req.params("user");
        final String markovName = req.params("markov");

        final Markov markov = markovs.get(user).get(markovName);
        if(markov != null) {
            try {
                return mapper.writeValueAsString(new SentenceResponseBuilder()
                        .sentence(markov.generateSentence())
                        .build());
            } catch (IllegalStateException ise) {
                res.status(500);
                return ise.getMessage();
            }
        } else {
            return "GET here with a valid markov name to get a sentence";
        }
    }

    private String getJobs(final Request req, final Response res) throws JsonProcessingException {
        return mapper.writeValueAsString(new GetJobsResponseBuilder()
                .jobs(markovs.keySet())
                .build());
    }

    public static void main(String[] args) {
        JobRunner runner = new JobRunner();
        runner.startScheduler();
        new HttpServer(runner);
    }
}
