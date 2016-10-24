/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import se.carlengstrom.internetonastick.job.AppendLineFileToMarkovJob;
import se.carlengstrom.internetonastick.job.AppendLineFileToMarkovJob.Delimiter;
import se.carlengstrom.internetonastick.job.Job;
import se.carlengstrom.internetonastick.job.JobRunner;
import se.carlengstrom.internetonastick.model.Markov;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

/**
 *
 * @author Eng
 */
public class HttpServer {

    private final Map<String, Map<String, Markov>> markovs = new HashMap<>();
    private final Map<String, Job> jobs = new HashMap<>();
    private JobRunner runner;

    public HttpServer(JobRunner runner) {
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

    private String getCreateMarkov(Request req, Response res) {
        return "<p>POST here with JSON payload of a single name parameter to create or overwrite a markov object.</p>" + 
                "<p>Response is JSON with the name</p>";
    }

    private String postCreateMarkov(Request req, Response res) throws IOException {
        String user = req.params("user");
        JsonObject jsonObject = new JsonParser().parse(req.body()).getAsJsonObject();
        String markovName = jsonObject.get("name").getAsString();

        if(!markovs.containsKey(user)) {
            markovs.put(user, new HashMap<>());
        }
        markovs.get(user).put(markovName, new Markov());

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(markovs.get(user).get(markovName));

        String path = "data/"+user+"/"+markovName+"/";
        File directory = new File(path);
        directory.mkdirs();
        File file = new File(path+"markov.json");
        if(!file.exists()) {
            file.createNewFile();
        }
        try (FileOutputStream stream = new FileOutputStream(file, false)) {
            stream.write(json.getBytes());
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("name", markovName);
        return obj.toString();
    }

    private String getAppendLineFile(Request req, Response res) {
        return "<p>POST here with JSON payload of a single data parameter ( { data:\"BASE64\" } ). data should be the base64 of the data you wish to append."+
                "Each line will be interpreted as a single sentence, regardless of content.</p>" + 
                "<p>Response is JSON with a job which you can query for status later on at /jobs/:jobname</p>" +
                "<p>Please only post stuff that you have, you know, the <i>legal right</i> to upload and whatnot</p>";
    }

    private String postAppendLineFile(Request req, Response res) throws IOException {
        String user = req.params("user");
        String markov = req.params("markov");
        JsonObject jsonObject = new JsonParser().parse(req.body()).getAsJsonObject();

        String path = "data/"+user+"/"+markov+"/source.txt";
        byte[] markovData = DatatypeConverter.parseBase64Binary(jsonObject.get("data").getAsString());

        File folder = new File("data/"+user+"/"+markov+"/");
        folder.mkdirs();

        File source = new File(path);
        if(!source.exists()) {
            source.createNewFile();
        }
        try (FileOutputStream stream = new FileOutputStream(path, true)) {
            stream.write(markovData);
        }

        File temp = File.createTempFile("markov-input-partial", "txt");
        try (FileOutputStream stream = new FileOutputStream(temp, true)) {
            stream.write(markovData);
        }

        Markov m = markovs.get(user).get(markov);
        AppendLineFileToMarkovJob job = new AppendLineFileToMarkovJob(
            m,
            folder.getAbsolutePath(),
            Delimiter.LINE);

        runner.scheduleJob(job);

        String jobName = user+System.currentTimeMillis();
        jobs.put(jobName, job);

        JsonObject obj = new JsonObject();
        obj.addProperty("jobName", jobName);
        return obj.toString();
    }

    private String getJob(Request req, Response res) {
        String jobname = req.params("jobName");
        Job j = jobs.get(jobname);
        if(j == null) {
            return "GET here with a valid job name to see its status";
        } else {
            JsonObject obj = new JsonObject();
            obj.addProperty("jobName", jobname);
            obj.addProperty("status", j.getStaus().toString());
            obj.addProperty("statusString", j.getStatusString());
            obj.addProperty("sample", j.getSample());
            obj.addProperty("duration", j.getDuration() + " ms");
            return obj.toString();
        }
    }

    private String getMarkov(Request req, Response res) {
        String user = req.params("user");
        String markov = req.params("markov");

        Markov m = markovs.get(user).get(markov);
        if(m != null) {
            try {
                JsonObject obj = new JsonObject();
                obj.addProperty("sentence", m.generateSentence());
                return obj.toString();
            } catch (IllegalStateException ise) {
                res.status(400);
                JsonObject obj = new JsonObject();
                obj.addProperty("message", ise.getMessage());
                return obj.toString();
            }
        } else {
            return "GET here with a valid markov name to get a sentence";
        }
    }

    private String getJobs(Request req, Response res) {
        JsonArray arr = new JsonArray();
        jobs.keySet().forEach(arr::add);
        JsonObject obj = new JsonObject();
        obj.add("jobs", arr);
        return obj.toString();
    }

    public static void main(String[] args) {
        JobRunner runner = new JobRunner();
        runner.startScheduler();
        new HttpServer(runner);
    }
}
