/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.job;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import se.carlengstrom.internetonastick.model.Markov;
import se.carlengstrom.internetonastick.model.builders.MarkovBuilder;

/**
 *
 * @author Eng
 */
public class AppendLineFileToMarkovJob extends Job {

    public enum Delimiter {
        LINE,
        PERIOD
    }

    private final String directory;
    private final Delimiter delimiter;

    public AppendLineFileToMarkovJob(Markov m, String directory, Delimiter delimiter) {
        super(m);
        this.directory = directory;
        this.delimiter = delimiter;
    }
    
    
    @Override
    public void jobRun() throws IOException {
        if(delimiter == Delimiter.LINE) {
            MarkovBuilder
                .appendMarkovFromLineDelimitedText(getMarkov(), directory + "/source.txt", this);
        } else if (delimiter == Delimiter.PERIOD) {
            MarkovBuilder
                .appendMarkovFromPeriodDelimitedText(getMarkov(), directory + "/source.txt", this);
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(getMarkov());
        try (FileOutputStream stream = new FileOutputStream(directory+"/markov.json", false)) {
            stream.write(json.getBytes());
        }
    }
    
}
