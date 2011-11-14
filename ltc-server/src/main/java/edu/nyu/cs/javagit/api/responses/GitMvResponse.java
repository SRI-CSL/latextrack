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

import java.io.File;

/**
 * A response data object for the git-mv command. For information about the contents of
 * GitMvResponse instances returned by a given method, please see the JavaDoc for the method
 * in question.
 */
public final class GitMvResponse extends AbstractResponse {

    // Variable to store the source file/folder/symlink of the response.
    protected File source;

    // Variable to store the destination file/folder/symlink of the response.
    protected File destination;

    // String Buffer to store the comment message after execution of git-mv.
    protected StringBuffer message = new StringBuffer();

    public String getMessage() {
        return message.toString();
    }

    public File getDestination() {
        return destination;
    }

    public File getSource() {
        return source;
    }

    /**
     * Adds comments from each line of the message, if received, upon successful execution of the
     * git-mv command, to the message buffer.
     *
     * @param comment The comment from each line of the message, if received, upon successful execution of
     *                the git-mv.
     */
    public void addComment(String comment) {
        message.append(comment);
    }

    /**
     * Sets the destination file/folder/symlink in response to the destination
     *
     * @param destination The destination to set
     */
    public void setDestination(File destination) {
        this.destination = destination;
    }

    /**
     * Sets the source file/folder/symlink in response object to the source string.
     *
     * @param source The source to set
     */
    public void setSource(File source) {
        this.source = source;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (null != source) {
            buffer.append("Source: ");
            buffer.append(source.getName());
            buffer.append(" ");
        }

        if (null != destination) {
            buffer.append("Destination: ");
            buffer.append(destination.getName());
            buffer.append(" ");
        }

        if ((message.length() != 0)) {
            buffer.append("Message: ");
            buffer.append(message.toString());
        }
        return buffer.toString();
    }
}
