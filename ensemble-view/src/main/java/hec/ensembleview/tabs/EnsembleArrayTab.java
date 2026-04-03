package hec.ensembleview.tabs;

import hec.ensembleview.charts.EnsembleDataTablePanel;
import hec.ensembleview.controllers.ComputePanelController;
import hec.ensembleview.controllers.EnsembleArrayChartManager;
import hec.ensembleview.viewpanels.ComputePanelView;
import hec.ensembleview.viewpanels.DataTransformView;
import hec.ensembleview.viewpanels.EnsembleDataTransformView;
import hec.ensembleview.viewpanels.StatEnsembleComputePanelView;

import javax.swing.*;
import java.awt.*;

public class EnsembleArrayTab extends JPanel {
    private final JPanel chartContainer;
    private final JPanel statAndDataViewPanel = new JPanel();
    private final EnsembleDataTablePanel tablePanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private boolean showingTable = false;
    private static final String CHART_CARD = "chart";
    private static final String TABLE_CARD = "table";

    public EnsembleArrayTab() {
        this.chartContainer = new JPanel(new BorderLayout());
        this.tablePanel = new EnsembleDataTablePanel();

        ComputePanelView statEnsembleComputePanelView = new StatEnsembleComputePanelView();
        DataTransformView ensembleDataTransformView = new EnsembleDataTransformView();

        initiateEnsembleChart(ensembleDataTransformView, statEnsembleComputePanelView);
        setupStatisticsAndDataViewPanel(statEnsembleComputePanelView, ensembleDataTransformView);

        cardPanel.add(chartContainer, CHART_CARD);
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
        JToggleButton toggleButton = new JToggleButton(new TimeSeriesTab.TableIcon());
        toggleButton.setToolTipText("Toggle Table/Chart View");
        toggleButton.setPreferredSize(new Dimension(28, 28));
        toggleButton.setFocusPainted(false);
        toggleButton.setMargin(new Insets(2, 2, 2, 2));
        toggleButton.addActionListener(e -> {
            showingTable = toggleButton.isSelected();
            if (showingTable) {
                toggleButton.setIcon(new TimeSeriesTab.ChartIcon());
                cardLayout.show(cardPanel, TABLE_CARD);
            } else {
                toggleButton.setIcon(new TimeSeriesTab.TableIcon());
                cardLayout.show(cardPanel, CHART_CARD);
            }
        });
        return toggleButton;
    }

    private void initiateEnsembleChart(DataTransformView dataTransformView, ComputePanelView computePanelView) {
        ComputePanelController computePanelController = new ComputePanelController(dataTransformView, computePanelView);
        new EnsembleArrayChartManager(computePanelController.getStatisticsMap(), chartContainer, tablePanel);
    }
}