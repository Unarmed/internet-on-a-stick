package se.carlengstrom.internetonastick.model;

import se.carlengstrom.internetonastick.job.Job;
import se.carlengstrom.internetonastick.model.properties.joiner.Joiner;
import se.carlengstrom.internetonastick.model.properties.splitter.SentenceSplitter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by eng
 */
public class Markov {
    private final Map<Long, HashMap<Long,Long>> parentsOf = new HashMap<>();
    private final Map<Long, HashMap<Long,Long>> childrenOf = new HashMap<>();
    private final Map<Long, Node> nodeById = new HashMap<>();
    private final Map<String, Set<Node>> nodesByWord = new HashMap<>();
    private long nodeCounter = 1;

    private final Node source = new Node(0, "SOURCE");
    private final Node sink = new Node(1, "SINK");

    private final SentenceSplitter splitter;
    private final int ancestorLevel;

    private final Joiner joiner;

    private int sentenceCounter = 0;

    public Markov(final SentenceSplitter splitter, int ancestorLevel, final Joiner joiner)
    {
        this.splitter = splitter;
        this.ancestorLevel = ancestorLevel;
        this.joiner = joiner;
        nodeById.put(0l, source);
        nodeById.put(1l, sink);
    }

    public void insertSentence(final String sentence, final Job job) {
        final Stream<String> words = splitter.split(sentence);

        final LinkedList<Node> ancestors = new LinkedList<>();
        Node parent = source;
        ancestors.add(parent);

        for(final String next : words.collect(Collectors.toList())) {
            // Look for a previous parse of '<parent> <next>'
            final Optional<Node> maybeMe = childrenOf(parent)
                .filter(node -> node.getWord().equals(next))
                .findFirst();
            // If present, no need to create this node
            final Node me = maybeMe.isPresent() ? maybeMe.get() : makeNewNode(next);

            // Idempotent
            addChild(parent, me);

            ancestors.addFirst(me);
            parent = me;

            while(ancestors.size() > ancestorLevel) {
                ancestors.removeLast();
            }

            // Has the combination "<parent> <next>" been parsed before?
            //If I am a new node some back-searching is needeed
            if(!maybeMe.isPresent()) {
                if (ancestors.size() == ancestorLevel) {
                    if (maybeFuseNodes(ancestors)) {
                        parent = ancestors.getFirst();
                        continue;
                    }
                }

                if (!nodesByWord.containsKey(next)) {
                    nodesByWord.put(next.intern(), new HashSet<>());
                }
                nodesByWord.get(next).add(me);
            }
        }

        addChild(parent, sink);
        sentenceCounter++;
        
        if(job != null && sentenceCounter % 1000 == 0) {
            job.setStatus("Read " + sentenceCounter + " sentences.");
            job.setSample(generateSentence());
        }
    }

    private boolean maybeFuseNodes(final LinkedList<Node> ancestors) {
        final LinkedList<Node> tmp = new LinkedList<>(ancestors);
        final Node start = tmp.removeFirst();
        if(!nodesByWord.containsKey(start.getWord())) {
            return false;
        }

        Set<Node> candidates = new HashSet<>(nodesByWord.get(start.getWord()));
        while(!tmp.isEmpty()) {
            final Node parent = tmp.removeFirst();
            candidates = candidates.stream()
                .flatMap(x -> parentsOf(x))
                .filter(x -> x.getWord().equals(parent.getWord()))
                .collect(Collectors.toSet());
        }

        // Don't fuse with self!
        candidates.removeAll(ancestors);
        // Nodes in candiates and oldestAncestor should share parents
        Node oldestAncestor = ancestors.getLast();
        for(final Node n : candidates) {
            fuseNodes(n, oldestAncestor);
            ancestors.removeLast();
            ancestors.addLast(n);
            oldestAncestor = n;
        }
        return !candidates.isEmpty();
    }

    private void fuseNodes(final Node keep, final Node discard) {
        for(final Long l : parentsOf.get(discard.getId()).keySet()) {
            final Node n = nodeById.get(l);
            addChild(n, keep);
        }

        final Set<Long> remove = parentsOf.remove(discard.getId()).keySet();
        remove.forEach(x -> childrenOf.get(x).remove(discard.getId()));

        if(childrenOf.get(discard.getId()) != null) {
            final Set<Long> children = childrenOf.get(discard.getId()).keySet();
            children.stream()
                .map(l -> nodeById.get(l))
                .forEach(child -> addChild(keep, child));
        }

        final Map<Long, Long> maybeChildren = childrenOf.remove(discard.getId());
        if(maybeChildren != null) {
            maybeChildren.keySet().forEach(x -> parentsOf.get(x).remove(discard.getId()));
        }
        // Remove discard from word lookups
        nodesByWord.get(discard.getWord()).remove(discard);
        // Remove discard entirely
        nodeById.remove(discard.getId());
    }

    private void addChild(final Node parent, final Node me) {
        if(!parentsOf.containsKey(me.getId())) {
            parentsOf.put(me.getId(), new HashMap<>());
        }
        parentsOf.get(me.getId()).compute(parent.getId(),(k,v) -> v == null ? 1 : v++);

        if(!childrenOf.containsKey(parent.getId())) {
            childrenOf.put(parent.getId(), new HashMap<>());
        }
        childrenOf.get(parent.getId()).compute(me.getId(),(k,v) -> v == null ? 1 : v+1);
    }

    private Node makeNewNode(String next) {
        long myId = ++nodeCounter;
        Node newNode = new Node(myId, next);
        nodeById.put(myId, newNode);
        return newNode;
    }

    private Stream<Node> parentsOf(Node child) {
        Map<Long, Long> data = parentsOf.containsKey(child.getId()) ? parentsOf.get(child.getId()) : Collections.emptyMap();
        return data.keySet().stream().map(id -> nodeById.get(id));
    }

    private Stream<Node> childrenOf(Node parent) {
        Map<Long, Long> data = childrenOf.containsKey(parent.getId()) ? childrenOf.get(parent.getId()) : Collections.emptyMap();
        return data.keySet().stream().map(id -> nodeById.get(id));
    }

    public String generateSentence() {
        //Don't generate sentences for empty chains.
        if(!childrenOf(source).findAny().isPresent()) {
            throw new IllegalStateException("Cannot generate sentences from empty markov chain");
        }

        Node current = source;
        final List<Node> sentence = new LinkedList<>();

        while(current != sink) {
            final Map<Long, Long> children = childrenOf.get(current.getId());
            final long totalSize = children.values().stream().reduce(0L, (l1,l2) -> l1 + l2);
            final long target = (long)(Math.random()*totalSize);
            long currentValue = 0L;
            for(final Map.Entry<Long,Long> e : children.entrySet()) {
                if(currentValue + e.getValue() >= target) {
                    current = nodeById.get(e.getKey());
                    break;
                } else {
                    currentValue += e.getValue();
                }
            }
            if(current != sink) {
                sentence.add(current);
            }
        }

        return sentence.stream().map(x -> x.getWord()).reduce(joiner::join).get();
    }

    //These methods expose internal state for testing. I don't particularly like that,
    //but that's what you get when you build a non-deterministic class
    public Stream<Node> getNodesForName(final String word) {
        return nodesByWord.get(word).stream();
    }

    public Stream<Node> getParentsOf(final Node n) {
        return parentsOf.get(n.getId()).keySet().stream().map(x -> nodeById.get(x));
    }

    public Stream<Node> getChildrenOf(final Node n) {
        return childrenOf.get(n.getId()).keySet().stream().map(x -> nodeById.get(x));
    }
}
