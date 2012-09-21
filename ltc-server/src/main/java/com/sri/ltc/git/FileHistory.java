/**
 ************************ 80 columns *******************************************
 * FileHistory
 *
 * Created on Jul 24, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */

package com.sri.ltc.git;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.CommitGraph;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author linda
 */
public abstract class FileHistory {

    private final static Logger LOGGER = Logger.getLogger(FileHistory.class.getName());
    final TrackedFile gitFile;
    List<Commit> commitList;
    final CommitGraph commitGraph = new CommitGraph();
    final Set<Author> authors = new HashSet<Author>();

    public FileHistory(TrackedFile gitFile) throws IOException, ParseException {
        if (gitFile == null)
            throw new IllegalArgumentException("Cannot create FileHistory with NULL as git file");
        this.gitFile = gitFile;
    }

    abstract List<Commit> updateCommits() throws ParseException, IOException;
    abstract void transformGraph();
    abstract void transformList() throws IOException;

    public final List<Object[]> update() throws IOException, ParseException {
        List<Commit> commits = updateCommits();

        // translate git commits into graph structure:
        commitGraph.clear();
        authors.clear();
        List<Object[]> list = new ArrayList<Object[]>();

        // 1) add vertices and build up authors and return list
        for (Commit commit : commits) {
            commitGraph.addVertex(commit);

            // fill set of authors
            Author author = commit.getAuthor();
            authors.add(author);
            
            // build up return list
            StringBuilder parentsAsString = new StringBuilder();
            List<Commit> parents = commit.getParents();
            for (Commit parent : parents) {
                parentsAsString.append(parent.getId() + " ");
            }

            list.add(new Object[] {
                    commit.getId(),
                    commit.getMessage().trim(),
                    author.name,
                    author.email,
                    Commit.serializeDate(commit.getDate()),
                    parentsAsString.toString()
            });
        }

        // 2) build up graph structure
        for (Commit commit : commits) {
            List<Commit> parents = commit.getParents();
            if (parents != null && !parents.isEmpty()) {
                Commit c = commitGraph.getCommit(commit.getId());
                for (Commit parentCommit : parents) {
                    Commit parent = commitGraph.getCommit(parentCommit.getId());
                    if (parent != null)
                        commitGraph.addEdge(c, parent);
                }
            }
        }

        // do any specific graph transformations before flattening
        transformGraph();

        // serialize commit graph by selecting the path with newest commits for merges
        commitList = commitGraph.getPath(new Comparator<Commit>() {
            public int compare(Commit o1, Commit o2) {
                return o1.getDate().compareTo(o2.getDate()); // TODO: decide whether newest/oldest commit is the key here
            }
        });
        LOGGER.info("Obtained path from commit graph for \""+gitFile.getFile().getName()+"\" with "+commitList.size()+" commits.");

        // do any specific list transformations before reversing
        transformList();

        Collections.reverse(commitList); // start with oldest commit first
        return list;
    }

    public List<Commit> getLog() throws IOException {
        List<Commit> commits = gitFile.getCommits();
        LOGGER.info("Obtained full history for \""+gitFile.getFile().getName()+"\" with "+commits.size()+" commits.");
        return commits;
    }

    public Set<Author> getAuthors() {
        return authors;
    }    
}
