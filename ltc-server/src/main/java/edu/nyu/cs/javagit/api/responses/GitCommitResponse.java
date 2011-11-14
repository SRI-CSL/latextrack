/*
 * ====================================================================
 * Copyright (c) 2008 JavaGit Project.  All rights reserved.
 *
 * This software is licensed using the GNU LGPL v2.1 license.  A copy
 * of the license is included with the distribution of this source
 * code in the LICENSE.txt file.  The text of the license can also
 * be obtained at:
 *
 *   http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the JavaGit project, see:
 *
 *   http://www.javagit.com
 * ====================================================================
 */
package edu.nyu.cs.javagit.api.responses;

import edu.nyu.cs.javagit.api.Ref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A response data object for the git-commit command.
 */
public final class GitCommitResponse extends AbstractResponse {

    // The short hash name for the commit.
    private Ref shortSHA1 = null;

    // The short comment for the commit.
    private String shortComment = "";

    // Indicates how many files have changed in a commit.
    private int filesChanged = 0;

    // Indicates how many lines were inserted in a commit.
    private int linesInserted = 0;

    // Indicates how many lines were deleted in a commit.
    private int linesDeleted = 0;

    /**
     * The list of the files added to the repository in this commit. The file name is the relative
     * path to the file from the root of the repository tree.
     */
    private List<AddedOrDeletedFile> addedFiles = new ArrayList<AddedOrDeletedFile>();

    /**
     * A list of new files that were copied from existing files already tracked in the repository. The
     * file names are relative paths to the files from the root of the repository tree.
     */
    private List<CopiedOrMovedFile> copiedFiles = new ArrayList<CopiedOrMovedFile>();

    /**
     * A list of the files deleted form the repository in this commit. The file name is the relative
     * path to the file from the root of the repository tree.
     */
    private List<AddedOrDeletedFile> deletedFiles = new ArrayList<AddedOrDeletedFile>();

    /**
     * A list of files that were moved/renamed in this commit. The file name is the relative path to
     * the file from the root of the repository tree.
     */
    private List<CopiedOrMovedFile> renamedFiles = new ArrayList<CopiedOrMovedFile>();

    private StringBuffer comment = new StringBuffer(); // collect lines starting with #

    public void appendComment(String line) {
        comment.append(line);
    }

    public void setCommit(String shortHashName, String shortComment) {
        this.shortSHA1 = Ref.createSha1Ref(shortHashName);
        this.shortComment = shortComment;
    }

    public String getShortComment() {
        return shortComment;
    }

    public Ref getShortSHA1() {
        return shortSHA1;
    }

    /**
     * Add the information about a newly added file in the repository for a given commit.
     *
     * @param pathToFile The path to the file from the root of the repository.
     * @param mode       The mode of the file.
     * @return False if the <code>pathToFile</code> is null or length zero. True otherwise.
     */
    public boolean addAddedFile(File pathToFile, String mode) {
        if (null == pathToFile) {
            return false;
        }

        return addedFiles.add(new AddedOrDeletedFile(pathToFile, mode));
    }

    /**
     * Add the information about a newly copied file in the repository for a given commit.
     *
     * @param sourceFilePath      The path to the source file.
     * @param destinationFilePath The path to the destination file.
     * @param percentage          The percentage.
     * @return False if <code>sourceFilePath</code> or <code>destinationFilePath</code> is null or
     *         length zero. True otherwise.
     */
    public boolean addCopiedFile(File sourceFilePath, File destinationFilePath, int percentage) {
        if (null == sourceFilePath || null == destinationFilePath) {
            return false;
        }
        return copiedFiles.add(new CopiedOrMovedFile(sourceFilePath, destinationFilePath, percentage));
    }

    /**
     * Add the information about a file deleted from the repository for a given commit.
     *
     * @param pathToFile The path to the file from the root of the repository.
     * @param mode       The mode of the file.
     * @return False if the <code>pathToFile</code> is null or length zero. True otherwise.
     */
    public boolean addDeletedFile(File pathToFile, String mode) {
        if (null == pathToFile) {
            return false;
        }

        return deletedFiles.add(new AddedOrDeletedFile(pathToFile, mode));
    }

    /**
     * Add the information about a moved/renamed file in the repository for a given commit.
     *
     * @param sourceFilePath      The path to the source file.
     * @param destinationFilePath The path to the destination file.
     * @param percentage          The percentage.
     * @return False if <code>sourceFilePath</code> or <code>destinationFilePath</code> is null or
     *         length zero. True otherwise.
     */
    public boolean addRenamedFile(File sourceFilePath, File destinationFilePath, int percentage) {
        if (null == sourceFilePath || null == destinationFilePath) {
            return false;
        }
        return renamedFiles.add(new CopiedOrMovedFile(sourceFilePath, destinationFilePath, percentage));
    }

    public void setNumbers(int filesChanged, int linesDeleted, int linesInserted) {
        this.filesChanged = filesChanged;
        this.linesDeleted = linesDeleted;
        this.linesInserted = linesInserted;
    }

    /**
     * Represents a file added to or deleted from the repository for a given commit.
     */
    public static class AddedOrDeletedFile {

        // The path to the file.
        private File pathTofile;

        // The mode the file was added/deleted with.
        private String mode;

        /**
         * Constructor.
         *
         * @param pathToFile The path to the file.
         * @param mode       The mode the file was added/deleted with.
         */
        public AddedOrDeletedFile(File pathToFile, String mode) {
            this.pathTofile = pathToFile;
            this.mode = mode;
        }

        /**
         * Gets the mode of the added/deleted file.
         *
         * @return The mode of the added/deleted file.
         */
        public String getMode() {
            return mode;
        }

        /**
         * Gets the path to the file.
         *
         * @return The path to the file.
         */
        public File getPathTofile() {
            return pathTofile;
        }
    }

    /**
     * Represents a file that was copied from an existing file already tracked in the repository or a
     * tracked file that was moved from one name/place to another.
     */
    public static class CopiedOrMovedFile {

        // The path to the file that is the source of the copied/moved file.
        private File sourceFilePath;

        // The path to the new file/location.
        private File destinationFilePath;

        // The percentage. (not sure how to read this yet, -- jhl388 2008.06.15)
        private int percentage;

        /**
         * Constructor.
         *
         * @param sourceFilePath      The path to the source file.
         * @param destinationFilePath The path to the destination file.
         * @param percentage          The percentage.
         */
        public CopiedOrMovedFile(File sourceFilePath, File destinationFilePath, int percentage) {
            this.sourceFilePath = sourceFilePath;
            this.destinationFilePath = destinationFilePath;
            this.percentage = percentage;
        }

        /**
         * Gets the path to the destination file.
         *
         * @return The path to the destination file.
         */
        public File getDestinationFilePath() {
            return destinationFilePath;
        }

        /**
         * Gets the percentage.
         *
         * @return The percentage.
         */
        public int getPercentage() {
            return percentage;
        }

        /**
         * Gets the path to the source file.
         *
         * @return The path to the source file.
         */
        public File getSourceFilePath() {
            return sourceFilePath;
        }
    }
}
