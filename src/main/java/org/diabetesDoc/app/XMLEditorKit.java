package org.diabetesDoc.app;

////////////////////////////////////////////////////////////////////////////////
//
// This file is part of DiabetesDoc.
//
//   Copyright 2017 Stephan Lunowa
//
// DiabetesDoc is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// DiabetesDoc is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with DiabetesDoc. If not, see <http://www.gnu.org/licenses/>.
//
////////////////////////////////////////////////////////////////////////////////

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.*;
import javax.swing.UIManager;

/**
 * This Class is an {@code EditorKit} for XML-files.
 * The text is highlighted by the Class {@link XMLView}.
 * @author Stephan Lunowa
 * @version 2.1 - last modified 2014-03-17
 */
class XMLEditorKit extends StyledEditorKit implements ViewFactory {
	/** @see Serializable */
    private static final long serialVersionUID = 1L;

    @Override
    public ViewFactory getViewFactory() {
        return this;
    }

   @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public View create(Element element) {
        return new XMLView(element);
    }

    /**
     * This Class manages the View for a {@code JTextComponent} as XML-data.
     * Only one-line detection possible.
     * @author Stephan Lunowa
     * @version 2.1 - last modified 2014-03-12
     */
    private static class XMLView extends PlainView {
    	/** The Colors for the different patterns. */
    	private static HashMap<Pattern, Color> patternColors = new HashMap<Pattern, Color>();

    	/** The pattern for XML-names. */
    	private static String XML_NAME = "[A-Za-z]+[\\w\\d\\-]*";
    	/** The pattern for generic XML-names. */
    	private static String GENERIC_XML_NAME = XML_NAME + "(:" + XML_NAME + ")?";
    	/** The pattern for tags. */
    	private static String TAG_PATTERN = "(\\</?" + GENERIC_XML_NAME + "\\s*\\>?)";
    	/** The pattern for tag-ends. */
    	private static String TAG_END_PATTERN = "(/>)";
    	/** The pattern for attributes in tags. */
    	//private static String TAG_ATTRIBUTE_PATTERN = "(" + GENERIC_XML_NAME + ")\\=";
    	/** The pattern for attribute-values in tags. */
    	private static String TAG_ATTRIBUTE_VALUE = "\\w*\\=\\w*((\"[^\"]*\")|('[^']*'))";

    	/**
    	 * Initializes the colors for the patterns.
    	 */
    	static {
    		patternColors.put(Pattern.compile(TAG_PATTERN), Color.BLUE);
    		//patternColors.put(Pattern.compile(TAG_ATTRIBUTE_PATTERN), Color.BLACK);
    		patternColors.put(Pattern.compile(TAG_END_PATTERN), Color.BLUE);
    		patternColors.put(Pattern.compile(TAG_ATTRIBUTE_VALUE), Color.GREEN);

    	}

    	public XMLView(Element element) {
    		super(element);

    		// Set tabsize to 4 (instead of the default 8)
    		getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
    	}

    	/**
    	 * Draws the unselected Text using the patterns to determine the texts color.
    	 * <p>
    	 * <b>Original specification:</b> {@inheritDoc}
    	 */
    	@Override
    	protected int drawUnselectedText(Graphics graphics, int x, int y, int p0,
    			int p1) throws BadLocationException {

    		Document doc = getDocument();
    		String text = doc.getText(p0, p1 - p0);

    		Segment segment = getLineBuffer();

    		SortedMap<Integer, Integer> startMap = new TreeMap<Integer, Integer>();
    		SortedMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();

    		// Match all regexes on this snippet, store positions
    		for (Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {

    			Matcher matcher = entry.getKey().matcher(text);

    			while (matcher.find()) {
    				startMap.put(matcher.start(1), matcher.end());
    				colorMap.put(matcher.start(1), entry.getValue());
    			}
    		}

    		// TODO: check the map for overlapping parts

    		int i = 0;
    		Color defaultColor = UIManager.getColor("Panel.foreground");
    		// Color the parts
    		for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
    			int start = entry.getKey();
    			int end = entry.getValue();

    			// Paint possible text in the front
    		   if (i < start) {
    				graphics.setColor(defaultColor);
    				doc.getText(p0 + i, start - i, segment);
    				x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
    			}

    			graphics.setColor(colorMap.get(start));
    			i = end;
    			doc.getText(p0 + start, i - start, segment);
    			x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
    		}

    		// Paint possible remaining text
    		if (i < text.length()) {
    			graphics.setColor(defaultColor);
    			doc.getText(p0 + i, text.length() - i, segment);
    			x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
    		}

    		return x;
    	}
    }

}
