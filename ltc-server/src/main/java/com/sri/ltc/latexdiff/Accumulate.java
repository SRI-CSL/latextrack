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
package com.sri.ltc.latexdiff;

import com.sri.ltc.CommonUtils;
import com.sri.ltc.server.LTCserverInterface;
import org.apache.commons.codec.binary.Base64;

import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.List;

/**
 * @author linda
 */
public final class Accumulate {

    private final static LatexDiff latexDiff = new LatexDiff();

    // in order to keep track of progress
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public final static String PROGRESS_PROPERTY = "float_progress";

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Perform accumulation of changes over the given array of texts.  If given array of texts is empty or <code>null</code>
     * then does nothing and returns a map with empty text and styles.  If given a array of texts has at least one entry,
     * the array of author indices must match up in length or be empty or <code>null</code>.  If there is at least 2 texts
     * to compare but the author indices is empty, this function assigns author indices [1..(n-1)] for n = length of text
     * array.
     * <p>
     * A number of boolean flags indicates whether to show or hide deletions, small changes, changes in preamble,
     * comments, and commands.
     * <p>
     * The returned map contains 3 entries for keys {@link LTCserverInterface.KEY_TEXT},
     * {@link LTCserverInterface.KEY_CARET} and {@link LTCserverInterface.KEY_STYLES}.
     * The value for the first key is the text obtained from accumulating all changes.  The value for the second key
     * is the transformed caret position.  The value for the last key is a list of 4-tuples denoting the mark up for
     * the text.  Each 4-tuple has integers
     * <pre>
     *     [start position (incl.), end position (excl.), addition/deletion, author index]
     * </pre>, which contain the start and end position of the change (positions in text start with 0), a 1 for
     * addition or 2 for deletion, and the index of the author who made this change.  The author index is either taken
     * from the given array or assigned by index of the text array.
     *
     * @param priorText an array of text wrappers denoting the oldest to the newest version
     * @param authorIndices an array of author indices for each version of <code>priorText</code>;
     *                      must be of the same length as <code>priorText</code> or empty or <code>null</code>
     * @param flagsToHide a set of {@link com.sri.ltc.latexdiff.Change.Flag}, which are to be hidden in accumulated markup
     * @param limitedAuthors a set of author indices, to which all markup is limited after accumulated;
     *                       if this is empty or <code>null</code>, then all authors are used
     * @param caretPosition the caret position to be transformed by the accumulation   @return Map with 3 entries pointing to the text, the updated caret position, and the list of styles to mark-up
     * the changes in the text  @throws IOException if given readers cannot load text
     * @throws BadLocationException if a location in the underlying document does not exist
     * @throws IllegalStateException if the given array <code>authorIndices</code> is not empty,
     *                               but its length does not match the one of <code>priorText</code>
     */
    @SuppressWarnings("unchecked")
    public Map perform(ReaderWrapper[] priorText,
                       Integer[] authorIndices,
                       Set<Change.Flag> flagsToHide, Set<Integer> limitedAuthors,
                       int caretPosition) throws Exception {

        // init return value:
        Map map = new HashMap();
        map.put(LTCserverInterface.KEY_TEXT, new byte[0]);
        map.put(LTCserverInterface.KEY_CARET, caretPosition);
        map.put(LTCserverInterface.KEY_STYLES, new ArrayList<Integer[]>());
        map.put(LTCserverInterface.KEY_REV_INDICES, new ArrayList<Integer>());

        if (priorText == null || priorText.length == 0)
            return map;

        // test author array and initialize if needed
        if (authorIndices == null || authorIndices.length == 0) {
            // init with ascending numbers
            authorIndices = new Integer[priorText.length];
            for (int i=0; i < priorText.length; i++)
                authorIndices[i] = i;
        }
        if (authorIndices.length != priorText.length)
            throw new IllegalStateException("author indices is not empty but also not the right size");

        // generate color palette for the largest author index +1, as the indices may start with 0
        int n = new TreeSet<Integer>(Arrays.asList(authorIndices)).last()+1;
        Color[] colors = new Color[n];
        for(int i = 0; i < n; i++)
            colors[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);

        // merge everything into one styled document: init document with latest text
        final MarkedUpDocument document = new MarkedUpDocument();
        document.insertString(0, CommonUtils.copyText(priorText[priorText.length - 1].createReader()), null);

        float progress = 0f; // track progress through the loops below

        // go from latest to earliest version: start with comparing current document with second latest
        float outer_step_increment = 0.9f/(float) (priorText.length - 1); // calculate increment of progress for each outer loop
        for (int index = priorText.length - 1; index > 0; index--) {

            // compare current document with next version
            List<Change> changes = latexDiff.getChanges(
                    priorText[index - 1],
                    new DocumentReaderWrapper(document)); // removes additions from current text but maintains positions

            // prepare styles with color and author index and revision number
            int authorIndex = authorIndices[index];
            document.updateStyles(authorIndex, colors[authorIndex], index - 1);

            int current_offset = 0;

            // go through changes and markup document
            if (changes.isEmpty()) {
                progress = updateProgress(progress, outer_step_increment);
                continue; // skip to next version if no changes
            }

            float inner_step_increment = outer_step_increment/(float) changes.size(); // increment of progress for each inner loop
            for (Change change : changes) {

                if (change instanceof Deletion) {
                    for (IndexFlagsPair<String> pair : ((Deletion) change).flags) {
                        document.insertDeletion(
                                change.start_position + current_offset,
                                pair.index,
                                pair.flags
                        );
                        // update caret position:
                        if (change.start_position + current_offset <= caretPosition)
                            caretPosition += pair.index.length();
                        current_offset += pair.index.length();
                    }
                }

                if (change instanceof Addition) {
                    int start_position = change.start_position + current_offset;
                    for (IndexFlagsPair<Integer> pair : ((Addition) change).flags) {
                        document.markupAddition(
                                start_position,
                                pair.index + current_offset,
                                pair.flags
                        );
                        start_position = pair.index + current_offset; // next fragment starts at current end position
                    }
                }

                progress = updateProgress(progress, inner_step_increment);
            }
        }

        // after changes are accumulated, apply the filters
        caretPosition = document.applyFiltering(flagsToHide, limitedAuthors, caretPosition);
        progress = updateProgress(0.9f, 0.05f);

        // create return value:
        map.put(LTCserverInterface.KEY_TEXT, Base64.encodeBase64(document.getText(0, document.getLength()).getBytes()));
        map.put(LTCserverInterface.KEY_STYLES, document.getStyles());
        map.put(LTCserverInterface.KEY_CARET, caretPosition);
        map.put(LTCserverInterface.KEY_REV_INDICES, document.getSortedRevisionIndices());

        updateProgress(0.95f, 0.05f);
        return map;
    }

    private final float updateProgress(float progress, float increment) {
        pcs.firePropertyChange(PROGRESS_PROPERTY, new Float(progress), new Float(progress+increment));
        return progress+increment;
    }
}
