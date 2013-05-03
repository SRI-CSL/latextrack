/*
 * #%L
 * LaTeX Track Changes (LTC) allows collaborators on a version-controlled LaTeX writing project to view and query changes in the .tex documents.
 * %%
 * Copyright (C) 2009 - 2012 SRI International
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.sri.ltc;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.CommitGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * @author linda
 */
public final class TestCommitGraph {

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
        Commit[] commits = new TestCommit[7];
        commits[0] = new TestCommit("d3f904cd6ea27f9d8eae2191483f111631cd5129", 
                CommonUtils.deSerializeDate("2010-07-23 20:27:04 +0200"),
                "Roger Sherman", "sherman@usa.gov",
                "sixth version");
        commits[1] = new TestCommit("203e0ce8a57032612912c92219f228ce23b8f1de",
                CommonUtils.deSerializeDate("2010-07-23 20:26:35 +0200"),
                "Roger Sherman", "sherman@usa.gov",
                "fifth version");
        commits[2] = new TestCommit("36eeab06e8a7d06a721cfa639702581b2ac7e688",
                CommonUtils.deSerializeDate("2010-07-23 20:12:42 +0200"),
                "Thomas Jefferson", "jefferson@usa.gov",
                "fourth version");
        commits[3] = new TestCommit("fa2be391bbaa3f926518e5f0b55bde7613805d6d",
                CommonUtils.deSerializeDate("2010-07-23 20:11:18 +0200"),
                "Benjamin Franklin", "franklin@usa.gov",
                "third version");
        commits[4] = new TestCommit("bac2f5155c502d5ee103b4f2ed2e0a520601dddf",
                CommonUtils.deSerializeDate("2010-07-23 20:09:51 +0200"),
                "John Adams", "adams@usa.gov",
                "second version");
        commits[5] = new TestCommit("d6d1cf81740be22fba6f7cef1a33831017736015",
                CommonUtils.deSerializeDate("2010-07-23 20:08:39 +0200"),
                "Thomas Jefferson", "jefferson@usa.gov",
                "first version");
        commits[6] = new TestCommit("xxxf5155c502d5ee103b4f2ed2e0a520601dddf",
                CommonUtils.deSerializeDate("2010-07-24 20:09:51 +0200"),
                "John Adams", "adams@usa.gov",
                "last version");

        // vertices
        for (Commit commit : commits) {
            graph.addVertex(commit);
        }

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
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        System.out.println("Path: "+path);
        assertTrue(path.size() == 2);
    }
}
