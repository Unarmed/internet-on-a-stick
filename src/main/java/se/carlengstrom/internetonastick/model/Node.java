package se.carlengstrom.internetonastick.model;

/**
 * Created by eng on 2/13/16.
 */
public class Node {

    private final long id;
    private final String word;

    public Node(long id, String word) {
        this.id = id;
        this.word = word.intern();
    }

    public long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }
}
