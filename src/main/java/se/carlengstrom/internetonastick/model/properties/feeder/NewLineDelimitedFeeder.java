package se.carlengstrom.internetonastick.model.properties.feeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by eng on 2016-11-02.
 */
public class NewLineDelimitedFeeder implements SentenceFeeder {

  private final BufferedReader reader;

  public NewLineDelimitedFeeder(final InputStream stream) {
      reader = new BufferedReader(new InputStreamReader(stream));
  }

  @Override
  public String nextSentence() throws IOException {
      return reader.readLine();
  }
}
