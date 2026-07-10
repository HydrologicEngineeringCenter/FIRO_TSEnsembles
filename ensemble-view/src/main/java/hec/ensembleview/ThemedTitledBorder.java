package hec.ensembleview;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.awt.Font;

/**
 * A {@link TitledBorder} whose title font follows the active Look &amp; Feel. It never pins its own
 * font; instead it derives a bold variant of the host component's current font each time the border
 * is painted, so the title tracks the host program's font size and theme (for example HMS light/dark
 * and the Program Settings font size) while staying visually distinct as a bold heading.
 * <p>
 * Because the font is resolved lazily in {@link #getFont(Component)} rather than stored, no explicit
 * refresh is needed when the host changes its font: the next repaint reads the updated L&amp;F font.
 */
public class ThemedTitledBorder extends TitledBorder {
    public ThemedTitledBorder(Border border, String title, int titleJustification, int titlePosition) {
        super(border, title, titleJustification, titlePosition);
    }

    @Override
    protected Font getFont(Component c) {
        // super.getFont resolves the component's current (L&F-driven) font; bold sets the heading apart.
        return super.getFont(c).deriveFont(Font.BOLD);
    }
}
