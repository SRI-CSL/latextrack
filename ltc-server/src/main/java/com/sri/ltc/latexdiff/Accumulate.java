/**
 ************************ 80 columns *******************************************
 * Accumulate
 *
 * Created on May 20, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.google.common.collect.Sets;
import com.sri.ltc.server.LTCserverInterface;

import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author linda
 */
public final class Accumulate {

    // in order to keep track of progress
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public final static String PROGRESS_PROPERTY = "float_progress";

    private final static LatexDiff latexDiff = new LatexDiff();

    private final StyledDocument document;

    private final static String ADDITION_STYLE = "addition";
    private final static String DELETION_STYLE = "deletion";
    public final static String AUTHOR_INDEX = "author index";
    private final static String FLAGS_ATTR = "flag attribute";

    public Accumulate(String initialText) throws BadLocationException {
        this((StyledDocument) null);
        document.insertString(0, initialText, null);
    }

    public Accumulate(StyledDocument document) {
        if (document == null)
            this.document = new DefaultStyledDocument();
        else
            this.document = document;
        // define styles for additions and deletions
        Style style;
        style = this.document.addStyle(ADDITION_STYLE, null);
        StyleConstants.setUnderline(style, true);
        style = this.document.addStyle(DELETION_STYLE, null);
        StyleConstants.setStrikeThrough(style, true);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public String applyRecentEdits(List<Object[]> recentEdits) throws BadLocationException {
        Style deletionStyle = document.getStyle(DELETION_STYLE);
        StyleConstants.setForeground(deletionStyle, Color.black); // color doesn't matter as it will be removed anyway

        // apply recent edits to current document
        for (Object[] edit : recentEdits) {
            if (edit.length != 3)
                throw new RuntimeException("recent edit is not of length 3");
            int offset = Integer.parseInt(edit[1].toString());
            switch (LTCserverInterface.EditType.valueOf(edit[0].toString())) {
                case REMOVE:
                    document.setCharacterAttributes(offset, Integer.parseInt(edit[2].toString()), deletionStyle, true);
                    break;
                case INSERT:
                    document.insertString(offset, edit[2].toString(), null);
                    break;
                case DELETE:
                    document.remove(offset, edit[2].toString().length());
                    break;
            }
        }

        // remove everything that is strike-through (deletion style) before returning text
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < document.getLength(); i++) {
            Object strikethrough = document.getCharacterElement(i).getAttributes().getAttribute(StyleConstants.StrikeThrough);
            if (!(strikethrough instanceof Boolean && (Boolean) strikethrough))
                builder.append(document.getText(i, 1));
        }
        return builder.toString();
    }

    /**
     * Perform accumulation of changes over the given array of texts.  If given array of texts is empty or <code>null</code>
     * then does nothing and returns a map with empty text and styles.  If given a array of texts has at least one entry,
     * the array of author indices must match up in length or be empty or <code>null</code>.  If there is at least 2 texts
     * to compare but the author indices is empty, this function assigns author indices [1..(n-1)] for n = length of text
     * array.
     * <p>
     * A number of boolean OLDflags indicates whether to show or hide deletions, small changes, changes in preamble,
     * comments, and commands.
     * <p>
     * The returned map contains 2 entries for keys {@link LTCserverInterface.KEY_TEXT} and {@link LTCserverInterface.KEY_STYLES}.
     * The value for the first key is the text obtained from accumulating all changes.  The value for the second key
     * is a list of 4-tuples denoting the mark up for the text.  Each 4-tuple has integers
     * <pre>
     *     <start position (incl.), end position (excl.), addition/deletion, author index>
     * </pre>, which contain the start and end position of the change (positions in text start with 0), a 1 for
     * addition or 2 for deletion, and the index of the author who made this change.  The author index is either taken
     * from the given array or assigned by index of the text array.
     * <p>
     * This is the second version of the algorithm.
     * </p>
     * @param priorText an array of text wrappers denoting the oldest to the newest version
     * @param authorIndices an array of author indices for each version of priorText;
     *                      must be of the same length as priorText or empty or <code>null</code>
     * @param flagsToHide a set of {@link Change.Flag}, which are to be hidden in accumulated markup
     * @return Map with 2 entries pointing to the text and the list of styles to mark-up the changes in the text
     * @throws IOException if given readers cannot load text
     * @throws BadLocationException if a location in the underlying document does not exist
     */
    @SuppressWarnings("unchecked")
    public Map perform2(ReaderWrapper[] priorText,
                        Integer[] authorIndices,
                        Set<Change.Flag> flagsToHide) throws IOException, BadLocationException {

        // init return value:
        Map map = new HashMap();
        map.put(LTCserverInterface.KEY_TEXT, "");
        map.put(LTCserverInterface.KEY_STYLES, new ArrayList<Integer[]>());

        if (priorText == null || priorText.length == 0)
            return map;

        // generate color palette
        int n = Math.max(priorText.length+1,
                (authorIndices == null || authorIndices.length == 0)?0:new TreeSet<Integer>(Arrays.asList(authorIndices)).last()+1);
        Color[] colors = new Color[n];
        for(int i = 0; i < n; i++)
            colors[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);

        // merge everything into one styled document: init document with latest text
        document.remove(0, document.getLength());
        document.insertString(0, LatexDiff.copyText(priorText[priorText.length - 1].createReader()), null);

        float progress = 0f; // track progress through the 2 loops below

        // go from latest to earliest version: start with comparing current document with second latest
        float outer_step_increment = 0.9f/(float) (priorText.length - 1); // calculate increment of progress for each outer loop
        for (int index = priorText.length - 1; index > 0; index--) {

            // compare current document with next version
            List<Change> changes = latexDiff.getChanges(
                    priorText[index - 1],
                    new StringReaderWrapper(document.getText(0, document.getLength())));
            if (changes.isEmpty())
                continue; // skip to next version if no changes

            // prepare styles with color and author index
            Style style;
            int authorIndex = (authorIndices == null || authorIndices.length != priorText.length)?
                    index:authorIndices[index];
            style = document.getStyle(DELETION_STYLE);
            StyleConstants.setForeground(style, colors[authorIndex]);
            style.addAttribute(AUTHOR_INDEX, authorIndex);
            style = document.getStyle(ADDITION_STYLE);
            StyleConstants.setForeground(style, colors[authorIndex]);
            style.addAttribute(AUTHOR_INDEX, authorIndex);

            int current_offset = 0;

            // go through changes and markup document
            float inner_step_increment = outer_step_increment/(float) changes.size(); // increment of progress for each inner loop
            for (Change change : changes) {

                if (change instanceof Deletion) {
                    for (IndexFlagsPair<String> pair : ((Deletion) change).getFlags()) {
                        style = document.getStyle(DELETION_STYLE);
                        style.addAttribute(FLAGS_ATTR, pair.flags);
                        document.insertString(
                                change.start_position + current_offset,
                                pair.index,
                                style);
                        current_offset += pair.index.length();
                    }
                }

                if (change instanceof Addition) {
                    int start_position = change.start_position + current_offset;
                    for (IndexFlagsPair<Integer> pair : ((Addition) change).getFlags()) {
                        // markup everything between start_position and end_position except
                        // currently marked ADDITIONS and DELETIONS:
                        for (int i = start_position; i < pair.index + current_offset; i++) {
                            Object styleName = document.getCharacterElement(i).getAttributes().getAttribute(StyleConstants.NameAttribute);
                            if (!DELETION_STYLE.equals(styleName) && !ADDITION_STYLE.equals(styleName)) {
                                style = document.getStyle(ADDITION_STYLE);
                                style.addAttribute(FLAGS_ATTR, pair.flags);
                                document.setCharacterAttributes(i, 1, style, true);
                            }
                        }
                        start_position = pair.index + current_offset; // next fragment starts at current end position
                    }
                }

                progress = updateProgress(progress, inner_step_increment);
            }
        }

        // apply filtering to marked up text
        if (!flagsToHide.isEmpty()) {
            for (int i = 0; i < document.getLength(); i++) {
                Set<Change.Flag> flags = (Set<Change.Flag>) document.getCharacterElement(i).getAttributes().getAttribute(FLAGS_ATTR);
                if (flags != null) {
                    Set<Change.Flag> intersection = Sets.intersection(flags, flagsToHide);
                    if (!intersection.isEmpty()) { // matching OLDflags
                        if (flags.contains((Change.Flag.DELETION))) { // change was a deletion, so remove character
                            document.remove(i, 1);
                            i--;
                        } else { // change was an addition, so remove all attributes
                            document.setCharacterAttributes(i, 1, SimpleAttributeSet.EMPTY, true);
                        }
                    }
                }
            }
        }
        progress = updateProgress(0.9f, 0.05f);

        // create return value:
        map.put(LTCserverInterface.KEY_TEXT, document.getText(0, document.getLength()));
        List<Integer[]> list = new ArrayList<Integer[]>();
        Chunk c = traverse(document.getDefaultRootElement(), null, list);
        // handle last chunk and add it if not default style:
        c.end = document.getLength()+1;
        if (!"default".equals(c.style))
            list.add(c.asList());
        map.put(LTCserverInterface.KEY_STYLES, list);

        updateProgress(0.95f, 0.05f);
        return map;
    }

    private final float updateProgress(float progress, float increment) {
        pcs.firePropertyChange(PROGRESS_PROPERTY, new Float(progress), new Float(progress+increment));
        return progress+increment;
    }

    private Chunk traverse(Element element, Chunk last, List<Integer[]> list) {
        Chunk chunk = last;
        if (element.isLeaf()) {
            AbstractDocument.LeafElement leaf = (AbstractDocument.LeafElement) element;
            chunk = new Chunk(element.getStartOffset(),
                    (String) leaf.getAttribute(StyleConstants.NameAttribute),
                    (Color) leaf.getAttribute(StyleConstants.Foreground),
                    (Integer) leaf.getAttribute(AUTHOR_INDEX));
            if (chunk.equals(last))
                return last;
            else {
                if (last != null) {
                    last.end = element.getStartOffset();
                    if (!"default".equals(last.style))
                        list.add(last.asList());
                }
                return chunk;
            }
        } else {
            for (int i=0; i < element.getElementCount(); i++) {
                chunk = traverse(element.getElement(i), chunk, list);
            }
        }
        return chunk;
    }

    private final class Chunk {
        final int start;
        int end;
        final String style;
        final Color color;
        final Integer author;

        private Chunk(int start, String style, Color color, Integer author) {
            this.start = start;
            this.style = style;
            this.color = color;
            this.author = author;
        }

        private Integer[] asList() {
            Integer[] values = {start, end, 0, 0};
            if (ADDITION_STYLE.equals(style))
                values[2] = 1;
            if (DELETION_STYLE.equals(style))
                values[2] = 2;
            if (author != null)
                values[3] = author;
            return values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Chunk chunk = (Chunk) o;

            if (author != null ? !author.equals(chunk.author) : chunk.author != null) return false;
            if (color != null ? !color.equals(chunk.color) : chunk.color != null) return false;
            if (style != null ? !style.equals(chunk.style) : chunk.style != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = style != null ? style.hashCode() : 0;
            result = 31 * result + (color != null ? color.hashCode() : 0);
            result = 31 * result + (author != null ? author.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "<chunk start="+start+
                    " end="+end+
                    " style=\""+style+"\""+
                    " author="+author+
                    " color=\""+color+"\" />";
        }
    }
}
