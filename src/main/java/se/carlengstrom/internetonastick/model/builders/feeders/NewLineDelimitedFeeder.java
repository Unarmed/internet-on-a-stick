package se.carlengstrom.internetonastick.model.builders.feeders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by eng on 2016-11-02.
 */
public class NewLineDelimitedFeeder implements SentenceFeeder {

  private final BufferedReader reader;

  public NewLineDelimitedFeeder(final InputStream stream) throws UnsupportedEncodingException {
      reader = new BufferedReader(new InputStreamReader(stream, "UTF8"));
  }

  @Override
  public String nextSentence() throws IOException {
      return reader.readLine();
  }
}
