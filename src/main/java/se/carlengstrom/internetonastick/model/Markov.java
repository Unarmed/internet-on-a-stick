package se.carlengstrom.internetonastick.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.carlengstrom.internetonastick.job.Job;

/**
 * Created by eng
 */
public class Markov {

    private final int ANCESTOR_LEVEL = 3;

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
        final Stream<String> words = Stream.of(sentence.split(" "))
            .filter(x -> !x.isEmpty());

        final LinkedList<Node> ancestors = new LinkedList<>();
        Node parent = source;
        ancestors.add(parent);

        for(final String next : words.collect(Collectors.toList())) {
            // Look for a previous parse of '<parent> <next>'
            final Optional<Node> maybeMe = childrenOf(parent).filter(node -> node.getWord().equals(next)).findFirst();
            // If present, no need to create this node
            final Node me = maybeMe.isPresent() ? maybeMe.get() : makeNewNode(next);

            // Idempotent
            addChild(parent, me);
            ancestors.addFirst(me);
            // Has the combination "<parent> <next>" been parsed before?
            //If I am a new node some back-searching is needeed
            if(!maybeMe.isPresent()) {
                if(ancestors.size() == ANCESTOR_LEVEL ) {
                    mapAncestors(ancestors);
                }

                if(!nodesByWord.containsKey(next)) {
                    nodesByWord.put(next.intern(), new HashSet<>());
                }
                nodesByWord.get(next).add(me);
            }

            parent = me;

            if(ancestors.size() >= ANCESTOR_LEVEL) {
                ancestors.removeLast();
            }
        }

        addChild(parent, sink);
        sentenceCounter++;
        
        if(job != null && sentenceCounter % 1000 == 0) {
            job.setStatus("Read " + sentenceCounter + " sentences.");
            job.setSample(generateSentence());
        }
    }

    private void mapAncestors(final LinkedList<Node> ancestors) {
        final LinkedList<Node> tmp = new LinkedList<>(ancestors);
        final Node start = tmp.removeFirst();
        if(!nodesByWord.containsKey(start.getWord())) {
            return;
        }
        Set<Node> candidates = new HashSet<>(nodesByWord.get(start.getWord()));
        while(!tmp.isEmpty()) {
            final Node parent = tmp.removeFirst();
            candidates = candidates.stream()
                .flatMap(x -> parentsOf(x))
                .filter(x -> x.getWord().equals(parent.getWord()))
                .collect(Collectors.toSet());
        }
         // Nodes in candiates and oldestAncestor should share parents
        final Node oldestAncestor = ancestors.getLast();
        for(final Node n : candidates) {
            parentsOf(n).forEach(x -> addChild(x, oldestAncestor));
            parentsOf(oldestAncestor).forEach(x -> addChild(x, n));
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
        //Don't generate sentences for empty chains.
        if(!childrenOf(source).findAny().isPresent()) {
            throw new IllegalStateException("Cannot generate sentences from empty markov chain");
        }

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
