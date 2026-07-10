package hec.ensembleview;

import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Font;

/**
 * Resolves fonts from the active Swing Look &amp; Feel so the viewer's text follows the host
 * program's font settings (for example HMS's Program Settings font size and light/dark theme).
 * <p>
 * HMS changes fonts by rewriting the {@link UIManager} font defaults and then calling
 * {@code SwingUtilities.updateComponentTreeUI} on every window, which reaches the embedded viewer.
 * Standard Swing components pick that up automatically as long as they do not pin their own font,
 * so the viewer no longer hard-codes a font on those components. For surfaces that cache their own
 * font (gfx2d plots, custom-painted overlays, and the bold table header) this class supplies the
 * current L&amp;F font on demand, so those surfaces can re-read it whenever they repaint or their UI
 * is updated.
 */
public final class FontManager {
    private FontManager() {
    }

    /** The active Look &amp; Feel's base UI font; its family and size follow the host theme / font size. */
    public static Font defaultFont() {
        Font font = UIManager.getFont("Label.font");
        if (font == null) {
            font = new JLabel().getFont();
        }
        return font;
    }

    /**
     * A bold variant of the L&amp;F font stored under {@code uiManagerKey} (falling back to the base
     * font), for headings such as table headers. Re-reading this on each UI update keeps the heading
     * following the host font size and theme instead of freezing at a pinned size.
     */
    public static Font boldFont(String uiManagerKey) {
        Font font = UIManager.getFont(uiManagerKey);
        if (font == null) {
            font = defaultFont();
        }
        return font.deriveFont(Font.BOLD);
    }
}
