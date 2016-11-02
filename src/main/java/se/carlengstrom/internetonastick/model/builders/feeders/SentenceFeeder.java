package se.carlengstrom.internetonastick.model.builders.feeders;

import java.io.IOException;

/**
 * Created by eng on 2016-11-02.
 */
public interface SentenceFeeder {
  String nextSentence() throws IOException;
}
