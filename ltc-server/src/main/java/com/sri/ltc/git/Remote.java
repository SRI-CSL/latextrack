/**
 ************************ 80 columns *******************************************
 * Remote
 *
 * Created on Oct 25, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.git;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates git remote objects.
 *
 * @author linda
 */
public final class Remote implements Comparable<Remote> {

    private final static Pattern REMOTE_PATTERN = Pattern.compile("^(\\S+)(\\s+<(\\S+)>)?(\\s+\\(read only\\))?$");

    public final String name;
    public final String url;
    private boolean readOnly = true;

    public Remote(String name, String url, boolean readOnly) {
        if (url == null || "".equals(url))
            throw new IllegalArgumentException("Cannot create remote with NULL or empty URL.");
        if (name == null)
            name = "";
        this.name = name;
        this.url = url;
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isAlias() {
        return !"".equals(name);
    }

    public static Remote parse(String string) throws ParseException {
        if (string == null || "".equals(string))
            return null;

        Matcher m = REMOTE_PATTERN.matcher(string.trim());
        if (!m.matches())
            throw new ParseException("Cannot parse given string into a remote: "+string, 0);

        if (m.group(3) == null)
            return new Remote(null, m.group(1), m.group(4) != null);
        else
            return new Remote(m.group(1), m.group(3), m.group(4) != null);

    }

    @Override
    public String toString() {
        StringBuilder urlBuilder = new StringBuilder();
        if (!"".equals(name)) {
            urlBuilder.append(" <");
            urlBuilder.append(url);
            urlBuilder.append(">");
        } else
            urlBuilder.append(url);
        return name + urlBuilder.toString() + (readOnly?" (read only)":"");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Remote remote = (Remote) o;

        if (name != null ? !name.equals(remote.name) : remote.name != null) return false;
        if (url != null ? !url.equals(remote.url) : remote.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Remote remote) {
        int result = name.compareTo(remote.name);
        if (result == 0)
            result = url.compareTo(remote.url);
        return result;
    }

    public String[] toArray() {
        return new String[] {name, url, ""+readOnly};
    }

    public static Remote fromArray(Object[] a) {
        if (a == null || a.length != 3)
            throw new IllegalArgumentException("Cannot create remote from array if NULL or length != 3");
        return new Remote(a[0].toString(), a[1].toString(), Boolean.parseBoolean(a[2].toString()));
    }
}
