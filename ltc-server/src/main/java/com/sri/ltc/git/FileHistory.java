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
import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.options.GitLogOptions;
import edu.nyu.cs.javagit.api.responses.GitLogResponse;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author linda
 */
public abstract class FileHistory {

    private final static Logger LOGGER = Logger.getLogger(FileHistory.class.getName());
    final GitFile gitFile;
    List<Commit> commitList;
    final GitLogOptions options = new GitLogOptions();
    {
        options.setOptFormatDate("iso8601");
        options.setOptOrderingTopological(true);
        options.setOptGraph(true);
        options.setOptFormat("commit %H%nAuthor: %an <%ae>%nDate: %ad%nParents: %P%n%s%n");
    }
    final CommitGraph commitGraph = new CommitGraph();
    final Set<Author> authors = new HashSet<Author>();

    public FileHistory(GitFile gitFile) throws IOException, ParseException, JavaGitException {
        if (gitFile == null)
            throw new IllegalArgumentException("Cannot create FileHistory with NULL as git file");
        this.gitFile = gitFile;
    }

    abstract List<GitLogResponse.Commit> updateCommits() throws ParseException, IOException, JavaGitException;
    abstract void transformGraph();
    abstract void transformList() throws IOException, JavaGitException;

    public final List<Object[]> update() throws IOException, JavaGitException, ParseException {
        List<GitLogResponse.Commit> commits = updateCommits();

        // translate git commits into graph structure:
        commitGraph.clear();
        authors.clear();
        List<Object[]> list = new ArrayList<Object[]>();

        // 1) add vertices and build up authors and return list
        for (GitLogResponse.Commit commit : commits) {
            commitGraph.addVertex(Commit.translate(commit));
            // fill set of authors
            Author author = Author.parse(commit.getAuthor());
            authors.add(author);
            // build up return list
            StringBuilder parentsAsString = new StringBuilder();
            for (String parent : commit.getParents())
                parentsAsString.append(parent + " ");
            list.add(new Object[] {
                    commit.getSha(),
                    commit.getMessage().trim(),
                    author.name,
                    author.email,
                    commit.getDate(),
                    parentsAsString.toString()
            });
        }

        // 2) build up graph structure
        for (GitLogResponse.Commit commit : commits) {
            List<String> parents = commit.getParents();
            if (parents != null && !parents.isEmpty()) {
                Commit c = commitGraph.getCommit(commit.getSha());
                for (String sha : parents) {
                    Commit parent = commitGraph.getCommit(sha);
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
                return o1.date.compareTo(o2.date); // TODO: decide whether newest/oldest commit is the key here
            }
        });
        LOGGER.info("Obtained path from commit graph for \""+gitFile.getFile().getName()+"\" with "+commitList.size()+" commits.");

        // do any specific list transformations before reversing
        transformList();

        Collections.reverse(commitList); // start with oldest commit first
        return list;
    }

    List<GitLogResponse.Commit> getLog(GitLogOptions options, String history)
            throws IOException, JavaGitException {
        List<GitLogResponse.Commit> commits = gitFile.getLog(options);
        LOGGER.info("Obtained "+history+" history for \""+gitFile.getFile().getName()+"\" with "+commits.size()+" commits.");
        return commits;
    }

    public Set<Author> getAuthors() {
        return authors;
    }    
}
