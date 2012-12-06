package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.versioncontrol.Remote;
import com.sri.ltc.versioncontrol.Remotes;
import org.tmatesoft.svn.core.SVNException;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class SVNRemotes extends Remotes<SVNRepository> {
    private final static Logger LOGGER = Logger.getLogger(SVNRemotes.class.getName());

    public SVNRemotes(SVNRepository repository) {
        super(repository);
    }

    @Override
    public Set<Remote> get() {
        Set<Remote> remotes = new HashSet<Remote>();

        try {
            String url = getRepository().getURL();
            remotes.add(new Remote("default", url, false));
        }
        catch (SVNException e) {
            LOGGER.warning("SVNRemotes could not get url for repository");
        }

        return remotes;
    }

    @Override
    public int addRemote(String name, String url) {
        // TODO
        return 0;
    }

    @Override
    public int removeRemote(String name) {
        // TODO
        return 0;
    }

    @Override
    public void pull(String name) throws Exception {
        // TODO
    }

    @Override
    public void push(String name) throws Exception {
        // TODO
    }
}
