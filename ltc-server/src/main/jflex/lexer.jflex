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
/* Analysis of LaTeX source code for tracking changes with integration into code base. */

/* -----------------User Code Section---------------------------------------- */
package com.sri.ltc.latexdiff;

import com.google.common.collect.Lists;
import com.sri.ltc.CommonUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

%%
/* -----------------Options and Declarations Section------------------------- */

%class Lexer
%final
%public
%type List<Lexeme>
%char
%unicode
%xstate PREAMBLE_SEEN, EOF

%{
    private final static Pattern pattern = Pattern.compile(".*(\r\n|\r|\n).*"); // newlines
    private boolean preambleSeen = false;

    /* Main functions to run analysis stand-alone. */

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... "+Lexer.class.getCanonicalName()+" [options...] [FILE] \nwith");
        parser.printUsage(out);
    }

    public static void main(String argv[]) {
        // parse arguments
        final LexerOptions options = new LexerOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(argv);
        } catch (CmdLineException e) {
            System.out.println(CommonUtils.getNotice()); // output NOTICE on command line
            System.err.println(e.getMessage());
            printUsage(System.err, parser);
            return;
        }

        if (options.displayHelp) {
            System.out.println(CommonUtils.getNotice()); // output NOTICE on command line
            printUsage(System.out, parser);
            System.exit(1);
        }

        if (options.displayLicense) {
            System.out.println(CommonUtils.getNotice()); // output NOTICE on command line
            System.out.println("LTC is licensed under:\n\n" + CommonUtils.getLicense());
            return;
        }

        Reader reader;
        List<Lexeme> lexemes;

        // Obtain reader from argument or STDIN
        if (options.file == null)
            reader = new InputStreamReader(System.in);
        else
            try {
                reader = new FileReader(options.file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

        // Run lexical analyzer over given file to get lexeme and locations
        Lexer scanner = new Lexer(reader);
        try {
            System.out.println(new Lexeme(LexemeType.START_OF_FILE, "", 0, false));
            while ((lexemes = scanner.yylex()) != null)
                for (Lexeme lexeme : lexemes)
                    System.out.println(lexeme);
            scanner.yyclose();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    static class LexerOptions {
        @Option(name="-h",usage="display usage and exit")
        boolean displayHelp = false;

        @Option(name="-c",usage="display copyright/license information and exit")
        boolean displayLicense = false;

        @Argument(required=false, metaVar="FILE", usage="file to analyze")
        File file;
    }

%}

EOL         = [\r\n] | \r\n
space       = [ \t\f]

%%
/* -----------------Lexical Rules Section------------------------------------ */ 

<YYINITIAL>
  \\begin\{document\} { preambleSeen = true;
		        yybegin(PREAMBLE_SEEN);
                        return Lists.newArrayList(
                          new Lexeme(LexemeType.COMMAND, "\\begin", yychar, true),
                          new Lexeme(LexemeType.SYMBOL, "{", yychar+6, true),
                          new Lexeme(LexemeType.WORD, "document", yychar+7, true),
                          new Lexeme(LexemeType.SYMBOL, "}", yychar+15, true)); }
  /* set flag that first preamble has been seen */

<YYINITIAL,PREAMBLE_SEEN>
  \\[A-Za-z]+         { return Lists.newArrayList(
                          new Lexeme(LexemeType.COMMAND, yytext(), yychar, preambleSeen)); }
  /* commands that are more than one letter long */

<YYINITIAL,PREAMBLE_SEEN>
  \\[^ \t\r\n\f]      { return Lists.newArrayList(
                          new Lexeme(LexemeType.COMMAND, yytext(), yychar, preambleSeen)); }
  /* commands that are one non-whitespace character after backslash */

<YYINITIAL,PREAMBLE_SEEN>
  [+\-]{0,1} [0-9] ([A-Za-z0-9]* | [,\.][0-9]+) 
                      { return Lists.newArrayList(
                          new Lexeme(LexemeType.NUMERAL, yytext(), yychar, preambleSeen)); }
  /* numerals start with an optional minus or plus and one digit, then almost anything goes */ 

<YYINITIAL,PREAMBLE_SEEN>
  [A-Za-z0-9\-]+      { return Lists.newArrayList(
                          new Lexeme(LexemeType.WORD, yytext(), yychar, preambleSeen)); }
  /* words are letters, digits and hyphen */ 

<YYINITIAL,PREAMBLE_SEEN> {
  \n({space}*{EOL})+ |
  \r{space}*\r({space}*{EOL})* |
  \r\n({space}*{EOL})+ 
                      { return Lists.newArrayList(
                          new Lexeme(LexemeType.PARAGRAPH, yytext(), yychar, preambleSeen)); }
}
  /* paragraphs are 2 or more end-of-lines and possibly white space in between */

<YYINITIAL,PREAMBLE_SEEN> 
  {space}+            { return Lists.newArrayList(
                          new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, preambleSeen)); }
  /* gobble-up any white space */

<YYINITIAL,PREAMBLE_SEEN> 
  {EOL}+              { return Lists.newArrayList(
                          new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, preambleSeen)); }
  /* gobble-up any end-of-line characters */

<YYINITIAL,PREAMBLE_SEEN> 
  <<EOF>>             { yybegin(EOF);
                        return Lists.newArrayList(
                          new Lexeme(LexemeType.END_OF_FILE, "", yychar, preambleSeen)); }
  /* mark end-of-file so that there is always one matching lexeme to determine end position of deletions */ 

<YYINITIAL,PREAMBLE_SEEN>
  .                   { return Lists.newArrayList(
                          new Lexeme(LexemeType.SYMBOL, yytext(), yychar, preambleSeen)); }
  /* match other single characters */
