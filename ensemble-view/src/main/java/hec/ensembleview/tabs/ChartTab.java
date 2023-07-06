package hec.ensembleview.tabs;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.Statistics;
import hec.ensembleview.*;
import hec.ensembleview.mappings.ChartTypeStatisticsMap;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChartTab extends JPanel implements EnsembleTab {
    public JPanel chartPanel;
    public ComponentsPanel componentsPanel;
    public ChartType chartType;

    EnsembleDatabase db;
    RecordIdentifier rid;
    ZonedDateTime zdt;

    public ChartTab(JPanel chartPanel, ComponentsPanel componentsPanel, ChartType chartType) {
        this.chartPanel = chartPanel;
        this.componentsPanel = componentsPanel;
        this.chartType = chartType;

        setLayout(new BorderLayout());
        add(componentsPanel.getPanel(), BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        addActionListeners();
    }

    private void addActionListeners() {
        for (Statistics stat : ChartTypeStatisticsMap.getMap().get(chartType)) {
            EnsembleViewStat evs = componentsPanel.getStat(stat);
            evs.addActionListener(e -> tryShowingChart());
        }
    }

    public void tryShowingChart() {
        EnsembleChart ec = null;
        try {
            ec = createChart();
            if (ec == null)
                return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        chartPanel.removeAll();
        chartPanel.revalidate();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(ec.generateChart(), BorderLayout.CENTER);
        chartPanel.repaint();
    }

    private EnsembleChart createChart() throws Exception {
        if (rid == null || zdt == null) {
            return null;
        }

        Ensemble ensemble = db.getEnsemble(rid, zdt);

        float[][] vals = ensemble.getValues();
        EnsembleViewStat[] selectedStats = getSelectedStatistics();
        ZonedDateTime[] dates = ensemble.startDateTime();
        PlotStatisticsForChartType plotStatisticsForChartType = new PlotStatisticsForChartType(db, zdt, rid);

        if (chartType == ChartType.TIMEPLOT) {
            return createTimePlotChart(ensemble, selectedStats, dates, plotStatisticsForChartType, vals);
        } else {
            return createScatterPlotChart(ensemble, selectedStats, plotStatisticsForChartType);
        }
    }

    private EnsembleChart createTimePlotChart(Ensemble ensemble, EnsembleViewStat[] selectedStats, ZonedDateTime[] dates,
                                              PlotStatisticsForChartType plotStatisticsForChartType, float[][] vals) throws Exception {
        EnsembleChartAcrossTime chart = new EnsembleChartAcrossTime();
        chart.setXLabel("Date/Time");
        boolean randomColor = selectedStats.length <= 1;

        if (isTimeSeriesViewSelected(selectedStats)) {
            EnsembleTimeSeries cumulativeEnsembles = plotStatisticsForChartType.getCumulativeEnsembles(ensemble);
            plotStatisticsForChartType.addStatisticsToTimePlot(chart, selectedStats, cumulativeEnsembles, dates);

            addLineMembersToChart(chart, cumulativeEnsembles.getEnsemble(zdt).getValues(), dates, randomColor);
            chart.setYLabel(String.join(" ", rid.parameter, cumulativeEnsembles.getUnits()));
        } else {
            plotStatisticsForChartType.addStatisticsToTimePlot(chart, selectedStats, db.getEnsembleTimeSeries(rid), dates);

            addLineMembersToChart(chart, vals, dates, randomColor);
            chart.setYLabel(String.join(" ", rid.parameter, ensemble.getUnits()));
        }

        return chart;
    }

    private EnsembleChart createScatterPlotChart(Ensemble ensemble, EnsembleViewStat[] selectedStats,
                                                 PlotStatisticsForChartType plotStatisticsForChartType) throws Exception {
        EnsembleChartAcrossEnsembles chart = new EnsembleChartAcrossEnsembles();
        if (isTimeSeriesViewSelected(selectedStats)) {
            chart.setXLabel("Probability");
        } else {
            chart.setXLabel("Ensembles");
        }
        chart.setYLabel(String.join(" ", rid.parameter, ensemble.getUnits()));

        if (isTimeSeriesViewSelected(selectedStats)) {
            plotStatisticsForChartType.addStatisticsToProbabilityPlot(chart, selectedStats, db.getEnsembleTimeSeries(rid));
        } else {
            plotStatisticsForChartType.addStatisticsToScatterPlot(chart, selectedStats, db.getEnsembleTimeSeries(rid));
        }

        return chart;
    }




    private void addLineMembersToChart(EnsembleChart chart, float[][] vals, ZonedDateTime[] dates, boolean randomColor) throws ParseException {
        Color c = null;
        if (!randomColor) {
            c = Color.blue;
            int alpha = 50;
            int cInt = (c.getRGB() & 0xffffff) | (alpha << 24);
            c = new Color(cInt, true);

        }
        for (int i = 0; i < vals.length; i++) {
            ((EnsembleChartAcrossTime) (chart)).addLine(new LineSpec(0, vals[i], dates, new BasicStroke(1.0f), c, "Member " + (i + 1)));
        }

    }

    private boolean isTimeSeriesViewSelected(EnsembleViewStat[] selectedStats) {
        for (EnsembleViewStat stat : selectedStats) {
            if (stat.getStatUIType() == StatisticUIType.RADIOBUTTON && stat.hasInput() && stat.getStatType() != Statistics.NONE)
                return true;
        }
        return false;
    }

    private EnsembleViewStat[] getSelectedStatistics() {
        List<EnsembleViewStat> selectedStats = new ArrayList<>();
        for (Statistics stat : ChartTypeStatisticsMap.getMap().get(chartType)) {
            EnsembleViewStat selectedStat = componentsPanel.getStat(stat);
            if (selectedStat.hasInput()) {
                selectedStats.add(selectedStat);
            }
        }
        return selectedStats.toArray(new EnsembleViewStat[]{});
    }

    @Override
    public void setDatabase(EnsembleDatabase db) {
        this.db = db;
    }

    @Override
    public void setRecordIdentifier(RecordIdentifier rid) {
        this.rid = rid;
    }

    @Override
    public void setZonedDateTime(ZonedDateTime zdt) {
        this.zdt = zdt;
    }
}
