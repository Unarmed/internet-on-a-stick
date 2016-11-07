package se.carlengstrom.internetonastick.model.properties.feeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.BreakIterator;
import java.util.Locale;

/**
 * Created by eng on 2016-11-02.
 */
public class EnglishSentenceFeeder implements SentenceFeeder {

  private final BufferedReader reader;
  private final BreakIterator bi;
  private String textBeingProcessed = "";

  public EnglishSentenceFeeder(final InputStream stream) {
    try {
      this.reader = new BufferedReader(new InputStreamReader(stream, "UTF8"));
      this.bi = BreakIterator.getSentenceInstance(Locale.ENGLISH);
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String nextSentence() throws IOException {
    int index = bi.next();
    // End of in memory buffer or end of text
    while (index == textBeingProcessed.length() || index == BreakIterator.DONE) {
      final String next = reader.readLine();
      // End of buffer, just add more stuff
      if(next != null) {
        textBeingProcessed += next + " ";
        bi.setText(textBeingProcessed);
        index = bi.next();
      } else {
        // File fully read and nothing in buffer
        if(index == BreakIterator.DONE) {
          return null;
        } else {
          // Successfully loaded more stuff. Start splitting.
          break;
        }
      }
    }
    final String textToReturn = textBeingProcessed.substring(0, index);
    textBeingProcessed = textBeingProcessed.substring(index);
    bi.setText(textBeingProcessed);
    return textToReturn;
  }
}
