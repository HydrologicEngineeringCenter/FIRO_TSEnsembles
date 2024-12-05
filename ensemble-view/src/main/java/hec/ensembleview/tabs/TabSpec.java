package hec.ensembleview.tabs;

import hec.ensembleview.viewpanels.ComputePanelView;
import hec.ensembleview.viewpanels.DataTransformView;
import hec.gfx2d.G2dPanel;

import javax.swing.*;
import java.awt.*;

public class TabSpec {
    private final String tabName;
    private final JPanel panel;
    private final ComputePanelView computePanelView;
    private final DataTransformView dataTransformView;

    public static class TabSpecBuilder {
        private String tabName;
        private JPanel panel;
        private ComputePanelView computePanelView;
        private DataTransformView dataTransformView;

        public TabSpecBuilder addTabName(String tabName) {
            this.tabName = tabName;
            return this;
        }

        public TabSpecBuilder addStatsPanel(ComputePanelView computePanelView,
                                            DataTransformView dataTransformView) {
            JPanel statsPanel = new JPanel();
            statsPanel.setLayout(new BorderLayout());
            this.panel.add(statsPanel, BorderLayout.NORTH);

            this.computePanelView = computePanelView;
            statsPanel.add(this.computePanelView, BorderLayout.NORTH);

            this.dataTransformView = dataTransformView;
            statsPanel.add(this.dataTransformView, BorderLayout.SOUTH);

            return this;
        }

        public TabSpecBuilder addPanel(JPanel panel) {
            this.panel = panel;
            return this;
        }

        public TabSpecBuilder addG2dPanel(G2dPanel g2dPanel) {
            this.panel.add(g2dPanel, BorderLayout.CENTER);
            return this;
        }

        public TabSpec build() {
            return new TabSpec(this);
        }
    }

    public String getTabName() {
        return tabName;
    }

    public JPanel getPanel() {
        return panel;
    }

    public ComputePanelView getComputePanelView() {
        return this.computePanelView;
    }

    public DataTransformView getDataTransformView() {
        return this.dataTransformView;
    }

    private TabSpec(TabSpecBuilder builder) {
        tabName = builder.tabName;
        panel = builder.panel;
        computePanelView = builder.computePanelView;
        dataTransformView = builder.dataTransformView;
    }
}
