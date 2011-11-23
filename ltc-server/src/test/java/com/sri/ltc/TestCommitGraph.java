/**
 ************************ 80 columns *******************************************
 * TestCommitGraph
 *
 * Created on Sep 17, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc;

import com.sri.ltc.filter.Author;
import com.sri.ltc.git.Commit;
import com.sri.ltc.git.CommitGraph;
import junit.framework.Assert;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.Test;

import java.text.ParseException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

/**
 * @author linda
 */
public class TestCommitGraph {

    DirectedGraph<String, DefaultEdge> createDirectedGraph() {
        DirectedGraph<String, DefaultEdge> graph =
                new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        // vertices
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");

        // edges
        graph.addEdge("A","E");
        graph.addEdge("A","C");
        graph.addEdge("A","B");
        graph.addEdge("B","F");
        graph.addEdge("B","D");
        graph.addEdge("F","E");

        return graph;
    }

    @Test
    public void creation() {
        DirectedGraph<String,DefaultEdge> graph = createDirectedGraph();
        assertTrue(graph.vertexSet().size() == 6);
        assertTrue(graph.edgeSet().size() == 6);
    }

    @Test
    public void iteration() {
        DepthFirstIterator<String,DefaultEdge> iterator =
                new DepthFirstIterator<String,DefaultEdge>(createDirectedGraph(),"A");
        StringBuilder result = new StringBuilder();
        while (iterator.hasNext()) {
            result.append(iterator.next());
        }
        assertTrue("ABDFEC".equals(result.toString()));
    }

    CommitGraph createCommitGraph() throws ParseException {
        CommitGraph graph = new CommitGraph();

        // commit objects
        Commit[] commits = new Commit[7];
        commits[0] = new Commit("d3f904cd6ea27f9d8eae2191483f111631cd5129",
                "2010-07-23 20:27:04 +0200",
                "Roger Sherman", "sherman@usa.gov",
                "sixth version");
        commits[1] = new Commit("203e0ce8a57032612912c92219f228ce23b8f1de",
                "2010-07-23 20:26:35 +0200",
                "Roger Sherman", "sherman@usa.gov",
                "fifth version");
        commits[2] = new Commit("36eeab06e8a7d06a721cfa639702581b2ac7e688",
                "2010-07-23 20:12:42 +0200",
                "Thomas Jefferson", "jefferson@usa.gov",
                "fourth version");
        commits[3] = new Commit("fa2be391bbaa3f926518e5f0b55bde7613805d6d",
                "2010-07-23 20:11:18 +0200",
                "Benjamin Franklin", "franklin@usa.gov",
                "third version");
        commits[4] = new Commit("bac2f5155c502d5ee103b4f2ed2e0a520601dddf",
                "2010-07-23 20:09:51 +0200",
                "John Adams", "adams@usa.gov",
                "second version");
        commits[5] = new Commit("d6d1cf81740be22fba6f7cef1a33831017736015",
                "2010-07-23 20:08:39 +0200",
                "Thomas Jefferson", "jefferson@usa.gov",
                "first version");
        commits[6] = new Commit("xxxf5155c502d5ee103b4f2ed2e0a520601dddf",
                "2010-07-24 20:09:51 +0200",
                "John Adams", "adams@usa.gov",
                "last version");

        // vertices
        for (int i=0; i < commits.length; i++)
            graph.addVertex(commits[i]);

        // edges
        graph.addEdge(commits[0], commits[1]);
        graph.addEdge(commits[1], commits[4]);
        graph.addEdge(commits[4], commits[3]);
        graph.addEdge(commits[0], commits[2]);
        graph.addEdge(commits[2], commits[5]);
        graph.addEdge(commits[5], commits[4]);
        graph.addEdge(commits[0], commits[6]);
        graph.addEdge(commits[6], commits[3]);

        return graph;
    }

    @Test
    public void commitGraphCreation() throws ParseException {
        CommitGraph graph = createCommitGraph();
        System.out.println("Original: "+graph.toString());
        assertTrue(graph.vertexSet().size() == 7);
    }

    @Test
    public void reduceByAuthors() throws ParseException {
        Set<Author> authors = new HashSet<Author>();
        authors.add(Author.parse("Thomas Jefferson <jefferson@usa.gov>"));
        authors.add(Author.parse("Roger Sherman <sherman@usa.gov>"));
        authors.add(Author.parse("Benjamin Franklin <franklin@usa.gov>"));

        CommitGraph graph = createCommitGraph();
        graph.reduceByAuthors(authors);
        System.out.println("Reduced: "+graph.toString());
        assertTrue(graph.vertexSet().size() == 5);

        List<Commit> path = graph.getPath(new Comparator<Commit> () {
            @Override
            public int compare(Commit o1, Commit o2) {
                return o1.date.compareTo(o2.date);
            }
        });
        System.out.println("Path: "+path);
        assertTrue(path.size() == 2);
    }
}
