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

public class EnsembleViewer {
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnsembleViewer::new);
    }

    public EnsembleViewer() {
        // Create the parent frame first - Docking.initialize needs a JFrame reference
        EnsembleParentPanel frame = new EnsembleParentPanel();

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
    }
}
