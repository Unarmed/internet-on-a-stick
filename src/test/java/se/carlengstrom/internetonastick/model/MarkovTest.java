package se.carlengstrom.internetonastick.model;

import org.junit.Test;
import se.carlengstrom.internetonastick.model.properties.joiner.BasicJoiners;
import se.carlengstrom.internetonastick.model.properties.splitter.BasicSplitters;

import static org.junit.Assert.*;

import java.util.stream.Stream;

/**
 * These tests abuse knowledge of the inner workings of Markov. I don't like it, but since the output of Markov is
 * non-deterministic then it's kind of hard to test otherwise.
 * Created by eng on 2/13/16.
 */
public class MarkovTest {

  @Test
  public void testSourceParenting() {
    final Markov m = new Markov(BasicSplitters.CHAR_SPLITTER,1, BasicJoiners.NOTHING_JOINER);
    m.insertSentence("a", null);
    final Stream<Node> as = m.getNodesForName("a");
    assertTrue(as.allMatch(x -> m.getParentsOf(x).anyMatch( y -> y.getId() == 0)));
  }

  @Test
  public void testSinkParenting() {
    final Markov m = new Markov(BasicSplitters.CHAR_SPLITTER,1, BasicJoiners.NOTHING_JOINER);
    m.insertSentence("a", null);
    final Stream<Node> as = m.getNodesForName("a");
    assertTrue(as.allMatch(x -> m.getChildrenOf(x).anyMatch( y -> y.getId() == 1)));
  }

  @Test
  public void testSimpleCrossover() {
    final Markov m = new Markov(BasicSplitters.CHAR_SPLITTER,1, BasicJoiners.NOTHING_JOINER);
    m.insertSentence("ab", null);
    m.insertSentence("cb", null);
    final Stream<Node> bs = m.getNodesForName("b");
    assertTrue(bs.allMatch(x -> m.getParentsOf(x).count() == 2));
  }

  @Test
  public void testLongerCrossover() {
    final Markov m = new Markov(BasicSplitters.CHAR_SPLITTER,3, BasicJoiners.NOTHING_JOINER);
    m.insertSentence("abxy", null);
    m.insertSentence("cbxy", null);
    final Stream<Node> bs = m.getNodesForName("b");
    assertTrue(bs.allMatch(x -> m.getParentsOf(x).count() == 2));

    final Stream<Node> xs = m.getNodesForName("x");
    assertTrue(xs.allMatch(x -> m.getParentsOf(x).count() == 1));

    final Stream<Node> ys = m.getNodesForName("y");
    assertTrue(ys.allMatch(x -> m.getParentsOf(x).count() == 1));
  }

  @Test
  public void testCrossoverMergesNodes() {
    final Markov m = new Markov(BasicSplitters.CHAR_SPLITTER,1, BasicJoiners.NOTHING_JOINER);
    m.insertSentence("ab", null);
    m.insertSentence("cb", null);
    assertEquals(1, m.getNodesForName("b").count());
  }

  @Test
  public void testDoubleFuse() {
    final Markov m = new Markov(BasicSplitters.CHAR_SPLITTER,2, BasicJoiners.NOTHING_JOINER);
    m.insertSentence("aHe", null);
    m.insertSentence("bHe", null);
    m.insertSentence("cHe", null);
    assertEquals(1, m.getNodesForName("H").count());
  }

  @Test
  public void testSentence() {
    final String sentence = "Hello there, this is a very nice test sentence that contains a lot of duplicate letters and other interesting things";
    final Markov m = new Markov(BasicSplitters.CHAR_SPLITTER,1, BasicJoiners.NOTHING_JOINER);
    m.insertSentence(sentence, null);
  }
}