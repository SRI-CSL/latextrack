package com.sri.ltc.versioncontrol;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.git.GitRepository;
import com.sri.ltc.versioncontrol.svn.SVNRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class RepositoryFactory {
    public static Repository fromPath(File path) throws IOException {
        // TODO: look at path and determine what kind of repository this is
        // or try to guess it. For now, we're assuming this is a Git Repo


        // TODO: remove this temp test code
        try {
            SVNRepository svn = new SVNRepository(path);
            TrackedFile tf = svn.getFile(path);
            tf.getStatus();
            List<Commit> commits = tf.getCommits();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new GitRepository(path);
    }
}
