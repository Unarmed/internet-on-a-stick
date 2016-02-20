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

    private final String directory;

    public AppendLineFileToMarkovJob(Markov m, String directory) {
        super(m);
        this.directory = directory;        
    }
    
    
    @Override
    public void jobRun() throws IOException {
        Markov out = MarkovBuilder.appendMarkovFromLineDelimitedText(getMarkov(), directory+"/source.txt", this);
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(getMarkov());
        try (FileOutputStream stream = new FileOutputStream(directory+"/markov.json", false)) {
            stream.write(json.getBytes());
        }
    }
    
}
