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
%xstate PREAMBLE_SEEN, IN_COMMENT, EOF

%{
    private final static Pattern pattern = Pattern.compile(".*(\r\n|\r|\n).*"); // newlines
    private int prior_state = 0;
    private boolean preambleSeen = false;

    private List<Lexeme> processNewline(Lexeme newlineLexeme) {
        List<Lexeme> lexemes = Lists.newArrayList(newlineLexeme);
        if (newlineLexeme.inComment)
            yybegin(prior_state);
        return lexemes;
    }

    /* Main function to run analysis stand-alone. */
    public static void main(String argv[]) {
        Reader reader;
        List<Lexeme> lexemes;

        // Obtain reader from argument or STDIN
        if (argv.length < 1)
            reader = new InputStreamReader(System.in);
        else
            try {
                reader = new FileReader(argv[0]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

        // Run lexical analyzer over given file to get lexeme and locations
        Lexer scanner = new Lexer(reader);
        try {
            System.out.println(new Lexeme(LexemeType.START_OF_FILE, "", 0, false, false));
            while ((lexemes = scanner.yylex()) != null)
                for (Lexeme lexeme : lexemes)
                    System.out.println(lexeme);
            scanner.yyclose();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
%}

EOL         = [\r\n] | \r\n
punctuation = [.,;:!?\'\"`\^]
symbol      = [{}\[\]()|#$%&@+\-<=>_\\/*~]
space       = [ \t\f]

%%
/* -----------------Lexical Rules Section------------------------------------ */ 

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT>
  \\[A-Za-z]+        { return Lists.newArrayList(
                         new Lexeme(LexemeType.COMMAND, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* commands that are more than one letter long */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT>
  \\[^ \t\r\n\f]     { return Lists.newArrayList(
                         new Lexeme(LexemeType.COMMAND, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* commands that are one non-whitespace character after backslash */

\\begin\{document\}  { preambleSeen = true;
		       yybegin(PREAMBLE_SEEN);
                       return Lists.newArrayList(
                         new Lexeme(LexemeType.COMMAND, "\\begin", yychar, true, false),
                         new Lexeme(LexemeType.SYMBOL, "{", yychar+6, true, false),
                         new Lexeme(LexemeType.WORD, "document", yychar+7, true, false),
                         new Lexeme(LexemeType.SYMBOL, "}", yychar+15, true, false)); }
  /* set flag that first preamble (not in comment!) has been seen */

<YYINITIAL,PREAMBLE_SEEN >
  %+                 { prior_state = yystate(); // remember prior state
                       yybegin(IN_COMMENT);
                       return Lists.newArrayList(
                         new Lexeme(LexemeType.COMMENT_BEGIN, yytext(), yychar, preambleSeen, true)); }
  /* if not escaped, first (in YYINITIAL) %'s indicates comment begin */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT>
  {punctuation}      { return Lists.newArrayList(
                         new Lexeme(LexemeType.PUNCTUATION, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* match single punctuation characters */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT>
  [+\-]{0,1} [0-9] ([A-Za-z0-9] | [,\.][0-9])+ 
                     { return Lists.newArrayList(
                         new Lexeme(LexemeType.NUMERAL, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* numerals start with an optional minus or plus and one digit, then almost anything goes */ 

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT>
  {symbol}           { return Lists.newArrayList(
                         new Lexeme(LexemeType.SYMBOL, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* match single symbol characters */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT>
  [A-Za-z0-9\-]+     { return Lists.newArrayList(
                         new Lexeme(LexemeType.WORD, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* words are letters, digits and hyphen */ 

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> {
  {space}*\n({space}*{EOL})+ |
  {space}*\r{space}*\r({space}*{EOL})* |
  {space}*\r\n({space}*{EOL})+ 
                     { return processNewline(
                         new Lexeme(LexemeType.PARAGRAPH, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
}
  /* paragraphs are 2 or more end-of-lines and possibly white space without line breaks in between */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> 
  {space}*{EOL}      { return processNewline(
                         new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* other, non-paragraph line breaks */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> 
  {space}+           { return Lists.newArrayList(
                         new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* gobble-up any white space not in front of newlines */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> 
  <<EOF>>            { yybegin(EOF);
                       return Lists.newArrayList(
                         new Lexeme(LexemeType.END_OF_FILE, "", yychar, preambleSeen, false)); }
  /* mark end-of-file so that there is always one matching lexeme to determine end position of deletions */ 

.                    { RuntimeException e = new RuntimeException("Cannot parse at position "+yychar+": "+yytext());
                       Logger.getLogger(Lexer.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                       throw e; }
