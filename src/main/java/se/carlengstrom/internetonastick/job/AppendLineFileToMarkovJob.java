/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.job;

import se.carlengstrom.internetonastick.model.Markov;
import se.carlengstrom.internetonastick.model.MarkovBuilder;
import se.carlengstrom.internetonastick.model.properties.BuilderParameters;
import se.carlengstrom.internetonastick.model.properties.feeder.SentenceFeeder;

import java.io.IOException;

/**
 *
 * @author Eng
 */
public class AppendLineFileToMarkovJob extends Job {
    private final SentenceFeeder feeder;

    public AppendLineFileToMarkovJob(final Markov markov, final SentenceFeeder feeder) {
        super(markov);
        this.feeder = feeder;
    }
    
    
    @Override
    public void jobRun() throws IOException {
        final BuilderParameters params = new BuilderParameters(
            getMarkov(),
            feeder,
            this);

        MarkovBuilder.appendToMarkov(params);

        //final ObjectMapper mapper = new ObjectMapper();
        //mapper.writeValueAsString(getMarkov());
    }
}
