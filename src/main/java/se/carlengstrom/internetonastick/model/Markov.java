package se.carlengstrom.internetonastick.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.carlengstrom.internetonastick.job.Job;

/**
 * Created by eng
 */
public class Markov {

    private final int ANCESTOR_LEVEL = 2;

    private final Map<Long, Set<Long>> parentsOf = new HashMap<>();
    private final Map<Long, Set<Long>> childrenOf = new HashMap<>();
    private final Map<Long, Node> nodeById = new HashMap<>();
    private final Map<String, Set<Node>> nodesByWord = new HashMap<>();

    private long nodeCounter = 1;
    private final Node source = new Node(0, "SOURCE");
    private final Node sink = new Node(1, "SINK");

    private int sentenceCounter = 0;

    public Markov()
    {
        nodeById.put(0l, source);
        nodeById.put(1l, sink);
    }
    
    public void insertSentence(String sentence) {
        insertSentence(sentence, null);
    }

    public void insertSentence(String sentence, Job job) {
        String[] words = sentence.split(" ");

        LinkedList<Node> ancestors = new LinkedList<>();
        Node parent = source;
        ancestors.add(parent);

        for(int i = 0 ; i < words.length ; i++) {
            String next = words[i];
            Optional<Node> maybeMe = childrenOf(parent).filter(node -> node.getWord().equals(next)).findFirst();
            Node me = maybeMe.isPresent() ? maybeMe.get() : makeNewNode(next);

            addChild(parent, me);

            //If I am a new node some back-searching is needeed
            if(!maybeMe.isPresent()) {
                connectAncestorsOf(me, ancestors, words, i);

                if(!nodesByWord.containsKey(next)) {
                    nodesByWord.put(next.intern(), new HashSet<>());
                }
                nodesByWord.get(next).add(me);
            }

            parent = me;
            ancestors.add(me);
            if(ancestors.size() > ANCESTOR_LEVEL) {
                ancestors.remove();
            }
        }

        addChild(parent, sink);
        sentenceCounter++;
        
        if(job != null && sentenceCounter % 1000 == 0) {
            job.statusString = "Read " + sentenceCounter + " sentences.\n" + 
                    "Sample: " + generateSentence();
        }
    }

    private void addChild(Node parent, Node me) {
        if(!parentsOf.containsKey(me.getId())) {
            parentsOf.put(me.getId(), new HashSet<>());
        }
        parentsOf.get(me.getId()).add(parent.getId());

        if(!childrenOf.containsKey(parent.getId())) {
            childrenOf.put(parent.getId(), new HashSet<>());
        }
        childrenOf.get(parent.getId()).add(me.getId());
    }

    private void connectAncestorsOf(Node me, LinkedList<Node> ancestors, String[] data, int indexOfMe) {
        if(indexOfMe < ANCESTOR_LEVEL) { return; }

        ArrayList<Node> ancestry = new ArrayList<>(ancestors);
        Collections.reverse(ancestry);
        Stream<Node> nodeStream = nodesByWord.get(data[indexOfMe-1]).stream();
        nodeStream.filter(n -> hasCorrectLineage(n, ancestry, ANCESTOR_LEVEL))
                .forEach(node -> addChild(node, me));
    }

    private boolean hasCorrectLineage(Node n, ArrayList<Node> ancestry, int ancestor_level) {
        Stream<Node> validNodes = Stream.of(n);

        for(int i = 0 ; i < ancestor_level ; i++) {
            final int lol = i;
            validNodes = validNodes.filter(node -> node.getWord().equals(ancestry.get(lol).getWord()))
                    .map(node -> parentsOf(node)).flatMap(a -> a);
        }
        return validNodes.findAny().isPresent();
    }

    private Node makeNewNode(String next) {
        long myId = ++nodeCounter;
        Node newNode = new Node(myId, next);
        nodeById.put(myId, newNode);
        return newNode;
    }

    private Stream<Node> parentsOf(Node child) {
        Set<Long> data = parentsOf.containsKey(child.getId()) ? parentsOf.get(child.getId()) : Collections.emptySet();
        return data.stream().map(id -> nodeById.get(id));
    }

    private Stream<Node> childrenOf(Node parent) {
        Set<Long> data = childrenOf.containsKey(parent.getId()) ? childrenOf.get(parent.getId()) : Collections.emptySet();
        return data.stream().map(id -> nodeById.get(id));
    }

    public String generateSentence() {
        Node current = source;
        List<Node> sentence = new LinkedList<>();        

        while(current != sink) {
            List<Node> children = childrenOf(current).collect(Collectors.toList());
            current = children.get((int)(Math.random()*children.size()));
            if(current != sink) {
                sentence.add(current);
            }
        }
        return sentence.stream().map(x -> x.getWord()).reduce( (x,y) -> x + " " + y  ).get();
    }

    //These methods expose internal state for testing. I don't particularly like that,
    //but that's what you get when you build a non-deterministic class

    public Map<Long, Node> getAllNodes() { return nodeById; }

    public Map<Long, Set<Long>> getParentsOf() { return parentsOf; }

    public Map<Long, Set<Long>> getChildrenOf() { return childrenOf; }
}
