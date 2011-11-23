/**
 ************************ 80 columns *******************************************
 * Accumulate
 *
 * Created on May 20, 2010.
 *
 * Copyright 2009-2010, SRI International.
 */
package com.sri.ltc.latexdiff;

import com.bmsi.gnudiff.Diff;
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

    private final StyledDocument document;

    private final static String ADDITION_STYLE = "addition";
    private final static String DELETION_STYLE = "deletion";
    public final static String AUTHOR_INDEX = "author index";

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
        // reset sequence numbering for changes
        Change.resetSequenceNumbering();
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

    @SuppressWarnings("unchecked")
    public Map perform(ReaderWrapper[] priorText,
                       Integer[] authorIndices,
                       boolean showDeletions,
                       boolean showSmallChanges,
                       boolean showPreambleChanges,
                       boolean showCommentChanges,
                       boolean showCommandChanges) throws IOException, BadLocationException {

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

        float progress = 0f;
        List<List<Change>> changes = new ArrayList<List<Change>>();
        // compare oldest version with next and so on:
        float step_increment = 0.5f/(float) (priorText.length - 1);
        for (int i=0; i<(priorText.length-1); i++) {
            changes.add(new LatexDiff().getChanges(priorText[i],priorText[i+1]));
            pcs.firePropertyChange(PROGRESS_PROPERTY, new Float(progress), new Float(progress+step_increment));
            progress += step_increment;
        }

        // merge everything into one styled document: init document with latest text
        document.remove(0, document.getLength());
        document.insertString(0, LatexDiff.copyText(priorText[priorText.length - 1].createReader()), null);

        Style style;
        SortedMap<PositionRange, Integer> translocations = new TreeMap<PositionRange, Integer>();

        // go through each comparison from newest to oldest to markup changes
        step_increment = 0.5f/(float) changes.size();
        for (int index=changes.size()-1; index>=0; index--) {

            // prepare styles with color and author index
            int authorIndex = (authorIndices == null || authorIndices.length != priorText.length)?
                    index+1:
                    authorIndices[index+1];
            style = document.getStyle(DELETION_STYLE);
            StyleConstants.setForeground(style, colors[authorIndex]);
            style.addAttribute(AUTHOR_INDEX, authorIndex);
            style = document.getStyle(ADDITION_STYLE);
            StyleConstants.setForeground(style, colors[authorIndex]);
            style.addAttribute(AUTHOR_INDEX, authorIndex);

            int current_offset = 0;

            // go through all changes of this version
            for (Change change : changes.get(index)) {

                // ignore certain changes:
                if (!(change instanceof Deletion || change instanceof Addition))
                    continue;
                if (!showPreambleChanges && change.inPreamble)
                    continue;
                if (!showCommentChanges && change.inComment)
                    continue;
                if (!showCommandChanges && change.isCommand)
                    continue;

                // translocate start position and apply current offset
                int start_position = applyTranslocation(change.start_position, translocations);
                start_position += current_offset;

                if (change instanceof Deletion && showDeletions &&
                        (showSmallChanges || !(change instanceof SmallDeletion))) {
                    document.insertString(start_position,
                            ((Deletion) change).text,
                            document.getStyle(DELETION_STYLE));
                    current_offset += ((Deletion) change).text.length();
                }

                if (change instanceof Addition &&
                        (showSmallChanges || !(change instanceof SmallAddition))) {
                    // show small additions only if set to do so
                    markupAddition(start_position, ((Addition) change).text, ((Addition) change).lexemes);
                }
            }

            // compare prior text (index) to current text for translocations of the next loop
            if (index > 0)
                translocations = buildTranslocationMap(new LatexDiff().getTranslocations(
                        priorText[index],
                        new StringReaderWrapper(document.getText(0,document.getLength()))));

            // update progress counter
            pcs.firePropertyChange(PROGRESS_PROPERTY, new Float(progress), new Float(progress+step_increment));
            progress += step_increment;            
        }

        // create return value:
        map.put(LTCserverInterface.KEY_TEXT, document.getText(0, document.getLength()));
        List<Integer[]> list = new ArrayList<Integer[]>();
        Chunk c = traverse(document.getDefaultRootElement(), null, list);
        // handle last chunk and add it if not default style:
        c.end = document.getLength()+1;
        if (!"default".equals(c.style))
            list.add(c.asList());
        map.put(LTCserverInterface.KEY_STYLES, list);

        pcs.firePropertyChange(PROGRESS_PROPERTY, new Float(progress), new Float(1f));
        return map;
    }

    private SortedMap<PositionRange,Integer> buildTranslocationMap(List<? extends Change> changes) {
        // go through all changes of this version and collect translocations in map
        // if 2 translocations with same start position, take the latter (and remove former)
        SortedMap<PositionRange,Integer> translocationMap = new TreeMap<PositionRange,Integer>();
        Translocation last_translocation = new Translocation(); // identity function at beginning
        for (Change c : changes) {
            if (c instanceof Translocation) {
                Translocation current_translocation = (Translocation) c;
                if (last_translocation.start_position != current_translocation.start_position) {
                    // simply add new entry
                    translocationMap.put(
                            new PositionRange(
                                    last_translocation.start_position,
                                    current_translocation.start_position),
                            last_translocation.position_offset);
                }
                last_translocation = (Translocation) c;
            }
        }
        // finish with last element of map
        translocationMap.put(
                new PositionRange(last_translocation.start_position, null),
                last_translocation.position_offset);
        return translocationMap;
    }

    private int applyTranslocation(int position, SortedMap<PositionRange,Integer> translocations) {
        // find applicable translocation of current position in that generation
        for (Map.Entry<PositionRange,Integer> entry : translocations.entrySet()) {
            if (entry.getKey().inRange(position)) {
                position += entry.getValue();
                break;
            }
        }
        return position;
    }

    private void markupAddition(int start_position, String text, List<Lexeme> lexemes)
            throws BadLocationException, IOException {
        if (document.getLength() > start_position) {
            // get current text to match against addition
            String current_text = document.getText(
                    start_position,
                    document.getLength()-start_position);
            int end_position = -1;
            if (lexemes.size() > 0) {
                // perform lexical analysis of current text to match against addition's lexemes
                LatexDiff latexDiff = new LatexDiff();
                List<Lexeme> current_lexemes = latexDiff.analyze(
                        new StringReaderWrapper(current_text),
                        LexemeType.COMMENT.equals(lexemes.get(0).type));
                List<List<Lexeme>> twoLexemeLists = new ArrayList<List<Lexeme>>();
                twoLexemeLists.add(lexemes);
                twoLexemeLists.add(current_lexemes);
                // figure out last matching lexeme to get end position of addition:
                int index1 = -1;
                for (Diff.change hunk = latexDiff.diff(twoLexemeLists); hunk != null; hunk = hunk.link)
                    index1 = hunk.line1 - 1;
                if (index1 >= 0 && index1 < current_lexemes.size())
                    end_position = start_position + current_lexemes.get(index1).pos+current_lexemes.get(index1).length;
            } else {
                // handle small additions
                String current = current_text.substring(0, Math.min(current_text.length(),text.length()));
                if (current.equals(text))
                    end_position = start_position + current.length();
            }
            // markup everything between start_position and end_position except
            // currently marked ADDITIONS and DELETIONS:
            for (int i=start_position; i < end_position; i++) {
                Object style = document.getCharacterElement(i).getAttributes().getAttribute(StyleConstants.NameAttribute);
                if (!DELETION_STYLE.equals(style) && !ADDITION_STYLE.equals(style))
                    document.setCharacterAttributes(i, 1, document.getStyle(ADDITION_STYLE), true);
            }
        }
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
