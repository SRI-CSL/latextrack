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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

    private static final Set<LexemeType> SPACE = Sets.newHashSet(
            LexemeType.PARAGRAPH,
            LexemeType.WHITESPACE);

    // variables to contain parts for diff'ing
    private List<List<Lexeme>> lexemLists = new ArrayList<List<Lexeme>>();
    private String[][] diffInputs = new String[3][];
    Diff.change script;

    // TODO: make this private (and unit test via reflection?)
    /**
     * Perform lexical analysis on the text given as a wrapper of a reader.
     * Remove whitespace in the whole text and paragraphs in the preamble.
     * Also removes any lexemes that are marked as additions if the wrapper changes them into NULL.
     * Closes the reader after analysis.
     * <p>
     * The return value is a pair of the resulting list of lexemes and an
     * index of the preamble, even if the preamble was removed during analysis
     * due to being marked as an addition.  If the preamble index is -1, then
     * no preamble was seen.
     *
     *
     * @param wrapper Wrapper of reader pointing to text to be analyzed
     * @return List of Lexemes obtained from analysis (never null) and preamble index.
     * @throws IOException if the scanner encounters an IOException
     */
    public List<Lexeme> analyze(ReaderWrapper wrapper) throws IOException {
        List<Lexeme> list = new ArrayList<Lexeme>(Arrays.asList(new Lexeme[] {
                new Lexeme(LexemeType.START_OF_FILE, "", 0, false, false)}));
        Lexer scanner = new Lexer(wrapper.createReader());
        List<Lexeme> lexemes;
        while ((lexemes = scanner.yylex()) != null)
            for (Lexeme lexeme : lexemes)
                if (!LexemeType.WHITESPACE.equals(lexeme.type)) { // ignore whitespace
                    lexeme = wrapper.removeAdditions(lexeme); // remove any additions
                    if (lexeme != null)
                        list.add(lexeme);
                }
        scanner.yyclose();
        // remove paragraphs in the preamble if there is one:
        if (list.get(list.size()-1).preambleSeen) { // EOF Lexeme has seen preamble
            for (Iterator<Lexeme> i = list.iterator(); i.hasNext(); ) {
                Lexeme lexeme = i.next();
                if (lexeme.preambleSeen)
                    break; // done with preamble
                if (LexemeType.PARAGRAPH.equals(lexeme.type))
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

    private EnumSet<Change.Flag> buildFlags(boolean isDeletion, boolean isSmall, boolean inPreamble, boolean inComment, boolean isCommand) {
        EnumSet<Change.Flag> flags = EnumSet.noneOf(Change.Flag.class);
        if (isDeletion) flags.add(Change.Flag.DELETION);
        if (isSmall) flags.add(Change.Flag.SMALL);
        if (inPreamble) flags.add(Change.Flag.PREAMBLE);
        if (inComment) flags.add(Change.Flag.COMMENT);
        if (isCommand) flags.add(Change.Flag.COMMAND);
        return flags;
    }

    private EnumSet<Change.Flag> buildFlags(boolean isDeletion, boolean inComment, Lexeme lexeme, boolean preamblePresent) {
        return buildFlags(isDeletion, false,
                preamblePresent && !lexeme.preambleSeen,
                inComment,
                LexemeType.COMMAND.equals(lexeme.type));
    }

    @SuppressWarnings("unchecked")
    private SortedSet<Change> mergeSmallDiffResult(Diff.change changes, String text0, Lexeme lexeme1, boolean inPreamble) {
        SortedSet<Change> result = new TreeSet<Change>();

        Queue<Integer> removed = Lists.newLinkedList(Lists.newArrayList(lexeme1.removed));
        int offset = 0; // keep track of current offset to be applied to transforming positions

        // go through linked list of changes
        for (Diff.change hunk = changes; hunk != null; hunk = hunk.link) {
            if (hunk.deleted == 0 && hunk.inserted == 0)
                continue;

            // transform hunk.line1 into position taking 'removed' into account
            while (removed.peek() != null && hunk.line1 + offset >= removed.peek()) {
                Integer head;
                int n_removed = 0;
                do {
                    n_removed++;
                    head = removed.poll();
                } while (head != null && removed.peek() != null && (head + 1) == removed.peek());
                offset += n_removed;
            }

            int start_position = lexeme1.pos + hunk.line1 + offset;

            // Additions
            if (hunk.inserted > 0) {
                result.add(new Addition(
                        start_position,
                        Arrays.asList(new IndexFlagsPair<Integer>(
                                start_position + hunk.inserted,
                                buildFlags(false, true, inPreamble, lexeme1.inComment, LexemeType.COMMAND.equals(lexeme1.type))))));
            }

            // Deletions
            if (hunk.deleted > 0) {
                result.add(new Deletion(
                        start_position,
                        Arrays.asList(new IndexFlagsPair<String>(
                                text0.substring(hunk.line0, hunk.line0+hunk.deleted),
                                buildFlags(true, true, inPreamble, lexeme1.inComment, LexemeType.COMMAND.equals(lexeme1.type))))));
            }
        }

        return result;
    }

    private List<Change> mergeDiffResult(Diff.change changes, List<Lexeme> list0, List<Lexeme> list1,
                                         String contents0) {
        SortedSet<Change> result = new TreeSet<Change>();
        boolean preambleSeen = list1.get(list1.size()-1).preambleSeen; // whether list1 has seen preamble

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
                                    preambleSeen && !lexeme1.preambleSeen));
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
                    Diff.change newLink = hunk.link;
                    ListIterator<IndexLengthPair> it = newHunks.listIterator(newHunks.size());
                    while (it.hasPrevious())
                        newLink = it.previous().createHunk(newLink);
                    hunk = newLink;
                }
            }

            // extracting some indices to compare (for determining white space existence):
            int ex0 = calcPosition(list0, hunk.line0+hunk.deleted-1, true);
            int ey0 = calcPosition(list0, hunk.line0+hunk.deleted, false);
            int sx1 = calcPosition(list1, hunk.line1-1, true);
            int sy1 = calcPosition(list1, hunk.line1, false);

            // calc start position for deletions and additions:
            int start_position = sx1;

            // Additions
            if (hunk.inserted > 0) {
                // build list of flags:
                List<IndexFlagsPair<Integer>> flags = new ArrayList<IndexFlagsPair<Integer>>();
                List<IndexPair> indices = getIndices(list1.subList(hunk.line1, hunk.line1+hunk.inserted), preambleSeen, hunk.line1, false, false);
                for (IndexPair indexPair : indices) {
                    if (indexPair.left.equals(indexPair.right)) { // extra pair to indicate change in flags
                        int ix = calcPosition(list1, indexPair.left-1, true);
                        int iy = calcPosition(list1, indexPair.right, false);
                        if (iy > ix) // only if there is actually space in between
                            flags.add(new IndexFlagsPair<Integer>(iy, indexPair.flags));
                    } else { // regular index pair with lexemes:
                        if (indexPair.addRearSpace)
                            flags.add(new IndexFlagsPair<Integer>(
                                    calcPosition(list1, indexPair.right, false), // pos of next lexeme
                                    indexPair.flags));
                        else
                            flags.add(new IndexFlagsPair<Integer>(
                                    calcPosition(list1, indexPair.right-1, true), // end of last lexeme in this region
                                    indexPair.flags));
                    }
                }
                result.add(new Addition(start_position, flags));
            }

            // Deletions
            if (hunk.deleted > 0) {
                int text_start = calcPosition(list0, hunk.line0-1, true); // start with end of prior lexeme
                int text_end;
                // calculating end position:
                // if last pair, then depends on whether
                // white space at end of deletion in old text AND
                // white space in front of position in new text
                int text_end_position = (ex0 != ey0 && sx1 != sy1)?ex0:ey0;
                // add one space after deletion if replacement without bordering space and next lexeme is a WORD:
                boolean addSpace = (hunk.inserted > 0 &&
                        LexemeType.WORD.equals(list1.get(hunk.line1).type) &&
                        ex0 == ey0 && sx1 == sy1);
                // build list of flags:
                List<IndexFlagsPair<String>> flags = new ArrayList<IndexFlagsPair<String>>();
                List<IndexPair> indices = getIndices(list0.subList(hunk.line0, hunk.line0+hunk.deleted), preambleSeen, hunk.line0,
                        true, list1.get(hunk.line1).inComment); // deletions are in comment if the position in new text is in comment
                for (IndexPair indexPair : indices) {
                    if (indexPair.left.equals(indexPair.right)) { // extra pair to indicate change in flags
                        int ix = calcPosition(list0, indexPair.left-1, true);
                        text_end = calcPosition(list0, indexPair.right, false);
                        if (text_end > ix) // only if there is actually space in between
                            flags.add(new IndexFlagsPair<String>(
                                    contents0.substring(ix, text_end),
                                    indexPair.flags));
                    } else { // regular index pair with lexemes:
                        // calc text:
                        if (indexPair.addRearSpace)
                            text_end = indexPair.right == hunk.line0+hunk.deleted?
                                    text_end_position: // last region, so use calculated end position
                                    calcPosition(list0, indexPair.right, false); // pos of next lexeme
                        else
                            text_end = calcPosition(list0, indexPair.right-1, true); // end of last lexeme in this region
                        String text = contents0.substring(text_start, text_end) +
                                ((addSpace && indexPair.right == hunk.line0+hunk.deleted)?
                                        " ": // last pair and we need to add 1 space
                                        "");
                        flags.add(new IndexFlagsPair<String>(text, indexPair.flags));
                    }
                    text_start = text_end;
                }
                result.add(new Deletion(start_position, flags));
            }
        }

        return new ArrayList<Change>(result);
    }

    // calculate pairs of indices that indicate successive lexemes in given input list
    // with the same settings for PREAMBLE, COMMENT, and COMMAND.
    // also evaluates the difference between inner regions:
    // if the intersection is neither the left nor the right set of flags, add additional region of length = 0
    private List<IndexPair> getIndices(List<Lexeme> list, boolean preamblePresent, int offset, boolean isDeletion, boolean inComment) {
        if (list == null || list.isEmpty())
            return Lists.newArrayList();
        SortedSet<IndexPair> result = new TreeSet<IndexPair>();
        // go through list and collect pairs of indices for regions with the same flags:
        int lastIndex = 0;
        Set<Change.Flag> lastFlags = buildFlags(isDeletion,
                (isDeletion?inComment:list.get(0).inComment),
                list.get(0), preamblePresent);
        for (int i = 1; i < list.size(); i++) {
            Set<Change.Flag> currentFlags = buildFlags(isDeletion,
                    (isDeletion?inComment:list.get(i).inComment),
                    list.get(i), preamblePresent);
            if (!lastFlags.equals(currentFlags)) {
                // evaluate difference to next region:
                Set<Change.Flag> intersection = Sets.intersection(lastFlags, currentFlags);
                if (intersection.equals(lastFlags)) {
                    result.add(new IndexPair(lastIndex + offset, i + offset, true, lastFlags));
                } else {
                    if (!intersection.equals(currentFlags)) {
                        // change in flags that cannot be reconciled: add extra index pair
                        result.add(new IndexPair(i + offset, i + offset, false, intersection));
                    }
                    result.add(new IndexPair(lastIndex + offset, i + offset, false, lastFlags));
                }
                // the following will be sorted before any extra index pairs:
                lastIndex = i;
                lastFlags = currentFlags;
            }
        }
        // add last pair
        result.add(new IndexPair(lastIndex + offset, list.size() + offset, true, lastFlags)); // last one often extends to next lexeme
        return new ArrayList<IndexPair>(result);
    }

    // calc position in given lexeme list at index (either beginning or end of lexeme)
    private int calcPosition(List<Lexeme> list, int index, boolean atEnd) {
        if (atEnd)
            return list.get(index).pos+list.get(index).length;
        else
            return list.get(index).pos;
    }

    private Diff.change removeNewlines(Diff.change hunk, List<Lexeme> list, boolean isInsertion) {
        boolean adjusted = false;
        int newIndex = isInsertion?hunk.line1:hunk.line0;
        int newLength = isInsertion?hunk.inserted:hunk.deleted;
        int end = newIndex + newLength;
        for (int i = newIndex; i < end; i++)
            if (LexemeType.NEWLINE.equals(list.get(i).type)) {
                adjusted = true;
                newLength--; // reduce length
                if (i == newIndex) // if at beginning, increase index as well
                    newIndex++;
            }
        if (adjusted) { // something has changed so create a new hunk in:
            if (isInsertion)
                return new Diff.change(hunk.line0, newIndex, hunk.deleted, newLength, hunk.link);
            else
                return new Diff.change(newIndex, hunk.line1, newLength, hunk.inserted, hunk.link);
        } else
            return hunk;
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
    public synchronized List<Change> getChanges(ReaderWrapper readerWrapper1, ReaderWrapper readerWrapper2)
            throws IOException {

        // Run lexical analyzer over both files to get lexeme and locations
        lexemLists.clear(); // start with empty lists
        lexemLists.add(analyze(readerWrapper1));
        lexemLists.add(analyze(readerWrapper2));

        // Diff between lexeme (without locations):
        // collect relevant lexemes into arrays
        for (int i=0; i<2; i++) {
            // go through each lexem list and build up string arrays:
            List<Lexeme> lexemes = lexemLists.get(i);
            diffInputs[i] = new String[lexemes.size()];
            int j=0;
            for (Lexeme lexeme : lexemes) {
                diffInputs[i][j] = lexeme.type+" "+lexeme.displayContents();
                j++;
            }
        }
        // create and save change script
        script = new Diff(diffInputs[0],diffInputs[1]).diff_2(false);

        // remove NEWLINE lexemes from script:
        if (script != null) {
            // collect new hunks in list
            List<IndexLengthPair> newHunks = new ArrayList<IndexLengthPair>();
            for (Diff.change hunk = script; hunk != null; hunk = hunk.link) {
                // remove any NEWLINE lexemes from hunk
                hunk = removeNewlines(hunk, lexemLists.get(0), false); // any deletions?
                hunk = removeNewlines(hunk, lexemLists.get(1), true); // any additions?
                newHunks.add(new IndexLengthPair(hunk));
            }
            // build new script by going from last to first new hunk:
            Diff.change newLink = null;
            ListIterator<IndexLengthPair> it = newHunks.listIterator(newHunks.size());
            while (it.hasPrevious())
                newLink = it.previous().createHunk(newLink);
            script = newLink;
        }

        // merge diff result with location information and convert into list of changes
        return mergeDiffResult(script,
                lexemLists.get(0), lexemLists.get(1),
                copyText(readerWrapper1.createReader()));
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
