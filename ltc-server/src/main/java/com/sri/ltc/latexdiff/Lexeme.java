package com.sri.ltc.latexdiff;

/**
 * @author linda
 */
public final class Lexeme {

    final LexemeType type;
    final String contents;
    final int pos, length;
    final boolean preambleSeen;
    final boolean inComment;
    final Integer[] removed;

    public Lexeme(LexemeType type, String contents, int pos, boolean preambleSeen, boolean inComment, Integer... removed) {
        if (type == null)
            throw new IllegalArgumentException("Cannot create Lexeme of type NULL");
        this.type = type;
        if (contents == null)
            throw new IllegalArgumentException("Cannot create Lexeme with contents NULL");
        this.contents = contents;
        this.length = contents.length();
        this.pos = pos;
        this.preambleSeen = preambleSeen;
        this.inComment = inComment;
        this.removed = removed;
    }

    public String displayContents() {
        return type.isPrintable()?contents:"";
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(type+" "+displayContents()+"  @ "+pos+" ("+length+")");
        if (preambleSeen)
            result.append(" P");
        if (inComment)
            result.append(" C");
        if (removed != null && removed.length > 0) {
            result.append(" [");
            for (int i = 0; i < removed.length; i++)
                result.append(removed[i]+",");
            result.deleteCharAt(result.length()-1); // remove last ","
            result.append("]");
        }
        return result.toString();
    }
}
