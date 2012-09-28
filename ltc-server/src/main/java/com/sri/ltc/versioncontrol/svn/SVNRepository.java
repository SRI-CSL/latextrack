package com.sri.ltc.versioncontrol.svn;

import com.sri.ltc.filter.Author;
import com.sri.ltc.versioncontrol.Commit;
import com.sri.ltc.versioncontrol.Remotes;
import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.TrackedFile;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SVNRepository implements Repository {
    private SVNClientManager clientManager = null;

    public SVNRepository(File localPath) throws Exception {
        //ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager("sbreidbach", "");

        clientManager = SVNClientManager.newInstance();

//        SVNRepositoryFactoryImpl.setup();
//        FSRepositoryFactory.setup();
//        //SVNURL url = SVNURL.parseURIEncoded("https://cmit.sri.com/svn/TextSummarization/trunk/summly");

//        SVNURL url = SVNURL.parseURIEncoded("file:///Users/skip/Devel/TextSummarization/trunk/summly");
//        repository = SVNRepositoryFactory.create(url, null);
//        repository.setAuthenticationManager(authManager);
//
//        SVNURL a = repository.getRepositoryRoot(true);
//        String b = repository.getRepositoryUUID(true);
//        long c = repository.getLatestRevision();
//
//        SVNNodeKind nodeKind = repository.checkPath( "" ,  -1 );
//        if ( nodeKind == SVNNodeKind.NONE ) {
//            System.err.println( "There is no entry at '" + url + "'." );
//        } else if ( nodeKind == SVNNodeKind.FILE ) {
//            System.err.println( "The entry at '" + url + "' is a file while a directory was expected." );
//        }
    }

    public SVNClientManager getClientManager() {
        return clientManager;
    }

    @Override
    public void addFile(File file) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Commit commit(String message) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TrackedFile getFile(File file) throws IOException {
        return new SVNTrackedFile(this, file);
    }

    @Override
    public Remotes getRemotes() {
        return new SVNRemotes(this);
    }

    @Override
    public Author getSelf() {
        // TODO:
        return new Author("<default>", null, null);
    }

    @Override
    public void setSelf(Author author) {
        // TODO:
    }

    @Override
    public void resetSelf() {
        // TODO:
    }
}