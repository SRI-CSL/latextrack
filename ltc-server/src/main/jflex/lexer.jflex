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
  {symbol}           { return Lists.newArrayList(
                         new Lexeme(LexemeType.SYMBOL, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* match single symbol characters */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT>
  [A-Za-z0-9\-]+     { return Lists.newArrayList(
                         new Lexeme(LexemeType.WORD, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* words are letters, digits and hyphen */ 

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> 
  {space}+           { return Lists.newArrayList(
                         new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, preambleSeen, yystate() == IN_COMMENT)); }
  /* gobble-up white space before deciding on newlines and paragraphs */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> {
  \n({space}*{EOL})+ |
  \r{space}*\r({space}*{EOL})* |
  \r\n({space}*{EOL})+ 
                     { if (yystate() == IN_COMMENT)
                         yybegin(prior_state);
                       return Lists.newArrayList(
                         new Lexeme(LexemeType.PARAGRAPH, yytext(), yychar, preambleSeen, false)); }
}
  /* paragraphs are 2 or more end-of-lines and possibly white space without line breaks in between */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> 
  {EOL}{space}*      { if (yystate() == IN_COMMENT) 
		         yybegin(prior_state);
                       return Lists.newArrayList(
                         new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, preambleSeen, false)); } 
  /* other, non-paragraph line breaks */

<YYINITIAL,PREAMBLE_SEEN,IN_COMMENT> 
  <<EOF>>            { yybegin(EOF);
                       return Lists.newArrayList(
                         new Lexeme(LexemeType.END_OF_FILE, "", yychar, preambleSeen, false)); }
  /* mark end-of-file so that there is always one matching lexeme to determine end position of deletions */ 

.                    { RuntimeException e = new RuntimeException("Cannot parse at position "+yychar+": "+yytext());
                       Logger.getLogger(Lexer.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                       throw e; }
