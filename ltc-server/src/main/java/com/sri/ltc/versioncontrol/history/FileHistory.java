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
package com.sri.ltc.versioncontrol.history;

import com.sri.ltc.CommonUtils;
import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.CommitGraph;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.TrackedFile;
import com.sri.ltc.versioncontrol.VersionControlException;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author linda
 */
public abstract class FileHistory {

    private final static Logger LOGGER = Logger.getLogger(FileHistory.class.getName());
    final TrackedFile trackedFile;
    List<Commit> commitList;
    final CommitGraph commitGraph = new CommitGraph();
    final Set<Author> authors = new HashSet<Author>();

    protected FileHistory(TrackedFile file) throws IOException, ParseException {
        if (file == null)
            throw new IllegalArgumentException("Cannot create FileHistory with NULL as git file");
        this.trackedFile = file;
    }

    abstract List<Commit> updateCommits() throws ParseException, VersionControlException, IOException;
    abstract void transformGraph();
    abstract void transformList() throws IOException;

    /**
     * Update the commit graph from the tracked file and create a list of commits based on current settings.
     * Each commit is a 6-tuple of strings:
     * <ol>
     *     <li>Revision ID (e.g., SHA-1 for git repositories)</li>
     *     <li>Commit message</li>
     *     <li>Author name</li>
     *     <li>Author email (could be empty)</li>
     *     <li>Date and time of commit</li>
     *     <li>List of parent commits as space separated revision IDs</li>
     * </ol>
     *
     * @return List of commits in order of newest to oldest
     * @throws ParseException
     * @throws IOException
     * @throws VersionControlException
     */
    public final List<Object[]> update() throws ParseException, IOException, VersionControlException {
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
                    CommonUtils.serializeDate(commit.getDate()),
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

        // serialize commit graph by selecting the path with oldest commits for merges
        commitList = commitGraph.getPath(new Comparator<Commit>() {
            public int compare(Commit o1, Commit o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        LOGGER.fine("Obtained path from commit graph for \""+ trackedFile.getFile().getName()+"\" with "+commitList.size()+" commits.");

        // do any specific list transformations before reversing
        transformList();

        Collections.reverse(commitList); // start with oldest commit first
        return list;
    }

    public Set<Author> getAuthors() {
        return authors;
    }    
}
