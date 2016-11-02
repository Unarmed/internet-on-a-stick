package se.carlengstrom.internetonastick.model.builders;

import se.carlengstrom.internetonastick.job.Job;
import se.carlengstrom.internetonastick.model.Markov;
import se.carlengstrom.internetonastick.model.builders.feeders.EnglishSentenceFeeder;
import se.carlengstrom.internetonastick.model.builders.feeders.NewLineDelimitedFeeder;
import se.carlengstrom.internetonastick.model.builders.feeders.SentenceFeeder;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by eng on 2/13/16.
 */
public class MarkovBuilder {

    /**
     * Builds a Markov from file with "normal" text. Normal text is defined as text where sentences are ended with
     * a full stop and where sentences may traverse newlines.For more information on delimiters see
     * BreakIterator.getSentenceInstance(Locale.ENGLISH)
     *
     * Good for parsing books and the like
     */
    public static Markov appendMarkovFromText(final Markov m, final String path,  final Job job) throws IOException {
        final SentenceFeeder feeder = new EnglishSentenceFeeder(new FileInputStream(path));
        String read;
        while((read = feeder.nextSentence()) != null ) {
            m.insertSentence(read, job);
        }
        return m;
    }

    /**
     * Builds a Markov from file where each sentence is assumed to be a new line. Any special characters on each line
     * is assumed to be part of the sentence.
     */
    public static Markov appendMarkovFromLineDelimitedText(final Markov m, final String path, final Job job) throws IOException {
        final SentenceFeeder feeder = new NewLineDelimitedFeeder(new FileInputStream(path));
        String read;
        while((read = feeder.nextSentence()) != null ) {
            m.insertSentence(read, job);
        }
        return m;
    }
}
