package se.carlengstrom.internetonastick.model.builders;

import se.carlengstrom.internetonastick.job.AppendLineFileToMarkovJob;
import se.carlengstrom.internetonastick.model.Markov;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.Scanner;

import se.carlengstrom.internetonastick.job.Job;

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
    public static Markov appendMarkovFromText(Markov m, String path) throws IOException {
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
    public static Markov appendMarkovFromLineDelimitedText(Markov m, String path, Job job) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
        String read;
        while((read = reader.readLine()) != null ) {
            m.insertSentence(read, job);
        }
        return m;
    }

    public static Markov appendMarkovFromPeriodDelimitedText(
        final Markov markov,
        final String path,
        final Job job) throws IOException {
        final Scanner scanner = new Scanner(new File(path));
        scanner.useDelimiter("\\.");
        while(scanner.hasNext()) {
            final String sentence = scanner.next();
            if(!sentence.isEmpty()) {
                markov.insertSentence(sentence, job);
            }
        }
        return markov;
    }
}
