package se.carlengstrom.internetonastick.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
