/**
 ************************ 80 columns *******************************************
 * CommitGraph
 *
 * Created on Sep 15, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import com.sri.ltc.filter.Author;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;

/**
 * @author linda
 */
public final class CommitGraph extends SimpleDirectedGraph<Commit,DefaultEdge> {

    private static final long serialVersionUID = 3699166530479808547L;

    private final Map<String,Commit> verticesBySHA = new HashMap<String,Commit>();
    private final Set<Commit> heads = new HashSet<Commit>();

    public CommitGraph() {
        super(DefaultEdge.class);
    }

    public void clear() {
        removeAllVertices(new HashSet<Commit>(vertexSet()));
        heads.clear();
    }

    @Override
    public boolean addVertex(Commit commit) {
        if (heads.isEmpty())
            heads.add(commit);
        verticesBySHA.put(commit.sha1, commit);
        return super.addVertex(commit);
    }

    @Override
    public boolean removeVertex(Commit commit) {
        verticesBySHA.remove(commit.sha1);
        return super.removeVertex(commit);
    }

    public Commit getCommit(String sha) {
        return verticesBySHA.get(sha);
    }

    /**
     * Transform commit graph into a connected graph that only contains vertices
     * with given authors.  Edges are drawn according to reachability depth-first.
     *
     * @param limitingAuthors set of authors to be retained in graph.  If null or empty
     *   the new graph will be equal to old one (no transformation done).
     */
    public void reduceByAuthors(Set<Author> limitingAuthors) {
        if (heads.isEmpty() || limitingAuthors == null || limitingAuthors.isEmpty())
            return;

        // make a deep copy of the current graph for traversal
        CommitGraph oldGraph = new CommitGraph();
        for (Commit c : vertexSet())
            oldGraph.addVertex(c);
        for (DefaultEdge e : edgeSet())
            oldGraph.addEdge(getEdgeSource(e), getEdgeTarget(e));

        // clear this graph
        removeAllVertices(new HashSet<Commit>(vertexSet()));

        // copy set of vertices and calculate intersection
        for (Commit vertex : oldGraph.vertexSet())
            if (limitingAuthors.contains(vertex.author))
                addVertex(vertex);

        // traverse graph depth-first to obtain edges and new heads
        Set<Commit> newHeads = new HashSet<Commit>();
        Set<Commit> alreadySeen = new HashSet<Commit>();
        for (Commit head : heads)
            traverseToReduceByAuthors(head, null, newHeads, alreadySeen, oldGraph);
        heads.clear();
        heads.addAll(newHeads);
    }

    private Set<Commit> traverseToReduceByAuthors(Commit vertex,
                                                  Commit lastVertex,
                                                  Set<Commit> newHeads,
                                                  Set<Commit> alreadySeen,
                                                  CommitGraph oldGraph) {
        // process vertex: figure out new edge or heads
        if (containsVertex(vertex)) {
            if (lastVertex == null)
                newHeads.add(vertex); // found new head
            else {
                newHeads.remove(vertex); // found different path to this node
                addEdge(lastVertex, vertex); // TODO: only add edge if not in connectivity super-graph
            }
            lastVertex = vertex;
        }

        Set<Commit> newVertices = new HashSet<Commit>();
        // traverse deeper if not already seen:
        if (!alreadySeen.contains(vertex)) {
            if (containsVertex(vertex))
                alreadySeen.add(vertex); // only stop traversing if vertex is in new graph
            for (DefaultEdge edge : oldGraph.outgoingEdgesOf(vertex)) {
                newVertices.clear();
                newVertices.addAll(traverseToReduceByAuthors(getEdgeTarget(edge), lastVertex, newHeads, alreadySeen, oldGraph));
                // TODO: add new edges to connectivity super-graph
            }
        }

        // initialize set with current vertex
        if (containsVertex(vertex))
            newVertices.add(vertex);
        return newVertices;
    }

    /**
     * Obtain a path from the head of the commit graph to one end
     * using the given comparator to determine through which parent
     * of each commit to traverse.  If a commit has more than one
     * parent the smallest (based on the comparator) is used in the path.
     * If a commit doesn't have any parents, the end of the path is reached.
     *
     * @param comparator used to compare multiple parents of a commit and
     *   select the smallest one as the next step in the path
     * @return a path of commits from the head to an end or
     *   an empty path if the graph is empty
     */
    public List<Commit> getPath(Comparator<Commit> comparator) {
        if (heads.isEmpty())
            return Collections.emptyList();
        List<Commit> path = new ArrayList<Commit>();

        SortedSet<Commit> sortedHeads = new TreeSet<Commit>(comparator);
        sortedHeads.addAll(heads);
        Commit commit = sortedHeads.first(); // start with first head by given comparator

        SortedSet<Commit> parents = new TreeSet<Commit>(comparator);
        while (true) {
            path.add(commit);
            parents.clear();
            for (DefaultEdge edge : outgoingEdgesOf(commit))
                parents.add(getEdgeTarget(edge));
            if (parents.isEmpty())
                break;
            commit = parents.first(); // traverse through first (= smallest) vertex element
        }
        return path;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Graph with heads: "+heads+"\n");
        builder.append("  "+vertexSet()+"\n");
        builder.append("  [");
        String sha;
        for (DefaultEdge e : edgeSet()) {
            builder.append("(");
            sha = getEdgeSource(e).sha1;
            builder.append(sha.substring(0,Math.min(6,sha.length())));
            builder.append(" -> ");
            sha = getEdgeTarget(e).sha1;
            builder.append(sha.substring(0,Math.min(6,sha.length())));
            builder.append(") ");
        }
        builder.append("]");
        return builder.toString();
    }
}
