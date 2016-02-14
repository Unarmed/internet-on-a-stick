package se.carlengstrom.internetonastick.model.builders;

import se.carlengstrom.internetonastick.model.Markov;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.Locale;

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
    public static Markov buildMarkovFromText(String path) throws IOException {
        Markov m = new Markov();
        String allText = String.join(" ", Files.readAllLines(Paths.get(path)));
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.ENGLISH);

        it.setText(allText);
        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            m.insertSentence(allText.substring(start,end));
        }
        return m;
    }

    /**
     * Builds a Markov from file where each sentence is assumed to be a new line. Any special characters on each line
     * is assumed to be part of the sentence.
     */
    public static Markov buildMarkovFromLineDelimitedText(String path) throws IOException {
        Markov m = new Markov();
        Files.readAllLines(Paths.get(path)).forEach(m::insertSentence);
        return m;
    }
}
