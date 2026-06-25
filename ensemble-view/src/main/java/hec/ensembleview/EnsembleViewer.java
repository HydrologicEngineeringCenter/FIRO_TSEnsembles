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
            // Standalone launch only: apply the system L&F before building the UI. When embedded
            // in a host (e.g. HMS) main() never runs, so the viewer inherits the host's theme.
            setSystemLookAndFeel();
            show();
        });
    }

    /** Installs the system look and feel; shared by the standalone launch and the embedded fallback. */
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
     * Opens the viewer, or focuses the already-open one. Host programs should call this so that
     * launching the viewer more than once never spawns duplicate windows.
     * <p>
     * Safe to call from any thread: an off-EDT call is re-dispatched onto the EDT and returns
     * {@code null}. {@code instance} is only touched on the EDT, keeping it race-free against the
     * window-close handler.
     */
    public static synchronized EnsembleViewer show() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(EnsembleViewer::show);
            return null;
        }
        if (instance != null && instance.frame.isDisplayable()) {
            EnsembleParentPanel openFrame = instance.frame;
            // Clear only the iconified bit so a window minimized while maximized is restored to
            // maximized, not shrunk to normal size.
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
        // Embedded fallback: if the host left the default cross-platform "Metal" L&F in place,
        // switch to the system L&F so the viewer never renders in dated Metal. Skipped when a host
        // (or main()) has already set a non-Metal L&F. Runs on the EDT, as the constructor does.
        if (UIManager.getLookAndFeel().getClass().getName()
                .equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
            // Cosmetic only and running inside a host: log and keep the default rather than throwing.
            try {
                setSystemLookAndFeel();
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "Could not apply system look and feel; using the host default.", ex);
            }
        }

        // Build the frame first - Docking.initialize needs a JFrame reference.
        frame = new EnsembleParentPanel();

        OptionsPanel optionsPanel = new OptionsPanel();
        new DatabaseController(optionsPanel);

        Docking.initialize(frame);

        DockablePanel timeSeriesDock = new DockablePanel("timeSeries", "Time Series Plot", new TimeSeriesTab());
        DockablePanel scatterDock = new DockablePanel("scatter", "Scatter Plot", new EnsembleArrayTab());
        DockablePanel summaryDock = new DockablePanel("summary", "Single Value Summary", new SingleValueSummaryTab());

        RootDockingPanel rootPanel = new RootDockingPanel(frame);
        frame.setContents(optionsPanel, rootPanel);

        // Dock all three as a tabbed group in the center, with Time Series active.
        Docking.dock(timeSeriesDock, frame);
        Docking.dock(scatterDock, timeSeriesDock, DockingRegion.CENTER);
        Docking.dock(summaryDock, timeSeriesDock, DockingRegion.CENTER);
        Docking.bringToFront(timeSeriesDock);

        // Tear down per-viewer docking and listener state so the next open starts clean.
        // This must run on windowClosing, not windowClosed: the frame is DISPOSE_ON_CLOSE, and once
        // it is disposed modern-docking has already removed its root panel, so undock/deregister
        // would throw RootDockingPanelNotFoundException and leave the dockable IDs registered. That
        // strands the docking singleton and makes the next open fail to re-register "timeSeries"
        // (DockableRegistrationFailureException) - the "viewer won't reopen" bug.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                safeDeregister(timeSeriesDock);
                safeDeregister(scatterDock);
                safeDeregister(summaryDock);
                try {
                    Docking.uninitialize();
                } catch (RuntimeException ex) {
                    logger.log(Level.WARNING, "Docking framework teardown failed on viewer close.", ex);
                }
                DatabaseHandlerService.getInstance().clearDatabaseChangeListeners();
                if (instance == EnsembleViewer.this) {
                    instance = null;
                }
            }
        });
    }

    /**
     * Deregisters a dockable, logging instead of propagating on failure so that one panel's
     * teardown error does not leave the other panels' IDs stranded in the registry.
     */
    private static void safeDeregister(Dockable dockable) {
        try {
            Docking.deregisterDockable(dockable);
        } catch (RuntimeException ex) {
            logger.log(Level.WARNING, "Failed to deregister dockable \"" + dockable.getPersistentID()
                    + "\" on viewer close.", ex);
        }
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

        // Return false so modern-docking's DisplayPanel does not wrap the dockable in a JScrollPane.
        // A scroll pane honors the chart's preferred size, letting the G2dPanel extend past the
        // viewport (clipped, oversized). Charts self-resize and should fill the space directly.
        @Override
        public boolean isWrappableInScrollpane() {
            return false;
        }
    }
}
