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
package edu.nyu.cs.javagit.utilities;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class provides utilities methods that perform various checks for validity.
 */
public class CheckUtilities {

    /**
     * Checks that the specified filename exists. This assumes that the above check for string
     * validity has already been run and the path/filename is neither null or of size 0.
     *
     * @param filename File or directory path
     */
    public static void checkFileValidity(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new IOException("File or path does not exist: " + filename);
        }
    }

    /**
     * Checks that the specified file exists.
     *
     * @param file File or directory path
     * @throws edu.nyu.cs.javagit.api.JavaGitException if given file does not exist
     */
    public static void checkFileValidity(File file) throws JavaGitException {
        if (!file.exists()) {
            throw new JavaGitException("Given file "+file.getAbsolutePath()+" does not exist");
        }
    }

    /**
     * Checks that the int to check is greater than <code>lowerBound</code>. If the int to check is
     * not greater than <code>lowerBound</code>, an <code>IllegalArgumentException</code> is
     * thrown.
     *
     * @param toCheck      The int to check.
     * @param lowerBound   The lower bound to check against.
     * @param variableName The name of the variable being checked; for use in exception messages.
     */
    public static void checkIntArgumentGreaterThan(int toCheck, int lowerBound, String variableName) {
        if (lowerBound >= toCheck) {
            throw new IllegalArgumentException("The int argument is not greater than the lower bound (lowerBound < toCheck): { toCheck=["
                    + toCheck + "], lowerBound=[" + lowerBound + "], variableName=[" + variableName + "] }");
        }
    }

    /**
     * Performs a null check on the specified object. If the object is null, a
     * <code>NullPointerException</code> is thrown.
     *
     * @param obj          The object to check.
     */
    public static void checkNullArgument(Object obj) {
        if (null == obj) {
            throw new NullPointerException();
        }
    }

    /**
     * Checks that two lists are equal, specifically: they are both null or the both contain the same
     * elements.
     *
     * @param l1 The first list to check.
     * @param l2 The second list to check.
     * @return True if one of the following conditions hold:
     *         <ol>
     *         <li>Both lists are null</li>
     *         <li>a) Neither list is null; b) for each element in list 1 an equivalent element
     *         exists in list 2; and c) for each element in list 2, an equivalent element exists in
     *         list 1</li>
     *         </ol>
     */
    public static boolean checkListsEqual(List<?> l1, List<?> l2) {

        // TODO (jhl388): write a test case for this method.

        if (null != l1 && null == l2) {
            return false;
        }

        if (null == l1 && null != l2) {
            return false;
        }

        if (null != l1) {
            for (Object e : l1) {
                if (!l2.contains(e)) {
                    return false;
                }
            }

            for (Object e : l2) {
                if (!l1.contains(e)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks to see if two objects are equal. The Object.equal() method is used to check for
     * equality.
     *
     * @param o1 The first object to check.
     * @param o2 The second object to check.
     * @return True if the two objects are equal. False if the objects are not equal.
     */
    public static boolean checkObjectsEqual(Object o1, Object o2) {
        if (null != o1 && !o1.equals(o2)) {
            return false;
        }

        if (null == o1 && null != o2) {
            return false;
        }

        return true;
    }

    /**
     * Checks a <code>String</code> argument to make sure it is not null and contains one or more
     * characters. If the <code>String</code> is null, a <code>NullPointerException</code> is
     * thrown. If the <code>String</code> has length zero, an <code>IllegalArgumentException</code>
     * is thrown.
     *
     * @param str          The string to check.
     * @param variableName The name of the variable being checked; for use in exception messages.
     */
    public static void checkStringArgument(String str, String variableName) {
        if (null == str) {
            throw new NullPointerException();
        }
        if (str.isEmpty()) {
            throw new IllegalArgumentException("String \"" + variableName + "\" is empty.");
        }
    }

    /**
     * Checks a <code>List&lt;String&gt;</code> argument to make sure it is not null, none of its
     * elements are null, and all its elements contain one or more characters. If the
     * <code>List&lt;String&gt;</code> or a contained <code>String</code> is null, a
     * <code>NullPointerException</code> is thrown. If the <code>List&lt;String&gt;</code> or a
     * contained <code>String</code> has length zero, an <code>IllegalArgumentException</code> is
     * thrown.
     *
     * @param str          The <code>List&lt;String&gt;</code> to check.
     * @param variableName The name of the variable being checked; for use in exception messages.
     */
    public static void checkStringListArgument(List<String> str, String variableName) {
        if (null == str) {
            throw new NullPointerException();
        }
        if (str.isEmpty()) {
            throw new IllegalArgumentException("String \"" + variableName + "\" is empty.");
        }
        for (int i = 0; i < str.size(); i++) {
            checkStringArgument(str.get(i), variableName+"["+i+"]");
        }
    }

    /**
     * Checks if two unordered lists are equal.
     *
     * @param l1 The first list to test.
     * @param l2 The second list to test.
     * @return True if:
     *         <ul>
     *         <li>both lists are null or</li>
     *         <li>both lists are the same length, there exists an equivalent object in l2 for all
     *         objects in l1, and there exists an equivalent object in l1 for all objects in l2</li>
     *         </ul>
     *         False otherwise.
     */
    public static boolean checkUnorderedListsEqual(List<?> l1, List<?> l2) {
        if (null == l1 && null != l2) {
            return false;
        }

        if (null != l1 && null == l2) {
            return false;
        }

        if (l1.size() != l2.size()) {
            return false;
        }

        for (Object o : l1) {
            if (!l2.contains(o)) {
                return false;
            }
        }

        for (Object o : l2) {
            if (!l1.contains(o)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks a <code>List</code> argument to make sure that all the <code>Ref</code> in the list
     * are of same <code>refType</code> type. If there is a mismatch
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param list         The <code>List</code> to check.
     * @param type         The <code>refType</code> check against.
     * @param variableName The name of the variable being checked; for use in exception messages.
     */
    public static void validateListRefType(List<Ref> list, Ref.RefType type, String variableName) {
        checkNullArgument(list);
        if (list.size() == 0) {
            throw new IllegalArgumentException("An List<?> argument was not specified or is empty but is required: "
                    + " { variableName=[" + variableName + "] }");
        }
        for (Ref ref : list) {
            validateArgumentRefType(ref, type, variableName);
        }
    }

    /**
     * Checks a <code>Ref</code> argument to make sure that it is of given <code>refType</code> type.
     * If not <code>IllegalArgumentException</code> is thrown.
     *
     * @param name         The argument to check.
     * @param type         The <code>refType</code> to check against.
     * @param variableName The name of the variable being checked; for use in exception messages.
     */
    public static void validateArgumentRefType(Ref name, Ref.RefType type, String variableName) {
        checkNullArgument(name);
        if (name.getRefType() != type) {
            throw new IllegalArgumentException("Incorrect refType type: "
                    + "{ variableName=[" + variableName + "] }");
        }
    }

    /**
     * Checks a <code>File</code> argument to make sure that it is a directory. If not
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param fileName     The <code>File</code> to be checked.
     * @param variableName The name of the variable being checked; for use in exception messages.
     */
    public static void checkDirectoryArgument(File fileName, String variableName) {
        checkNullArgument(fileName);
        if (!fileName.isDirectory()) {
            throw new IllegalArgumentException("The argument should be a directory: "
                    + "{ variableName=[" + variableName + "] }");
        }
    }
}
