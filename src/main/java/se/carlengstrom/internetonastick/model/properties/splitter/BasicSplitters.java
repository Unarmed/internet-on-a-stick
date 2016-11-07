package se.carlengstrom.internetonastick.model.properties.splitter;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by eng on 2016-11-03.
 */
public class BasicSplitters {

  public static SentenceSplitter SPACE_SPLITTER = x -> Stream.of(x.split(" ")).filter(y -> !y.isEmpty());
  public static SentenceSplitter CHAR_SPLITTER = x -> x.chars().mapToObj(y -> ((char)y)+"");

  public static final Map<String, SentenceSplitter> SPLITTERS =
          ImmutableMap.of("space", SPACE_SPLITTER, "char", CHAR_SPLITTER);
}
