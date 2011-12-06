/* Analysis of LaTeX source code for tracking changes with integration into code base. */

/* -----------------User Code Section---------------------------------------- */
package com.sri.ltc.latexdiff;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

%%
/* -----------------Options and Declarations Section------------------------- */

%class Lexer
%final
%public
%type Lexeme
%char
%line
%column
%unicode
%state EOF
%xstate IN_COMMENT

%{
    private final static Pattern pattern = Pattern.compile(".*(\r\n|\r|\n).*"); // newlines
    private int prior_state = 0;
    public void startInComment() {
        yybegin(IN_COMMENT);
    }

    /* Main function to run analysis stand-alone. */
    public static void main(String argv[]) {
        Reader reader;
        Lexeme lexem;

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
            while ((lexem = scanner.yylex()) != null)
                System.out.println(lexem);
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
whitespace  = {EOL} | {space}

%%
/* -----------------Lexical Rules Section------------------------------------ */ 

\\[A-Za-z]+          { return new Lexeme(LexemeType.COMMAND, yytext(), yychar, yyline+1, yycolumn, yylength()); } 
  /* commands that are more than one letter long */

\\[^ \t\r\n\f]       { return new Lexeme(LexemeType.COMMAND, yytext(), yychar, yyline+1, yycolumn, yylength()); }
  /* commands that are one non-whitespace character after backslash */

\\begin\{document\}  { return new Lexeme(LexemeType.PREAMBLE, yytext(), yychar, yyline+1, yycolumn, yylength()); }
  /* dividing preamble from rest of document */

%                    { prior_state = yystate(); // remember prior state
                       yybegin(IN_COMMENT);
                       return new Lexeme(LexemeType.COMMENT, yytext(), yychar, yyline+1, yycolumn, yylength()); }
  /* if not escaped, % indicates comments until next newline */

<IN_COMMENT> {
  {punctuation} |
  {symbol} |
  \\[A-Za-z]+ |
  \\[^ \t\r\n\f] |
  [A-Za-z0-9\-]+
                     { return new Lexeme(LexemeType.COMMENT, yytext(), yychar, yyline+1, yycolumn, yylength()); }
}
  /* match COMMENT lexemes until end-of-line */

{punctuation}        { return new Lexeme(LexemeType.PUNCTUATION, yytext(), yychar, yyline+1, yycolumn, yylength()); }
  /* match single punctuation characters */

{symbol}             { return new Lexeme(LexemeType.SYMBOL, yytext(), yychar, yyline+1, yycolumn, yylength()); }
  /* match single symbol characters */

[A-Za-z0-9\-]+       { return new Lexeme(LexemeType.WORD, yytext(), yychar, yyline+1, yycolumn, yylength()); }
  /* words are letters, digits and hyphen */ 

<YYINITIAL,IN_COMMENT> 
  {space}+           { return new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, yyline+1, yycolumn, yylength()); }
  /* gobble-up white space before deciding on newlines and paragraphs */

<YYINITIAL,IN_COMMENT> {
  \n({space}*{EOL})+ |
  \r{space}*\r({space}*{EOL})* |
  \r\n({space}*{EOL})+ 
                     { if (yystate() == IN_COMMENT)
                         yybegin(prior_state);
                       return new Lexeme(LexemeType.PARAGRAPH, yytext(), yychar, yyline+1, yycolumn, yylength()); }
}
  /* paragraphs are 2 or more end-of-lines and possibly white space without line breaks in between */

<YYINITIAL,IN_COMMENT> 
  {EOL}{space}*      { if (yystate() == IN_COMMENT) 
		         yybegin(prior_state);
                       return new Lexeme(LexemeType.WHITESPACE, yytext(), yychar, yyline+1, yycolumn, yylength()); } 
  /* other, non-paragraph whitespace */

<YYINITIAL,IN_COMMENT> 
  <<EOF>>            { yybegin(EOF);
                       return new Lexeme(LexemeType.END_OF_FILE, yytext(), yychar, yyline+1, yycolumn, 0); }
  /* mark end-of-file so that there is always one matching lexeme to determine end position of deletions */ 

.                    { RuntimeException e = new RuntimeException("Cannot parse at position "+yychar+": "+yytext());
                       Logger.getLogger(Lexer.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                       throw e; }
