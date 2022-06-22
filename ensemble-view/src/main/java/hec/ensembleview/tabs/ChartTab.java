package hec.ensembleview.tabs;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensembleview.ChartType;
import hec.ensembleview.ComponentsPanel;
import hec.ensembleview.ComputeEngine;
import hec.ensembleview.EnsembleChart;
import hec.ensembleview.EnsembleChartAcrossEnsembles;
import hec.ensembleview.EnsembleChartAcrossTime;
import hec.ensembleview.EnsembleViewStat;
import hec.ensembleview.LineSpec;
import hec.ensembleview.PointSpec;
import hec.ensembleview.StatisticUIType;
import hec.ensembleview.TextBoxStat;
import hec.ensembleview.mappings.ChartTypeStatisticsMap;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChartTab extends JPanel implements EnsembleTab {
    public JPanel chartPanel;
    public ComponentsPanel componentsPanel;
    public ChartType chartType;

    EnsembleDatabase db;
    RecordIdentifier rid;
    ZonedDateTime zdt;
    ComputeEngine computeEngine;

    public ChartTab(JPanel chartPanel, ComponentsPanel componentsPanel, ChartType chartType) {
        this.chartPanel = chartPanel;
        this.componentsPanel = componentsPanel;
        this.chartType = chartType;
        computeEngine = new ComputeEngine();

        setLayout(new BorderLayout());
        add(componentsPanel.getPanel(), BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        addActionListeners();
    }

    private void addActionListeners() {
        for (Statistics stat : ChartTypeStatisticsMap.map.get(chartType)) {
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

        /*
        depending on which tab pane is selected, show time series plot or show scatter plot
         */

        if(chartType == ChartType.TimePlot) {
            EnsembleChartAcrossTime chart = new EnsembleChartAcrossTime();
            chart.setXLabel("Date/Time");
            chart.setYLabel(String.join(" ", rid.parameter, ensemble.getUnits()));
            boolean randomColor = selectedStats.length <= 1;
            if (isTimeSeriesViewSelected(selectedStats)){  // if the Radio button is selected to Cumulative or Moving Average, compute metric for time series view
                float[][] cumulativeVals = computeEngine.computeRadioButtonTimeSeriesView(db.getEnsembleTimeSeries(rid),
                        getSelectedTimeSeriesView(selectedStats), zdt, ChartType.TimePlot);
                EnsembleTimeSeries ets = new EnsembleTimeSeries(rid, "units", "data_type", "version");
                ets.addEnsemble(new Ensemble(ensemble.getIssueDate(), cumulativeVals, ensemble.getStartDateTime(), ensemble.getInterval(), ensemble.getUnits()));
                addStatisticsToTimePlot(chart, selectedStats, ets, dates);
                addLineMembersToChart(chart, cumulativeVals, dates, randomColor);
            }
            else
            {
                addStatisticsToTimePlot(chart, selectedStats, db.getEnsembleTimeSeries(rid), dates);
                addLineMembersToChart(chart, vals, dates, randomColor);
            }
            return chart;

        } else {
            EnsembleChartAcrossEnsembles chart = new EnsembleChartAcrossEnsembles();
            chart.setXLabel("Ensembles");
            chart.setYLabel(String.join(" ", rid.parameter, ensemble.getUnits()));
            addStatisticsToScatterPlot(chart, selectedStats, db.getEnsembleTimeSeries(rid));
            return chart;
        }
    }

    private void addStatisticsToTimePlot(EnsembleChartAcrossTime chart, EnsembleViewStat[] stats, EnsembleTimeSeries ets, ZonedDateTime[] dates) throws ParseException {
        for (EnsembleViewStat selectedStat : stats) {
            switch (selectedStat.getStatType()) {
                case MIN:
                case MAX:
                    chart.addLine(
                            new LineSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.TimePlot), dates,
                                    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                            1.0f, new float[]{6.0f, 6.0f}, 0.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case AVERAGE:
                    chart.addLine(
                            new LineSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.TimePlot), dates,
                                    new BasicStroke(3.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEDIAN:
                    chart.addLine(
                            new LineSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.TimePlot), dates,
                                    new BasicStroke(3.0f), Color.BLUE, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case PERCENTILE:
                    float[] percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for(int i = 0; i < percentiles.length; i++) {
                        chart.addLine(
                                new LineSpec(0, computeEngine.computeTextBoxStat(ets, selectedStat.getStatType(), zdt, new float[] {(percentiles[i])}, ChartType.TimePlot), dates,
                                        new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                1.0f, new float[]{6.0f, 6.0f}, 0.0f), randomColor(i+1), StatisticsStringMap.map.get(selectedStat.getStatType()) + " " + df.format(percentiles[i]*100) + "%"));
                    }
                    break;
            }
        }
    }

    private void addStatisticsToScatterPlot(EnsembleChartAcrossEnsembles chart, EnsembleViewStat[] stats, EnsembleTimeSeries ets) throws ParseException {
        for (EnsembleViewStat selectedStat : stats) {
            switch (selectedStat.getStatType()) {
                case MIN:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.ScatterPlot),
                                    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                            1.0f, new float[]{6.0f, 6.0f}, 0.0f), Color.RED, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MAX:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.ScatterPlot),
                                    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                            1.0f, new float[]{6.0f, 6.0f}, 0.0f), Color.BLUE, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case AVERAGE:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.ScatterPlot),
                                    new BasicStroke(3.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEDIAN:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.ScatterPlot),
                                    new BasicStroke(3.0f), Color.ORANGE, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case TOTAL:
                    chart.addPoint(
                            new PointSpec(1, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), zdt, ChartType.ScatterPlot),
                                    new BasicStroke(3.0f), Color.GRAY, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case PERCENTILE:
                    float[] percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for(int i = 0; i < percentiles.length; i++) {

                        chart.addPoint(
                                new PointSpec(0, computeEngine.computeTextBoxStat(ets, selectedStat.getStatType(), zdt, new float[] {(percentiles[i])}, ChartType.ScatterPlot),
                                        new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                                1.0f, new float[]{6.0f, 6.0f}, 0.0f), randomColor(i+1), StatisticsStringMap.map.get(selectedStat.getStatType()) + " " + df.format(percentiles[i]*100) + "%"));
                    }
                    break;
                case MAXAVERAGEDURATION:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeTextBoxStat(db.getEnsembleTimeSeries(rid), selectedStat.getStatType(), zdt, ((TextBoxStat) selectedStat).getTextFieldValue(), ChartType.ScatterPlot),
                                    new BasicStroke(3.0f), Color.PINK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MAXACCUMDURATION:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeTextBoxStat(db.getEnsembleTimeSeries(rid), selectedStat.getStatType(), zdt, ((TextBoxStat) selectedStat).getTextFieldValue(), ChartType.ScatterPlot),
                                    new BasicStroke(3.0f), Color.GREEN, StatisticsStringMap.map.get(selectedStat.getStatType())));
            }
        }
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

    private Statistics getSelectedTimeSeriesView(EnsembleViewStat[] selectedStats) {
        for (EnsembleViewStat stat : selectedStats) {
            if (stat.getStatUIType() == StatisticUIType.RADIOBUTTON && stat.hasInput())
                return stat.getStatType();
        }
        return null;
    }

    private EnsembleViewStat[] getSelectedStatistics() {
        List<EnsembleViewStat> selectedStats = new ArrayList<>();
        for (Statistics stat : ChartTypeStatisticsMap.map.get(chartType)) {
            EnsembleViewStat selectedStat = componentsPanel.getStat(stat);
            if (selectedStat.hasInput()) {
                selectedStats.add(selectedStat);
            }
        }
        return selectedStats.toArray(new EnsembleViewStat[]{});
    }

    private Color randomColor(int i) {
        Random rand = new Random(i);
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        Color color = new Color(r, g, b);
        return color;
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
