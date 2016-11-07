package se.carlengstrom.internetonastick.model;

import se.carlengstrom.internetonastick.model.Markov;
import se.carlengstrom.internetonastick.model.properties.BuilderParameters;

import java.io.IOException;

/**
 * Created by eng on 2/13/16.
 */
public class MarkovBuilder {

    /**
     * Builds a Markov from file where each sentence is assumed to be a new line. Any special characters on each line
     * is assumed to be part of the sentence.
     */
    public static Markov appendToMarkov(final BuilderParameters params) throws IOException {
        String read;
        while((read = params.getFeeder().nextSentence()) != null ) {
            params.getMarkov().insertSentence(read, params.getJob());
        }
        return params.getMarkov();
    }
}
