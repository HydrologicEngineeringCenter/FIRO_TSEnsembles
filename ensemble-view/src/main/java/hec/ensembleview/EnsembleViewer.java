package hec.ensembleview;

import hec.ensembleview.controllers.DatabaseController;
import hec.ensembleview.tabs.EnsembleArrayTab;
import hec.ensembleview.tabs.SingleValueSummaryTab;
import hec.ensembleview.tabs.TimeSeriesTab;
import hec.ensembleview.viewpanels.EnsembleParentPanel;
import hec.ensembleview.viewpanels.OptionsPanel;
import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnsembleViewer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Standalone launch: render in the system look and feel. Embedded in a host app
            // (e.g. HMS), this main() never runs, so the frame is built under the host's
            // active L&F and renders in the host's theme. Setting the L&F here (rather than
            // in a static initializer) also means the viewer never re-stomps the global L&F,
            // so a host theme toggle re-skins it.
            setSystemLookAndFeel();
            show();
        });
    }

    /**
     * Installs the system look and feel. Used for the standalone launch and as the embedded
     * fallback below; the single catch lives here so the two call sites don't duplicate it.
     */
    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    // Only one viewer window may exist per JVM. The docking framework and the
    // DatabaseHandlerService are process-wide singletons, so a second concurrent
    // viewer would collide on dockable registration and share the first viewer's
    // loaded database. A host program may invoke the viewer repeatedly; show()
    // brings the already-open window to the front instead of opening another.
    private static final Logger logger = Logger.getLogger(EnsembleViewer.class.getName());
    private static EnsembleViewer instance;
    private final EnsembleParentPanel frame;

    /**
     * Opens the Ensemble Viewer, or focuses the already-open viewer if one exists. This is the
     * entry point host programs should call to launch the viewer so that selecting it more than
     * once never spawns duplicate windows.
     * <p>
     * Safe to call from any thread: a call off the event dispatch thread is re-dispatched onto
     * the EDT (and returns {@code null}, since the viewer is built asynchronously). Swing builds
     * the UI and mutates {@code instance} only on the EDT, so this also keeps {@code instance}
     * race-free against the window-close handler.
     */
    public static synchronized EnsembleViewer show() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(EnsembleViewer::show);
            return null;
        }
        if (instance != null && instance.frame.isDisplayable()) {
            EnsembleParentPanel openFrame = instance.frame;
            // Clear only the iconified bit so a window that was maximized before being minimized
            // is restored to maximized, not shrunk to normal size.
            int state = openFrame.getExtendedState();
            if ((state & Frame.ICONIFIED) != 0) {
                openFrame.setExtendedState(state & ~Frame.ICONIFIED);
            }
            openFrame.toFront();
            openFrame.requestFocus();
            return instance;
        }
        instance = new EnsembleViewer();
        return instance;
    }

    private EnsembleViewer() {
        // Defensive fallback for embedded use: if the host has not installed a look and feel
        // (the JVM is still on the default cross-platform "Metal" L&F), apply the system L&F
        // so an embedded viewer never renders in dated Metal. When a host such as HMS has
        // already set its own theme, the current L&F is not Metal, so this is skipped and the
        // viewer matches the host. Standalone, main() has already set the system L&F, so this
        // is likewise skipped. Runs on the EDT (constructor is invoked from invokeLater / the
        // host's EDT), which is where Swing requires the L&F to be set.
        if (UIManager.getLookAndFeel().getClass().getName()
                .equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
            // Unlike the standalone launch, a failure here must not propagate: this is a cosmetic
            // fallback running inside an embedding host, so log it and keep the default L&F rather
            // than throwing out of the constructor and destabilizing the host.
            try {
                setSystemLookAndFeel();
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "Could not apply system look and feel; using the host default.", ex);
            }
        }

        // Create the parent frame first - Docking.initialize needs a JFrame reference
        frame = new EnsembleParentPanel();

        OptionsPanel optionsPanel = new OptionsPanel();
        new DatabaseController(optionsPanel);

        // Initialize the docking framework
        Docking.initialize(frame);

        // Wrap each tab content as a Dockable
        DockablePanel timeSeriesDock = new DockablePanel("timeSeries", "Time Series Plot", new TimeSeriesTab());
        DockablePanel scatterDock = new DockablePanel("scatter", "Scatter Plot", new EnsembleArrayTab());
        DockablePanel summaryDock = new DockablePanel("summary", "Single Value Summary", new SingleValueSummaryTab());

        // Create the root docking panel
        RootDockingPanel rootPanel = new RootDockingPanel(frame);

        // Mount the root docking panel into the parent frame
        frame.setContents(optionsPanel, rootPanel);

        // Default layout: dock all three as a tabbed group in the center
        Docking.dock(timeSeriesDock, frame);
        Docking.dock(scatterDock, timeSeriesDock, DockingRegion.CENTER);
        Docking.dock(summaryDock, timeSeriesDock, DockingRegion.CENTER);

        // Make Time Series the active tab on startup
        Docking.bringToFront(timeSeriesDock);

        // Tear down per-viewer state on close so a fresh open re-initializes cleanly:
        //  - uninitialize docking so dockables can re-register,
        //  - drop this viewer's listeners from the DatabaseHandlerService singleton so the
        //    disposed UI graph can be garbage-collected and stale listeners stop firing,
        //  - release the single-instance reference so show() can open a new viewer.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Docking.uninitialize();
                DatabaseHandlerService.getInstance().clearDatabaseChangeListeners();
                if (instance == EnsembleViewer.this) {
                    instance = null;
                }
            }
        });
    }

    /**
     * A simple Dockable wrapper that hosts a content component as a draggable panel.
     */
    private static class DockablePanel extends JPanel implements Dockable {
        private final String persistentID;
        private final String tabText;

        DockablePanel(String persistentID, String tabText, JComponent content) {
            super(new BorderLayout());
            this.persistentID = persistentID;
            this.tabText = tabText;
            add(content, BorderLayout.CENTER);
            Docking.registerDockable(this);
        }

        @Override
        public String getPersistentID() {
            return persistentID;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public String getTabText() {
            return tabText;
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public boolean isLimitedToWindow() {
            return false;
        }

        @Override
        public DockableStyle getStyle() {
            return DockableStyle.BOTH;
        }

        @Override
        public boolean isClosable() {
            return false;
        }

        @Override
        public boolean isAutoHideAllowed() {
            return false;
        }

        @Override
        public boolean isMinMaxAllowed() {
            return false;
        }

        // modern-docking's DisplayPanel wraps the dockable in a JScrollPane
        // when this returns true (the interface default). The scroll pane
        // honors the chart's preferred size, so the G2dPanel can extend
        // beyond the visible viewport — i.e. the chart appears clipped and
        // sized larger than the docking frame. Charts are self-resizing and
        // should fill the available space directly, with no scroll pane.
        @Override
        public boolean isWrappableInScrollpane() {
            return false;
        }
    }
}
