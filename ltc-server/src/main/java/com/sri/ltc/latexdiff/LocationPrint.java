/**
 ************************ 80 columns *******************************************
 * LocationPrint
 *
 * Created on Jan 1, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.bmsi.gnudiff.Diff;
import com.bmsi.gnudiff.DiffPrint;

import java.util.List;

/**
 * @author linda
 */
public class LocationPrint extends DiffPrint.Base {

    private final List<Lexeme> list0, list1;

    public LocationPrint(Object[] a, Object[] b, List<Lexeme> list0, List<Lexeme> list1) {
        super(a, b);
        this.list0 = list0;
        this.list1 = list1;
    }

    @Override
    protected void print_hunk(Diff.change hunk) {
        /* Determine range of line numbers involved in each file.  */
        analyze_hunk(hunk);
        if (deletes == 0 && inserts == 0)
            return;

        /* Print out the line number header for this hunk */
        print_number_range (',', first0, last0);
        outfile.print(change_letter(inserts, deletes));
        print_number_range (',', first1, last1);
        outfile.println();

        /* Print the lines that the first file has.  */
        // Deletions
        if (deletes != 0)
            for (int i = first0; i <= last0; i++) {
                Lexeme lexeme = list0.get(i);
                print_1_line ("< ", file0[i]+"  @ "+lexeme.pos+" ("+lexeme.length+")");
            }

        // Replacements
        if (inserts != 0 && deletes != 0) {
            outfile.println("---");
        }

        /* Print the lines that the second file has.  */
        // Additions
        if (inserts != 0)
            for (int i = first1; i <= last1; i++) {
                Lexeme lexeme = list1.get(i);
                print_1_line ("> ", file1[i]+"  @ "+lexeme.pos+" ("+lexeme.length+")");
            }
    }
}
