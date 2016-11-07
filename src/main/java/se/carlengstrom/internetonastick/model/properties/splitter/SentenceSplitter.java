package se.carlengstrom.internetonastick.model.properties.splitter;

import java.util.stream.Stream;

/**
 * Created by eng on 2016-11-02.
 */
public interface SentenceSplitter {
  Stream<String> split(String str);
}
