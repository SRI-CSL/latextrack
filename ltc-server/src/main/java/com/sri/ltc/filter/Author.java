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
package com.sri.ltc.filter;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describing an author under GIT.  Authors have unique names and non-empty email addresses.
 * Their default initials are computed as the capital letters of all beginning words in the full name.
 *
 * @author linda
 */
public final class Author implements Comparable<Author> {

    private final static Pattern INITIALS_PATTERN = Pattern.compile("\\b([a-zA-Z])\\S*\\b");
    private final static Pattern EMAIL_PATTERN = Pattern.compile("^\\w([\\.\\w\\-])*[\\w]*@(\\w[\\w\\-\\.\\(\\)]*)+$");
    private final static Pattern AUTHOR_PATTERN = Pattern.compile("^\\s*(([a-zA-Z\\-\\.]+\\s*)+)(\\s+<([^<>\\s]+)>){0,1}(\\s+\\(([A-Z]+)\\)){0,1}\\s*$");

    public final String name;
    public final String email;
    private String initials;

    public Author(String name, String email, String initials) {
        if (name == null || "".equals(name))
            throw new IllegalArgumentException("Cannot create an author with empty or NULL name.");
        if (email == null)
            email = "";
        this.name = name;
        this.email = email;
        if (initials == null || "".equals(initials))
            initials = createInitials();
        this.initials = initials;
    }

    private String createInitials() {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = INITIALS_PATTERN.matcher(name);
        while (matcher.find()) {
            if (matcher.groupCount() > 0)
                buffer.append(matcher.group(1));
        }
        return buffer.toString().toUpperCase();
    }

    public Object[] asList() {
        return new Object[] {name, email};
    }

    public static Author fromList(Object[] list) {
        if (list == null || list.length < 1)
            throw new RuntimeException("Cannot create author from NULL or list with less than 1 entry");
        return new Author((String) list[0],
                list.length>1?(String) list[1]:null,
                null);
    }

    public static Author parse(String string) throws ParseException {
        if (string == null || "".equals(string))
            return null;

        Matcher mAuthor = AUTHOR_PATTERN.matcher(string);
        if (!mAuthor.matches())
            throw new ParseException("Cannot parse given string into an author: "+string, 0);

        if (mAuthor.group(4) != null) {
            Matcher mEmail = EMAIL_PATTERN.matcher(mAuthor.group(4));
            if (!mEmail.matches())
                throw new ParseException("Given email string is not valid: "+mAuthor.group(4), mAuthor.start(4));
        }

        return new Author(mAuthor.group(1).trim(), mAuthor.group(4), mAuthor.group(6));
    }

    public String gitRepresentation() {
        return name+("".equals(email)?"":" <"+email+">");
    }

    @Override
    public int compareTo(Author author) {
        int result = name.compareTo(author.name);
        if (result == 0)
            return email.compareTo(author.email);
        return result;
    }

    @Override
    public String toString() {
        return gitRepresentation()+" ("+initials+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;

        if (!email.equals(author.email)) return false;
        if (!name.equals(author.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }
}
