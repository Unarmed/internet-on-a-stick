package se.carlengstrom.internetonastick.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * These tests abuse knowledge of the inner workings of Markov. I don't like it, but since the output of Markov is
 * non-deterministic then it's kind of hard to test otherwise.
 * Created by eng on 2/13/16.
 */
public class MarkovTest {

    @Test
    public void TestAddSingleWordAssignsAtTwo() {
        Markov m = new Markov();
        m.insertSentence("Test");
        Node n = m.getAllNodes().get(2l); //Abusing the fact that I know the id of 'Test'
        assertEquals("Test", n.getWord());
    }

    @Test
    public void TestAddSingleWordGetsSourceAsParent() {
        Markov m = new Markov();
        m.insertSentence("Test");
        assertTrue(m.getParentsOf().get(2l).contains(0l));
    }

    @Test
    public void TestAddSingleWordGetsSinkAsChild() {
        Markov m = new Markov();
        m.insertSentence("Test");
        assertTrue(m.getChildrenOf().get(2l).contains(1l));
    }

    @Test
    public void TestCrossingCorrectNumberOfResults() {
        Markov m = new Markov();
        m.insertSentence("a b c");
        m.insertSentence("a b d");
        long bId = 3; //Abusing that i know that the id of 'b' is 3

        assertEquals(2, m.getChildrenOf().get(bId).size());
    }

    @Test
    public void TestLateCrossingCorrectNumberOfResults() {
        Markov m = new Markov();
        m.insertSentence("1 a b c");
        m.insertSentence("2 a b d");
        long bId = 4; //Abusing that i know that the id of 'b' is 4

        assertEquals(2, m.getChildrenOf().get(bId).size());
    }
}