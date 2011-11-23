package articles.showpar;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.Position;
import java.awt.*;

public class ShowParLabelView extends LabelView {
    public ShowParLabelView(Element elem) {
        super(elem);
    }
    public void paint(Graphics g, Shape a) {
        super.paint(g, a);

        if (getDocument().getProperty("show paragraphs")!=null) {
            try {
                Rectangle r=a instanceof Rectangle ? (Rectangle)a : a.getBounds();
                String labelStr=getDocument().getText(getStartOffset(), getEndOffset()-getStartOffset());
                int x0=modelToView(getStartOffset(), new Rectangle(r.width, r.height), Position.Bias.Forward).getBounds().x;
                int y = r.y + r.height - (int) getGlyphPainter().getDescent(this);                
                for (int i=0; i<labelStr.length(); i++) {
                    int x=modelToView(i+getStartOffset(), new Rectangle(r.width, r.height), Position.Bias.Forward).getBounds().x-x0;
                    char c=labelStr.charAt(i);
                    int sumOfTabs = 0;                    
                    if (c=='\n') {
                        String s="\u00B6";
                        g.setFont(getFont());
                        int w=g.getFontMetrics().stringWidth(s);
                        Rectangle clip = new Rectangle(r.x+x, r.y, 2*w, r.height);
                        Shape oldClip = g.getClip();
                        g.setClip(clip);
                        g.drawString(s, r.x + x, r.y + g.getFontMetrics().getMaxAscent());
                        renderUnderlineOrStrike(g, r.x + x, y, w);
                        g.setClip(oldClip);
                    }
                    else if (c=='\r') {
                        int w=5;
                        Rectangle clip = new Rectangle(r.x+x, r.y, 2*w, r.height);
                        Shape oldClip = g.getClip();
                        g.setClip(clip);
                        g.drawLine(r.x + x, r.y+r.height/2, r.x +x+ w, r.y+r.height/2);
                        g.drawLine(r.x + x, r.y+r.height/2, r.x +x+ 3, r.y+r.height/2+3);
                        g.drawLine(r.x + x, r.y+r.height/2, r.x +x+ 3, r.y+r.height/2-3);
                        g.drawLine(r.x + x+w, r.y+r.height/2, r.x + x+w, r.y+2);
//                        renderUnderlineOrStrike(g, r.x + x, y, 2*w);
                        g.setClip(oldClip);
                    }
                    else if (c=='\t') {
                        int tabWidth = (int) getTabExpander().nextTabStop((float) r.x + x, i) - x - r.x;                        
                        int x2=modelToView(i+1+getStartOffset(), new Rectangle(r), Position.Bias.Forward).getBounds().x;
                        int w = (int) getGlyphPainter().getSpan(this, getStartOffset() + i, getStartOffset() + i + 1, getTabExpander(), r.x);
//                        int w = x2 - x;
                        Rectangle clip = new Rectangle(r.x + x, r.y, x2 - (r.x + x), r.height);
                        Shape oldClip = g.getClip();
                        g.setClip(clip);
//                        x=x+(x2-x-w)/2;
                        Color oldColor = g.getColor();
                        g.setColor(Color.red);
                        int yTmp = r.y + (int) (getGlyphPainter().getAscent(this) * 0.5f);
//                        g.drawLine(r.x + x, yTmp, x2, yTmp);
                        g.setColor(oldColor);
//                        g.drawLine(r.x + x, r.y+r.height/2, r.x +x+ w, r.y+r.height/2);
//                        g.drawLine(r.x + x+w, r.y+r.height/2, r.x +x+w- 3, r.y+r.height/2+3);
//                        g.drawLine(r.x + x+w, r.y+r.height/2, r.x +x+w- 3, r.y+r.height/2-3);
//                        renderUnderlineOrStrike(g, r.x + x, y, 2*w);
                        g.setClip(oldClip);
                    }
                    else if (c==' ') {
                        int x2=modelToView(i+1+getStartOffset(), new Rectangle(r.width, r.height), Position.Bias.Forward).getBounds().x-x0;
                        int w=2;
                        Rectangle clip = new Rectangle(r.x + x, r.y, x2 - x, r.height);
                        Shape oldClip = g.getClip();
                        g.setClip(clip);
                        int x_new = x + (x2 - x - w)/2;
                        g.drawLine(r.x + x_new, r.y+r.height/2, r.x + x_new + w, r.y+r.height/2);
                        g.drawLine(r.x + x_new, r.y+r.height/2+1, r.x + x_new + w, r.y+r.height/2+1);
                        renderUnderlineOrStrike(g, r.x + x, y, x2 - x);
                        g.setClip(oldClip);
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void renderUnderlineOrStrike(Graphics g, int x, int y, int width) {
        if (isUnderline()) {
            // just 1 pixel below baseline
            int yTmp = y + 1;
            g.drawLine(x, yTmp, x + width, yTmp);
        }
        if (isStrikeThrough()) {
            // move y coordinate above baseline by roughly a third of the ascent
            int yTmp = y - (int) (getGlyphPainter().getAscent(this) * 0.3f);
            g.drawLine(x, yTmp, x + width, yTmp);
        }
    }
}
