package se.carlengstrom.internetonastick.model.properties.joiner;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by eng on 2016-11-03.
 */
public class BasicJoiners {

  public static final Joiner NOTHING_JOINER = (a,b) -> a+b;
  public static final Joiner SPACE_JOINER = (a,b) -> a + " " + b;

  public static final Map<String, Joiner> JOINERS = ImmutableMap.of("nothing", NOTHING_JOINER, "space", SPACE_JOINER);
}
