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

public class EnsembleViewer {
    static {
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
    private static EnsembleViewer instance;
    private final EnsembleParentPanel frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnsembleViewer::show);
    }

    /**
     * Opens the Ensemble Viewer, or focuses the already-open viewer if one exists.
     * This is the entry point host programs should call to launch the viewer so that
     * selecting it more than once never spawns duplicate windows.
     * Must be called on the Swing event dispatch thread.
     */
    public static synchronized EnsembleViewer show() {
        if (instance != null && instance.frame.isDisplayable()) {
            EnsembleParentPanel openFrame = instance.frame;
            if (openFrame.getExtendedState() == Frame.ICONIFIED) {
                openFrame.setExtendedState(Frame.NORMAL);
            }
            openFrame.toFront();
            openFrame.requestFocus();
            return instance;
        }
        instance = new EnsembleViewer();
        return instance;
    }

    private EnsembleViewer() {
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

        // Tear the singleton down on close so a fresh open re-initializes cleanly,
        // and release the single-instance reference so show() can open a new viewer.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Docking.uninitialize();
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
