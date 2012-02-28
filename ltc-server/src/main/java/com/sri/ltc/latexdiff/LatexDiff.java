/**
 ************************ 80 columns *******************************************
 * LatexDiff
 *
 * Created on Dec 29, 2009.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.bmsi.gnudiff.Diff;
import com.sri.ltc.logging.LevelOptionHandler;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * @author linda
 */
public final class LatexDiff {

    private static final Set<LexemeType> SPACE = new HashSet<LexemeType>();
    static {
        SPACE.add(LexemeType.PARAGRAPH);
        SPACE.add(LexemeType.WHITESPACE);
    }
    private static final Set<LexemeType> INDICES_TYPES = new HashSet<LexemeType>();
    static {
        INDICES_TYPES.add(LexemeType.COMMAND);
        INDICES_TYPES.add(LexemeType.COMMENT);
    }
    private static final Comparator<Lexeme> LEXEME_TYPE_COMPARATOR = new Comparator<Lexeme>() {
        public int compare(Lexeme o1, Lexeme o2) {
            return o1.type.compareTo(o2.type);
        }
    };

    // variables to contain parts for diff'ing
    private List<List<Lexeme>> lexemLists = new ArrayList<List<Lexeme>>();
    private String[][] diffInputs = new String[2][];
    private String[] contents = new String[2];
    Diff.change script;

    // TODO: make this private (and unit test via reflection?)
    /**
     * Perform lexical analysis on the text given as a wrapper of a reader.
     * Remove whitespace in the whole text and paragraphs in the preamble. 
     * Closes the reader after analysis.
     *
     * @param wrapper Wrapper of reader pointing to text to be analyzed
     * @param startInComment Flag whether to start this analysis inside a comment
     * @return List of Lexemes obtained from analysis (never null)
     * @throws IOException if the scanner encounters an IOException
     */
    public List<Lexeme> analyze(ReaderWrapper wrapper, boolean startInComment) throws IOException {
        List<Lexeme> list = new ArrayList<Lexeme>(Arrays.asList(new Lexeme[] {
                new Lexeme(LexemeType.START_OF_FILE, "", 0, 0)}));
        Lexer scanner = new Lexer(wrapper.createReader());
        if (startInComment) scanner.startInComment();
        Lexeme lexeme;
        while ((lexeme = scanner.yylex()) != null)
            if (!LexemeType.WHITESPACE.equals(lexeme.type)) // ignore whitespace
                list.add(lexeme);
        scanner.yyclose();
        // remove paragraphs in the preamble if there is one:
        int pos = findPreamble(list);
        if (pos != -1) {
            List<Lexeme> preamble = list.subList(0, pos);
            for (Iterator<Lexeme> i = preamble.iterator(); i.hasNext(); ) {
                if (LexemeType.PARAGRAPH.equals(i.next().type))
                    i.remove();
            }
        }
        return list;
    }

    private Character[] toCharacters(char[] chars) {
        Character[] characters = new Character[chars.length];
        for (int i = 0; i < chars.length; i++) characters[i] = chars[i];
        return characters;
    }

    private EnumSet<Change.Flag> buildFlags(boolean inPreamble, LexemeType type) {
        EnumSet<Change.Flag> flags = EnumSet.noneOf(Change.Flag.class);
        if (inPreamble) flags.add(Change.Flag.PREAMBLE);
        if (LexemeType.COMMENT.equals(type)) flags.add(Change.Flag.COMMENT);
        if (LexemeType.COMMAND.equals(type) || LexemeType.PREAMBLE.equals(type)) flags.add(Change.Flag.COMMAND);
        return flags;
    }

    private SortedSet<Change> mergeSmallDiffResult(Diff.change changes, String text0, Lexeme lexeme1, boolean inPreamble) {
        SortedSet<Change> result = new TreeSet<Change>();

        // go through linked list of changes
        for (Diff.change hunk = changes; hunk != null; hunk = hunk.link) {
            if (hunk.deleted == 0 && hunk.inserted == 0)
                continue;

            // Additions
            if (hunk.inserted > 0) {
                result.add(new SmallAddition(
                        lexeme1.pos+hunk.line1,
                        lexeme1.pos+hunk.line1+hunk.inserted,
                        buildFlags(inPreamble, lexeme1.type)));
            }

            // Deletions
            if (hunk.deleted > 0) {
                result.add(new SmallDeletion(
                        lexeme1.pos+hunk.line1,
                        text0.substring(hunk.line0, hunk.line0+hunk.deleted),
                        buildFlags(inPreamble, lexeme1.type)));
            }
        }

        return result;
    }

    // find location of preamble in given list of lexemes
    // returns -1 if no preamble exists and otherwise position of first preamble
    private int findPreamble(List<Lexeme> list) {
        List<Lexeme> sortedList = new ArrayList<Lexeme>(list);
        Collections.sort(sortedList, LEXEME_TYPE_COMPARATOR);
        int pos = Collections.binarySearch(sortedList,
                new Lexeme(LexemeType.PREAMBLE, "", 0, 0),
                LEXEME_TYPE_COMPARATOR);
        if (pos < 0)
            return -1;
        return list.indexOf(sortedList.get(pos));
    }

    // calculate pairs of indices that indicate successive COMMENT or COMMAND lexemes in given input list.
    private List<IndexPair> getIndices(List<Lexeme> list, int offset) {
        List<IndexPair> result = new ArrayList<IndexPair>();
        if (list == null || list.isEmpty())
            return result;

        int lastLeft = 0;
        for (int i = 1; i < list.size(); i++) {
            LexemeType currentType = list.get(i).type;
            switch (currentType) {
                case COMMAND:
                case COMMENT:
                    if (!currentType.equals(list.get(lastLeft).type)) {
                        result.add(new IndexPair(lastLeft + offset, i + offset));
                        lastLeft = i;
                    }
                    break;
                default:
                    if (INDICES_TYPES.contains(list.get(lastLeft).type)) {
                        result.add(new IndexPair(lastLeft + offset, i + offset));
                        lastLeft = i;
                    }
            }
        }
        // add last pair
        result.add(new IndexPair(lastLeft + offset, list.size() + offset));

        return result;
    }

    private synchronized List<Change> mergeDiffResult(Diff.change changes, List<Lexeme> list0, List<Lexeme> list1) {
        SortedSet<Change> result = new TreeSet<Change>();

        int preamble1 = findPreamble(list1); // find preamble index in newer text

        // go through linked list of changes and convert each hunk into Change(s):
        int last_i0 = 0, last_i1 = 0; // remember last position of replacements to avoid checking them again
        for (Diff.change hunk = changes; hunk != null; hunk = hunk.link) {

            // no real change if both are 0:
            if (hunk.deleted == 0 && hunk.inserted == 0)
                continue;

            // determine, if this could be a replacement containing small changes:
            // compare each lexeme from list0 to each in list1
            if (hunk.line0 >= last_i0 && hunk.line1 >= last_i1 && hunk.deleted > 0 && hunk.inserted > 0) {
                // collect hunks that are not small here to insert for further processing:
                List<IndexLengthPair> newHunks = new ArrayList<IndexLengthPair>();
                int i0 = hunk.line0, i1 = hunk.line1;
                last_i0 = hunk.line0; last_i1 = hunk.line1;
                int start_i1 = hunk.line1; // shorten inner loop after matching small changes
                for (; i0<hunk.line0+hunk.deleted; i0++) {
                    for (i1=start_i1; i1<hunk.line1+hunk.inserted; i1++) {
                        Lexeme lexeme0 = list0.get(i0);
                        Lexeme lexeme1 = list1.get(i1);
                        // small changes := lexemes are not both SPACE and of the same type and
                        //   have a Levenshtein distance less than 3 and less than the length of shorter lexeme
                        // TODO: consider using Damerau-Levenshtein and limit to 1 instead!
                        // see (http://spider.my/static/contrib/Levenshtein.java)
                        if (!(SPACE.contains(lexeme0.type) && SPACE.contains(lexeme1.type)) &&
                                lexeme0.type.equals(lexeme1.type) &&
                                Levenshtein.getLevenshteinDistance(lexeme0.contents, lexeme1.contents) <
                                        Math.min(3, Math.min(lexeme0.contents.length(), lexeme1.contents.length()))) {
                            // small change: determine character diff using arrays of characters from contents
                            Diff chardiff = new Diff(
                                    toCharacters(lexeme0.contents.toCharArray()),
                                    toCharacters(lexeme1.contents.toCharArray()));
                            result.addAll(mergeSmallDiffResult(chardiff.diff_2(false),
                                    lexeme0.contents,
                                    lexeme1,
                                    i1 < preamble1));
                            // prepare new hunks (if needed)
                            if (i0 > last_i0 || i1 > last_i1)
                                newHunks.add(new IndexLengthPair(last_i0, last_i1, i0 - last_i0, i1 - last_i1));
                            last_i0 = i0 + 1;
                            last_i1 = i1 + 1;
                            // continue loops with rest of indices:
                            start_i1 = ++i1;
                            break; // finish inner loop
                        }
                    }
                }
                // link new hunks into old chain
                // if at least one new hunk already or if at least one small change encountered
                if (!newHunks.isEmpty() || last_i0 > hunk.line0 && last_i1 > hunk.line1) {
                    // if needed, add last new hunk:
                    if (last_i0 < hunk.line0+hunk.deleted || last_i1 < hunk.line1+hunk.inserted) {
                        newHunks.add(new IndexLengthPair(last_i0, last_i1,
                                hunk.line0+hunk.deleted - last_i0,
                                hunk.line1+hunk.inserted - last_i1));
                        // remember last position to avoid comparing these hunks again for small changes:
                        last_i0 = hunk.line0+hunk.deleted;
                        last_i1 = hunk.line1+hunk.inserted;
                    }
                    // if still no new hunks, then current hunk consisted completely of small changes
                    // and we need to continue the outer loop
                    if (newHunks.isEmpty())
                        continue;
                    // go through new hunks from last to first and add to old chain:
                    IndexLengthPair indices;
                    Diff.change newLink = hunk.link;
                    for (ListIterator<IndexLengthPair> it = newHunks.listIterator(newHunks.size());
                         it.hasPrevious(); ) {
                        indices = it.previous();
                        newLink = new Diff.change(
                                indices.index0, indices.index1, indices.length0, indices.length1, newLink);
                    }
                    hunk = newLink;
                }
            }

            boolean inPreamble = hunk.line1+hunk.inserted-1 < preamble1;

            // extracting some indices to compare (for determining white space existence):
            int ex0 = calcPosition(list0, hunk.line0+hunk.deleted-1, true);
            int ey0 = calcPosition(list0, hunk.line0+hunk.deleted, false);
            int sx1 = calcPosition(list1, hunk.line1-1, true);
            int sy1 = calcPosition(list1, hunk.line1, false);

            // calc start position for deletions and additions:
            int start_position = sx1;

            // Additions
            if (hunk.inserted > 0) {
                List<IndexPair> indices = getIndices(list1.subList(hunk.line1, hunk.line1+hunk.inserted), hunk.line1);
                for (IndexPair indexPair : indices)
                    result.add(new Addition(
                            indexPair.left == hunk.line1?
                                    start_position:  // if first pair, use calculated start position
                                    calcPosition(list1, indexPair.left-1, true), // if not first pair, use end of prior lexeme
                            indexPair.right == hunk.line1+hunk.inserted?
                                    calcPosition(list1, hunk.line1+hunk.inserted, false): // if last pair, use next lexeme
                                    calcPosition(list1, indexPair.right-1, true), // if not last pair, use end of right lexeme
                            buildFlags(inPreamble, list1.get(indexPair.left).type)
                    ));
            }

            // Deletions
            if (hunk.deleted > 0) {
                List<IndexPair> indices = getIndices(list0.subList(hunk.line0, hunk.line0+hunk.deleted), hunk.line0);
                for (IndexPair indexPair : indices) {
                    // calc text :
                    int text_start = indexPair.left == hunk.line0?
                            calcPosition(list0, hunk.line0-1, true): // if first pair, start with prior lexeme
                            calcPosition(list0, indexPair.left-1, true); // not first pair, use end of prior lexeme
                    int text_end =  indexPair.right == hunk.line0+hunk.deleted?
                            (ex0 != ey0 && sx1 != sy1)?ex0:ey0: // if last pair, then depends on whether
                            // white space at end of deletion in old text AND
                            // white space in front of position in new text
                            calcPosition(list0, indexPair.right-1, true); // if not last pair, use end of right lexeme
                    String text = contents[0].substring(text_start, text_end);
                    result.add(new Deletion(
                            start_position,
                            text,
                            buildFlags(inPreamble, list0.get(indexPair.left).type)));
                    start_position += text.length();
                }
            }
        }

        return new ArrayList<Change>(result);
    }

    // calc position in given lexeme list at index (either beginning or end of lexeme)
    private int calcPosition(List<Lexeme> list, int index, boolean atEnd) {
        if (atEnd)
            return list.get(index).pos+list.get(index).length;
        else
            return list.get(index).pos;
    }

    /**
     * Obtain changes from two texts given as wrapped readers.  The return value is a list of changes ordered 
     * by position in the new text.  If positions are the same, the change is ordered by an order imposed on 
     * the subclasses of Change (see {@link Change.ORDER}).  Finally, we also employ unique sequence numbers
     * for each diff operation, which will be used if the subclasses are of the same class.
     *
     * @param readerWrapper1 text that denotes old version
     * @param readerWrapper2 text that denotes new version
     * @return an ordered list of changes from old to new version
     * @throws IOException if the text cannot be extracted from the wrapped reader
     */
    public List<Change> getChanges(ReaderWrapper readerWrapper1, ReaderWrapper readerWrapper2)
            throws IOException {

        Diff.change changes = createScript(readerWrapper1, readerWrapper2);
        // merge diff result with location information and convert into list of changes
        return mergeDiffResult(changes, lexemLists.get(0), lexemLists.get(1));
    }

    private Diff.change createScript(ReaderWrapper readerWrapper1, ReaderWrapper readerWrapper2) throws IOException {
        // Run lexical analyzer over both files to get lexeme and locations
        lexemLists.clear(); // start with empty lists
        lexemLists.add(analyze(readerWrapper1, false));
        lexemLists.add(analyze(readerWrapper2, false));

        // copy contents
        contents[0] = copyText(readerWrapper1.createReader());
        contents[1] = copyText(readerWrapper2.createReader());

        return diff(lexemLists);
    }

    private Diff.change diff(List<List<Lexeme>> twoListsOfLexemes) {
        // Diff between lexeme (without locations):
        // collect relevant lexemes into arrays
        for (int i=0; i<2; i++) {
            // go through each lexem list and build up string arrays:
            List<Lexeme> lexemes = twoListsOfLexemes.get(i);
            diffInputs[i] = new String[lexemes.size()];
            int j=0;
            for (Lexeme lexeme : lexemes) {
                diffInputs[i][j] = lexeme.type+" "+lexeme.displayContents();
                j++;
            }
        }
        // create and return change script
        return (script = new Diff(diffInputs[0],diffInputs[1]).diff_2(false));
    }

    public static String copyText(Reader reader) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int c;
        while (( c = reader.read()) != -1)
            buffer.append((char) c);
        reader.close();
        return buffer.toString();
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        out.println("usage: java -cp ... com.sri.ltc.latexdiff.LatexDiff [options] FILE1 FILE2 \nwith");
        parser.printUsage(out);
    }

    public static void main(String args[]) {
        // parse arguments
        CmdLineParser.registerHandler(Level.class, LevelOptionHandler.class);
        final LatexDiffOptions options = new LatexDiffOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            printUsage(System.err, parser);
            System.exit(ReturnCodes.PARSING_ARGUMENTS.ordinal());
        }

        if (options.displayHelp) {
            printUsage(System.out, parser);
            System.exit(1);
        }

        try {
            LatexDiff latexDiff = new LatexDiff();
            List<Change> changes = latexDiff.getChanges(
                    new FileReaderWrapper(options.file1),
                    new FileReaderWrapper(options.file2));
            // Convert to output
            if (options.asXML)
                for (Change c : changes)
                    System.out.println(c);
            else
                new LocationPrint(latexDiff.diffInputs[0],latexDiff.diffInputs[1],
                        latexDiff.lexemLists.get(0),latexDiff.lexemLists.get(1)).print_script(latexDiff.script);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(ReturnCodes.FILE_NOT_FOUND.ordinal());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ReturnCodes.SCANNING_ERROR.ordinal());
        }

        System.exit(ReturnCodes.SUCCESS.ordinal());
    }

    private enum ReturnCodes {
        SUCCESS,
        PARSING_ARGUMENTS,
        FILE_NOT_FOUND,
        SCANNING_ERROR,
        UNKNOWN_OPTION;
    }

    private static class LatexDiffOptions {
        @Option(name="-h", usage="display usage and exit")
        boolean displayHelp = false;

        @Option(name="-x", usage="output as XML (default is Unix style)")
        boolean asXML = false;

        @Argument(required=true, index=0, metaVar="FILE1", usage="first file to diff")
        String file1;

        @Argument(required=true, index=1, metaVar="FILE2", usage="second file to diff")
        String file2;
    }
}
