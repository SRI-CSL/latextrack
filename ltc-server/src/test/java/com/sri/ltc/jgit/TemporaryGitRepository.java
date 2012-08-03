package com.sri.ltc.jgit;

import com.sri.ltc.versioncontrol.Repository;
import com.sri.ltc.versioncontrol.RepositoryFactory;
import org.junit.rules.TemporaryFolder;

import java.net.URI;

public class TemporaryGitRepository extends TemporaryFolder {
    private Repository repository = null;

    public Repository getRepository() {
        return repository;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        assert this.getRoot().exists();
        Repository repository = RepositoryFactory.fromURI(new URI(this.getRoot().toString()));
        repository.create();
//        System.out.println("Creating a git repo at" + this.getRoot().toString());
//
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        repository = builder
//                .setWorkTree(this.getRoot())
//                .readEnvironment()
//                .build();
//
//        repository.create();
    }
}
