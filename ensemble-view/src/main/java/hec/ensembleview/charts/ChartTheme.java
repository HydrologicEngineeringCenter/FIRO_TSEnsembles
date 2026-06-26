package hec.ensembleview.charts;

import hec.gfx2d.AxisLabel;
import hec.gfx2d.AxisTics;
import hec.gfx2d.G2dLabel;
import hec.gfx2d.G2dPanel;
import hec.gfx2d.Viewport;

import javax.swing.UIManager;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repaints a gfx2d {@link G2dPanel} in the active Swing Look &amp; Feel's colors so the plot
 * follows the host theme (e.g. HMS light/dark).
 * <p>
 * gfx2d draws its plot background, gridlines, axis tics, axis labels, and title with its own
 * stored colors, which it seeds once at construction and never re-derives from the L&amp;F. So when
 * the host switches theme the surrounding Swing chrome re-skins but the plot stays in its original
 * colors. This class pushes the current L&amp;F colors into those gfx2d elements. The colors are read
 * from {@link UIManager} at call time, so the plot matches whatever theme is active without this
 * class needing to know which one.
 */
final class ChartTheme {
    private static final Logger logger = Logger.getLogger(ChartTheme.class.getName());

    // The four axis slots gfx2d exposes per viewport; a given viewport may not use all of them.
    private static final int[] AXES = {G2dPanel.X1, G2dPanel.X2, G2dPanel.Y1, G2dPanel.Y2};

    private ChartTheme() {
    }

    /**
     * Applies the current Look &amp; Feel colors to every viewport, axis, and the title of {@code panel}
     * and repaints it. Safe to call at any time: if the panel has not finished building its viewports
     * yet (e.g. during construction) it is left untouched.
     */
    static void apply(G2dPanel panel) {
        if (panel == null) {
            return;
        }
        Color background = background();
        Color foreground = foreground();
        Color grid = blend(background, foreground, 0.30f);
        String foregroundString = rgb(foreground);

        panel.setBackground(background);

        Viewport[] viewports;
        try {
            viewports = panel.getViewports();
        } catch (RuntimeException ex) {
            // The plot is not built yet (e.g. updateUI() firing during construction); nothing to skin.
            logger.log(Level.FINE, "Plot not ready for theming; skipping.", ex);
            return;
        }

        applyLabel(panel.getTitlePanel(), background, foregroundString);

        if (viewports != null) {
            for (Viewport viewport : viewports) {
                if (viewport == null) {
                    continue;
                }
                viewport.setBackground(background);
                viewport.setGridXColor(grid);
                viewport.setGridYColor(grid);
                viewport.setBorderColor(foregroundString);
                for (int axis : AXES) {
                    applyAxis(panel, viewport, axis, background, foregroundString);
                }
            }
        }
        panel.repaint();
    }

    private static void applyAxis(G2dPanel panel, Viewport viewport, int axis, Color background, String foregroundString) {
        try {
            AxisTics tics = panel.getViewportAxisTics(viewport, axis);
            if (tics != null) {
                tics.setAxisTicColor(foregroundString);
            }
            AxisLabel label = panel.getViewportAxisLabel(viewport, axis);
            applyLabel(label, background, foregroundString);
        } catch (RuntimeException ex) {
            // A viewport need not have all four axes; skip the ones it does not define.
            logger.log(Level.FINE, "Skipped theming an absent axis.", ex);
        }
    }

    private static void applyLabel(G2dLabel label, Color background, String foregroundString) {
        if (label == null) {
            return;
        }
        label.setBackground(background);
        label.setForeground(foregroundString);
    }

    /** The plot background: the host's panel background, so the plot blends into the themed window. */
    private static Color background() {
        return uiColor("Panel.background", Color.WHITE);
    }

    /** The plot foreground (axis tics, labels, title, border): the host's standard text color. */
    private static Color foreground() {
        Color c = UIManager.getColor("Label.foreground");
        if (c == null) {
            c = UIManager.getColor("textText");
        }
        return c != null ? new Color(c.getRGB()) : Color.BLACK;
    }

    private static Color uiColor(String key, Color fallback) {
        Color c = UIManager.getColor(key);
        // Copy to a plain Color: UIManager returns a ColorUIResource the L&F may swap out.
        return c != null ? new Color(c.getRGB()) : fallback;
    }

    private static Color blend(Color base, Color overlay, float overlayWeight) {
        float w = Math.max(0f, Math.min(1f, overlayWeight));
        int r = Math.round(base.getRed() * (1 - w) + overlay.getRed() * w);
        int g = Math.round(base.getGreen() * (1 - w) + overlay.getGreen() * w);
        int b = Math.round(base.getBlue() * (1 - w) + overlay.getBlue() * w);
        return new Color(r, g, b);
    }

    /**
     * gfx2d color strings are parsed by {@code rma.swing.RmaColor.parseColorString}; an "r,g,b"
     * triple is read directly as RGB, which avoids that parser's brittle hex/name handling.
     */
    private static String rgb(Color c) {
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }
}
