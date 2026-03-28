package hec.ensembleview.tabs;

import hec.ensembleview.charts.EnsembleChartAcrossTime;
import hec.ensembleview.charts.EnsembleDataTablePanel;
import hec.ensembleview.controllers.ComputePanelController;
import hec.ensembleview.controllers.EnsembleTimeSeriesChartManager;
import hec.ensembleview.viewpanels.ComputePanelView;
import hec.ensembleview.viewpanels.DataTransformView;
import hec.ensembleview.viewpanels.StatTimeSeriesComputePanelView;
import hec.ensembleview.viewpanels.TimeSeriesDataTransformView;
import hec.gfx2d.G2dPanel;

import javax.swing.*;
import java.awt.*;

public class TimeSeriesTab extends JPanel {
    private final G2dPanel ensembleChart;
    private final JPanel statAndDataViewPanel = new JPanel();
    private final EnsembleDataTablePanel tablePanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private boolean showingTable = false;
    private static final String CHART_CARD = "chart";
    private static final String TABLE_CARD = "table";

    public TimeSeriesTab() {
        this.ensembleChart = createEnsembleChart();
        this.tablePanel = new EnsembleDataTablePanel();

        ComputePanelView statTimeSeriesComputePanelView = new StatTimeSeriesComputePanelView();
        DataTransformView timeSeriesDataTransformView = new TimeSeriesDataTransformView();

        initiateChartManager(timeSeriesDataTransformView, statTimeSeriesComputePanelView);
        setupStatisticsAndDataViewPanel(statTimeSeriesComputePanelView, timeSeriesDataTransformView);

        cardPanel.add(ensembleChart, CHART_CARD);
        cardPanel.add(tablePanel, TABLE_CARD);

        setLayout(new BorderLayout());
        add(statAndDataViewPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
    }

    private void setupStatisticsAndDataViewPanel(JPanel statsPanel, JPanel dataViewPanel) {
        statAndDataViewPanel.setLayout(new BorderLayout());
        statAndDataViewPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel dataViewRow = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(createToggleButton());
        dataViewRow.add(buttonPanel, BorderLayout.WEST);
        dataViewRow.add(dataViewPanel, BorderLayout.CENTER);

        statAndDataViewPanel.add(dataViewRow, BorderLayout.SOUTH);
    }

    private JToggleButton createToggleButton() {
        JToggleButton toggleButton = new JToggleButton(new TableIcon());
        toggleButton.setToolTipText("Toggle Table/Chart View");
        toggleButton.setPreferredSize(new Dimension(28, 28));
        toggleButton.setFocusPainted(false);
        toggleButton.setMargin(new Insets(2, 2, 2, 2));
        toggleButton.addActionListener(e -> {
            showingTable = toggleButton.isSelected();
            if (showingTable) {
                toggleButton.setIcon(new ChartIcon());
                cardLayout.show(cardPanel, TABLE_CARD);
            } else {
                toggleButton.setIcon(new TableIcon());
                cardLayout.show(cardPanel, CHART_CARD);
            }
        });
        return toggleButton;
    }

    private G2dPanel createEnsembleChart() {
        return new EnsembleChartAcrossTime().generateChart();
    }

    private void initiateChartManager(DataTransformView dataTransformView, ComputePanelView computePanelView) {
        ComputePanelController computePanelController = new ComputePanelController(dataTransformView, computePanelView);
        new EnsembleTimeSeriesChartManager(computePanelController.getStatisticsMap(), ensembleChart, tablePanel);
    }

    static class TableIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1));
            int w = getIconWidth();
            int h = getIconHeight();
            for (int i = 0; i <= 3; i++) {
                int ly = y + i * h / 3;
                g2.drawLine(x, ly, x + w, ly);
            }
            for (int i = 0; i <= 3; i++) {
                int lx = x + i * w / 3;
                g2.drawLine(lx, y, lx, y + h);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    static class ChartIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
            int w = getIconWidth();
            int h = getIconHeight();
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 2, y + 1, x + 2, y + h - 1);
            g2.drawLine(x + 2, y + h - 1, x + w - 1, y + h - 1);
            g2.setColor(new Color(51, 153, 255));
            g2.setStroke(new BasicStroke(1.5f));
            int[] xPoints = {x + 3, x + 6, x + 9, x + 12, x + 15};
            int[] yPoints = {y + 10, y + 5, y + 8, y + 3, y + 6};
            g2.drawPolyline(xPoints, yPoints, 5);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}