package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensembleview.mappings.ChartTypeStatisticsMap;
import hec.ensembleview.mappings.StatisticsStringMap;
import hec.ensembleview.tabs.ChartTab;
import hec.ensembleview.tabs.SingleValueSummaryTab;
import hec.stats.Statistics;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.Random;

public class EnsembleViewer {
    public SqliteDatabase db;
    private ComputeEngine computeEngine;
    private EnsembleChart ec;

    private RecordIdentifier selectedRid = null;
    private ZonedDateTime selectedZdt = null;

    private JFrame frame;
    private JPanel topPanel;
    private final List<TabSpec> tabs = new ArrayList<>();
    private JPanel optionsPanel;
    private JPanel filePathPanel;
    private JTextField filePath;
    private JButton fileSearchButton;
    private JComboBox<String> locations;
    private JComboBox<String> dateTimes;
    private JTabbedPane tabPane;

    public static void main(String[] args) {
        EnsembleViewer ev = new EnsembleViewer();
        ev.setVisible(true);
    }

    public void setVisible(boolean value) {
        frame.setVisible(value);
    }

    private void setRidFromString(String rid) {
        selectedRid = getRecordIdentifierFromString(rid);
    }

    private void setDateTimeFromString(String date) {
        selectedZdt = getZonedDateTimeFromString(selectedRid, date);
    }

    private void setupDateTimeComboBox(JComboBox<String> dateTimeComboBox) {
        String[] zdts = db.getEnsembleIssueDates(selectedRid).stream().map(ZonedDateTime::toString).toArray(String[]::new);
        ComboBoxModel<String> model = new DefaultComboBoxModel<>(zdts);
        dateTimeComboBox.setModel(model);
    }

    private void showEmptyChart(JPanel chartPanel) {
        chartPanel.removeAll();
        chartPanel.revalidate();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(new EnsembleChartAcrossTime().generateChart(), BorderLayout.CENTER);
        chartPanel.repaint();
    }

    private RecordIdentifier getRecordIdentifierFromString(String stringRID){
        List<RecordIdentifier> rids = db.getEnsembleTimeSeriesIDs();
        for (RecordIdentifier rid : rids) {
            if (Objects.equals(rid.toString(), stringRID)) {
                return rid;
            }
        }
        return null;
    }

    private ZonedDateTime getZonedDateTimeFromString(RecordIdentifier rid ,String stringZDT){
        List<ZonedDateTime> zdts = db.getEnsembleIssueDates(rid);
        for (ZonedDateTime zdt : zdts) {
            if (Objects.equals(zdt.toString(), stringZDT)){
                return zdt;
            }
        }
        return null;
    }

    private void tryShowingChart() {
        if (tabs.get(tabPane.getSelectedIndex()).tabType != TabType.Chart)
            return;



        JPanel chartPanel = getCurrentlyShownChart();

        try {
            ec = createChart();
            if (ec == null){
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        chartPanel.removeAll();
        chartPanel.revalidate();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(ec.generateChart(), BorderLayout.CENTER);
        chartPanel.repaint();
    }

    private EnsembleChart createChart() throws Exception {
        if (selectedRid == null || selectedZdt == null) {
            return null;
        }

        Ensemble ensemble = db.getEnsemble(selectedRid, selectedZdt);

        float[][] vals = ensemble.getValues();
        EnsembleViewStat[] selectedStats = getSelectedStatistics();
        ZonedDateTime[] dates = ensemble.startDateTime();

        /*
        depending on which tab pane is selected, show time series plot or show scatter plot
         */

        if(((ChartTab)tabs.get(tabPane.getSelectedIndex()).panel).chartType == ChartType.TimePlot) {
            EnsembleChartAcrossTime chart = new EnsembleChartAcrossTime();
            chart.setXLabel("Date/Time");
            chart.setYLabel(String.join(" ", selectedRid.parameter, ensemble.getUnits()));
            boolean randomColor = selectedStats.length <= 1;
            if (isTimeSeriesViewSelected(selectedStats)){  // if the Radio button is selected to Cumulative or Moving Average, compute metric for time series view
                float[][] cumulativeVals = computeEngine.computeRadioButtonTimeSeriesView(db.getEnsembleTimeSeries(selectedRid),
                        getSelectedTimeSeriesView(selectedStats), selectedZdt, ChartType.TimePlot);
                EnsembleTimeSeries ets = new EnsembleTimeSeries(selectedRid, "units", "data_type", "version");
                ets.addEnsemble(new Ensemble(ensemble.getIssueDate(), cumulativeVals, ensemble.getStartDateTime(), ensemble.getInterval(), ensemble.getUnits()));
                addStatisticsToTimePlot(chart, selectedStats, ets, dates);
                addLineMembersToChart(chart, cumulativeVals, dates, randomColor);
            }
            else
            {
                addStatisticsToTimePlot(chart, selectedStats, db.getEnsembleTimeSeries(selectedRid), dates);
                addLineMembersToChart(chart, vals, dates, randomColor);
            }
            return chart;

        } else {
            EnsembleChartAcrossEnsembles chart = new EnsembleChartAcrossEnsembles();
            chart.setXLabel("Ensembles");
            chart.setYLabel(String.join(" ", selectedRid.parameter, ensemble.getUnits()));
            addStatisticsToScatterPlot(chart, selectedStats, db.getEnsembleTimeSeries(selectedRid));
            return chart;
        }
    }

    private Statistics getSelectedTimeSeriesView(EnsembleViewStat[] selectedStats) {
        for (EnsembleViewStat stat : selectedStats) {
            if (stat.getStatUIType() == StatisticUIType.RADIOBUTTON && stat.hasInput())
                return stat.getStatType();
        }
        return null;
    }

    private boolean isTimeSeriesViewSelected(EnsembleViewStat[] selectedStats) {
        for (EnsembleViewStat stat : selectedStats) {
            if (stat.getStatUIType() == StatisticUIType.RADIOBUTTON && stat.hasInput() && stat.getStatType() != Statistics.NONE)
                return true;
        }
        return false;
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

    private Color randomColor(int i) {
        Random rand = new Random(i);
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        Color color = new Color(r, g, b);
        return color;
    }

    /*
    Calls the Compute Engine class to compute selected metrics and add metric to scatter plot for a given Point Specification and y-axis
    */

    private void addStatisticsToScatterPlot(EnsembleChartAcrossEnsembles chart, EnsembleViewStat[] stats, EnsembleTimeSeries ets) throws ParseException {
        for (EnsembleViewStat selectedStat : stats) {
            switch (selectedStat.getStatType()) {
                case MIN:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.ScatterPlot),
                                new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                    1.0f, new float[]{6.0f, 6.0f}, 0.0f), Color.RED, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MAX:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.ScatterPlot),
                                new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1.0f, new float[]{6.0f, 6.0f}, 0.0f), Color.BLUE, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEAN:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.ScatterPlot),
                                new BasicStroke(3.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEDIAN:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.ScatterPlot),
                                new BasicStroke(3.0f), Color.ORANGE, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case TOTAL:
                    chart.addPoint(
                            new PointSpec(1, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.ScatterPlot),
                                new BasicStroke(3.0f), Color.GRAY, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case PERCENTILE:
                    float[] percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for(int i = 0; i < percentiles.length; i++) {

                        chart.addPoint(
                                new PointSpec(0, computeEngine.computeTextBoxStat(ets, selectedStat.getStatType(), selectedZdt, new float[] {(percentiles[i])}, ChartType.ScatterPlot),
                                        new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                    1.0f, new float[]{6.0f, 6.0f}, 0.0f), randomColor(i+1), StatisticsStringMap.map.get(selectedStat.getStatType()) + " " + df.format(percentiles[i]*100) + "%"));
                    }
                    break;
                case MAXAVERAGEDURATION:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeTextBoxStat(db.getEnsembleTimeSeries(selectedRid), selectedStat.getStatType(), selectedZdt, ((TextBoxStat) selectedStat).getTextFieldValue(), ChartType.ScatterPlot),
                                new BasicStroke(3.0f), Color.PINK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MAXACCUMDURATION:
                    chart.addPoint(
                            new PointSpec(0, computeEngine.computeTextBoxStat(db.getEnsembleTimeSeries(selectedRid), selectedStat.getStatType(), selectedZdt, ((TextBoxStat) selectedStat).getTextFieldValue(), ChartType.ScatterPlot),
                                new BasicStroke(3.0f), Color.GREEN, StatisticsStringMap.map.get(selectedStat.getStatType())));
            }
        }
    }

    /*
    Calls the Compute Engine class to compute selected metrics and add metric to time series plot for a given Line Specification and y-axis
    */

    private void addStatisticsToTimePlot(EnsembleChartAcrossTime chart, EnsembleViewStat[] stats, EnsembleTimeSeries ets, ZonedDateTime[] dates) throws ParseException {
        for (EnsembleViewStat selectedStat : stats) {
            switch (selectedStat.getStatType()) {
                case MIN:
                case MAX:
                    chart.addLine(
                            new LineSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.TimePlot), dates,
                                    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                1.0f, new float[]{6.0f, 6.0f}, 0.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEAN:
                    chart.addLine(
                            new LineSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.TimePlot), dates,
                                new BasicStroke(3.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEDIAN:
                    chart.addLine(
                            new LineSpec(0, computeEngine.computeCheckBoxStat(ets, selectedStat.getStatType(), selectedZdt, ChartType.TimePlot), dates,
                                    new BasicStroke(3.0f), Color.BLUE, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case PERCENTILE:
                    float[] percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for(int i = 0; i < percentiles.length; i++) {
                        chart.addLine(
                                new LineSpec(0, computeEngine.computeTextBoxStat(ets, selectedStat.getStatType(), selectedZdt, new float[] {(percentiles[i])}, ChartType.TimePlot), dates,
                                        new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                    1.0f, new float[]{6.0f, 6.0f}, 0.0f), randomColor(i+1), StatisticsStringMap.map.get(selectedStat.getStatType()) + " " + df.format(percentiles[i]*100) + "%"));
                    }
                    break;
            }
        }
    }

    public EnsembleViewer() {
        /*
        Create panel that holds file name, location, and date/time information.
         */
        optionsPanel = new JPanel();
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        optionsPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Options", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)optionsPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        GridLayout experimentLayout = new GridLayout(0,2);
        optionsPanel.setLayout(experimentLayout);

        /*
        Create file select area.
         */
        optionsPanel.add(new JLabel("File"));
        filePathPanel = new JPanel();
        filePathPanel.setLayout(new GridLayout(0,2));
        filePath = new JTextField();
        filePath.setEditable(false);
        filePathPanel.add(filePath);
        fileSearchButton = new JButton();
        fileSearchButton.setText("Choose File...");
        filePathPanel.add(fileSearchButton);
        optionsPanel.add(filePathPanel);

        /*
        Create location combo box.
         */
        optionsPanel.add(new JLabel("Location"));
        locations = new JComboBox<>();
        optionsPanel.add(locations);

        /*
        Create date/time list combo box.
         */
        optionsPanel.add(new JLabel("Date/Time"));
        dateTimes = new JComboBox<>();
        optionsPanel.add(dateTimes);

        /*
        Create panel for holding options and metrics panels.
         */
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 1));
        topPanel.add(optionsPanel);

        /*
        Create tab specs and tabs in the tab pane.
         */
        createTabs();

        /*
        Setup window with options and graph.
         */
        frame = new JFrame();
        frame.setTitle("Ensemble Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(tabPane, BorderLayout.CENTER);
        frame.setSize(1000,1000);


      //  frame.pack();

        addActionListeners();
    }


    private void createTabs() {
        /*
        Create tab spec.
         */
        tabs.add(new TabSpec("Time Series Plot", new JPanel(), TabType.Chart));
        tabs.get(0).panel = new ChartTab(new EnsembleChartAcrossTime().generateChart(), new ComponentsPanel(ChartTypeStatisticsMap.map.get(ChartType.TimePlot)), ChartType.TimePlot);

        tabs.add(new TabSpec("Scatter Plot", new JPanel(), TabType.Chart));
        tabs.get(1).panel = new ChartTab(new EnsembleChartAcrossEnsembles().generateChart(), new ComponentsPanel(ChartTypeStatisticsMap.map.get(ChartType.ScatterPlot)), ChartType.ScatterPlot);

        tabs.add(new TabSpec("Single Value Summary", new JPanel(), TabType.SingleValueSummary));
        tabs.get(2).panel = new SingleValueSummaryTab();

        /*
        Create tabs in tab pane.
         */
        tabPane = new JTabbedPane();
        for(TabSpec tab: tabs) {
            tabPane.addTab(tab.tabName, tab.panel);
        }

    }

    private void addActionListeners() {
        /*
        Add listeners to file path button, locations combo box, date/time combo box, and statistics combo boxes.
         */
        fileSearchButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SQLite Database File", "db"));
            if (fileChooser.showOpenDialog(filePathPanel) == 0)
            {
                filePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                try {
                    db = new SqliteDatabase(fileChooser.getSelectedFile().getAbsolutePath(),
                            SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
                    computeEngine = new ComputeEngine();
                    List<RecordIdentifier> rids = db.getEnsembleTimeSeriesIDs();
                    String[] sRids = rids.stream().map(RecordIdentifier::toString).toArray(String[]::new);
                    ComboBoxModel<String> model = new DefaultComboBoxModel<>(sRids);
                    locations.setModel(model);
                    selectedRid = null;
                    selectedZdt = null;
                    //showEmptyChart(getCurrentlyShownChart());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        locations.addActionListener(e -> {
            setRidFromString(String.valueOf(locations.getSelectedItem()));
            setupDateTimeComboBox(dateTimes);
            setDateTimeFromString(String.valueOf(dateTimes.getSelectedItem()));
            tryShowingChart();
        });

        dateTimes.addActionListener(e -> {
            setDateTimeFromString(String.valueOf(dateTimes.getSelectedItem()));
            tryShowingChart();
        });

        for(TabSpec tab: tabs) {
            if (tab.tabType == TabType.Chart){
                ChartTab chartTab = ((ChartTab)tab.panel);
                for (Statistics stat : ChartTypeStatisticsMap.map.get(chartTab.chartType)) {
                    EnsembleViewStat evs = chartTab.componentsPanel.getStat(stat);
                    evs.addActionListener(e -> tryShowingChart());
                }
            } else if (tab.tabType == TabType.SingleValueSummary) {
                SingleValueSummaryTab summaryTab = ((SingleValueSummaryTab)tab.panel);
                summaryTab.computeButton.addActionListener(e -> tryShowingSingleValueSummary(summaryTab));
            }
        }

        tabPane.addChangeListener(e -> tryShowingChart());

    }

    private void tryShowingSingleValueSummary(SingleValueSummaryTab tab) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);
        float value = computeEngine.computeTwoStepComputable(ets, selectedZdt, tab.getFirstStat(), tab.getFirstTextFieldValue(),
                tab.getSecondStat(), tab.getSecondTextFieldValue(), tab.getChartType() == ChartType.TimePlot);
        if(tab.getChartType() == ChartType.TimePlot) {
            tab.writeLn(String.join(" ", "Computing",
                    tab.getFirstStat().toString() + " across all ensemble members for each time-step,",
                    "then computing " + tab.getSecondStat().toString() + " across all time-steps ",
                    "=", Float.toString(value)));
        } else {
            tab.writeLn(String.join(" ", "Computing",
                    tab.getFirstStat().toString() + " for each ensemble across all time-steps,",
                    "then computing " + tab.getSecondStat().toString() + " across all ensemble members ",
                    "=", Float.toString(value)));
        }
    }


    private EnsembleViewStat[] getSelectedStatistics() {
        List<EnsembleViewStat> selectedStats = new ArrayList<>();
        ChartTab chartTab = ((ChartTab)tabs.get(tabPane.getSelectedIndex()).panel);
        for (Statistics stat : ChartTypeStatisticsMap.map.get(chartTab.chartType)) {
            EnsembleViewStat selectedStat = getCurrentlyShownComponentsPanel().getStat(stat);
            if (selectedStat.hasInput()) {
                selectedStats.add(selectedStat);
            }
        }
        return selectedStats.toArray(new EnsembleViewStat[]{});
    }

    private JPanel getCurrentlyShownChart() {
        return ((ChartTab)tabs.get(tabPane.getSelectedIndex()).panel).chartPanel;
    }

    private ComponentsPanel getCurrentlyShownComponentsPanel() {
        return ((ChartTab)tabs.get(tabPane.getSelectedIndex()).panel).componentsPanel;
    }

}
