/**
 ************************ 80 columns *******************************************
 * XmlPrint
 *
 * Created on Jan 1, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.bmsi.gnudiff.Diff;
import com.bmsi.gnudiff.DiffPrint;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author linda
 */
public class XmlPrint extends DiffPrint.Base {

    private final List<Lexeme> list0, list1;

    public XmlPrint(Object[] a, Object[] b, List<Lexeme> list0, List<Lexeme> list1) {
        super(a, b);
        this.list0 = list0;
        this.list1 = list1;
        // TODO: print header with author information?
        // TODO: print header with current setting of definition of SMALL CHANGE?
    }

    private String escapeText(String text) {
        // escape "<", ">", "&", ', and "
        text = text.replaceAll("<","&lt;");
        text = text.replaceAll(">","&gt;");
        text = text.replaceAll("&","&amp;");
        text = text.replaceAll("'","&apos;");
        text = text.replaceAll("\"","&quot;");
        return text;
    }

    private static final Character[] toCharacters(char[] chars) {
        Character[] characters = new Character[chars.length];
        for (int i = 0; i < chars.length; i++) characters[i] = chars[i];
        return characters;
    }

    @Override
    protected void print_hunk(Diff.change hunk) {
        /* Determine range of line numbers involved in each file.  */
        analyze_hunk(hunk);
        if (deletes == 0 && inserts == 0)
            return;

        int length;

        // determine, if this is a small change
        if (deletes != 0 && inserts != 0 &&
                first0==last0 && first1==last1) {
            Lexeme lexem0 = list0.get(first0);
            Lexeme lexem1 = list1.get(first1);
            if (lexem0.contents.length() < 3 || lexem1.contents.length() < 3 ||
                    Levenshtein.getLevenshteinDistance(lexem0.contents, lexem1.contents) < 3) {
                // small change: determine character diff:
                // build up arrays for character-level diff
                Character[] a = toCharacters(lexem0.contents.toCharArray());
                Character[] b = toCharacters(lexem1.contents.toCharArray());
                Diff chardiff = new Diff(a,b);
                // generate output
                Diff.change charscript = chardiff.diff_2(false);
                DiffPrint.Base p = new SmallPrint(a,b,lexem1,outfile);
                p.print_script(charscript);
                return;
            }
        }

        // Deletions
        if (deletes != 0) {
            outfile.println("<deletion>");
            length = 0;
            StringBuilder contents = new StringBuilder();
            for (int i = first0; i <= last0; i++) {
                Lexeme lexeme = list0.get(i);
                length += lexeme.length;
                contents.append(lexeme.contents);
            }
            // deletion point in newer file determined by replacement or deletion
            // and the location of the next lexem (if any)
            if (inserts != 0)
                // if this pertains to a replacement, simply use last lexem position
                outfile.println("  <start line="+
                        list1.get(last1).line+" column="+
                        list1.get(last1).column+" />");
            else if (list1.size() > last1+1)
                outfile.println("  <start line="+
                        list1.get(last1+1).line+" column="+
                        list1.get(last1+1).column+" />");
            else
                // if this pertains to last token, may add column and length
                outfile.println("  <start line="+
                        list1.get(last1).line+" column="+
                        (list1.get(last1).column+list1.get(last1).length)+" />");            
            outfile.println("  <length chars="+length+" />");
            outfile.println("  <text>"+escapeText(contents.toString())+"</text>");
            outfile.println("</deletion>");
        }

        // Additions
        if (inserts != 0) {
            outfile.println("<addition>");
            length = 0;
            for (int i = first1; i <= last1; i++) {
                Lexeme lexeme = list1.get(i);
                length += lexeme.length;
            }
            outfile.println("  <start line="+
                    list1.get(first1).line+" column="+
                    list1.get(first1).column+" />");
            if (list1.size() > last1+1)
                outfile.println("  <end line="+
                        list1.get(last1+1).line+" column="+
                        list1.get(last1+1).column+" />");
            else
                outfile.println("  <end line="+
                        list1.get(last1).line+" column="+
                        (list1.get(last1).column+list1.get(last1).length)+" />");
            outfile.println("  <length chars="+length+" />");
            outfile.println("</addition>");
        }
    }

    private class SmallPrint extends DiffPrint.Base {

        private final Lexeme lexem1;

        private SmallPrint(Object[] a, Object[] b, Lexeme lexem1, PrintWriter writer) {
            super(a, b);
            this.lexem1 = lexem1;
            this.outfile = writer;
        }

        @Override
        protected void print_hunk(Diff.change hunk) {
            /* Determine range of line numbers involved in each file.  */
            analyze_hunk(hunk);
            if (deletes == 0 && inserts == 0)
                return;

            // Deletions
            if (deletes != 0) {
                outfile.println("<small_deletion>");
                StringBuilder contents = new StringBuilder();
                for (int i = first0; i <= last0; i++) {
                    contents.append(file0[i]);
                }
                outfile.println("  <start line="+
                        lexem1.line+" column="+
                        (lexem1.column+first0)+" />");
                outfile.println("  <length chars="+(last0-first0+1)+" />");
                outfile.println("  <text>"+escapeText(contents.toString())+"</text>");
                outfile.println("</small_deletion>");
            }

            // Additions
            if (inserts != 0) {
                outfile.println("<small_addition>");
                outfile.println("  <start line="+
                        lexem1.line+" column="+
                        (lexem1.column+first1)+" />");
                outfile.println("  <end line="+
                        lexem1.line+" column="+
                        (lexem1.column+last1+1)+" />");
                outfile.println("  <length chars="+(last1-first1+1)+" />");
                outfile.println("</small_addition>");
            }
        }
    }
}
