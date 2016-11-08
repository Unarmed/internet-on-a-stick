package se.carlengstrom.internetonastick.model.properties.feeder;

import java.io.IOException;

/**
 * Created by eng on 2016-11-02.
 */
public interface SentenceFeeder {
  String nextSentence() throws IOException;
}
