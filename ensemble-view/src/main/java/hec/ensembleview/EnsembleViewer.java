package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.MultiStatComputable;
import hec.stats.Statistics;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

public class EnsembleViewer {
    private SqliteDatabase db;
    private EnsembleChart ec;

    private RecordIdentifier selectedRid = null;
    private ZonedDateTime selectedZdt = null;

    private boolean minFlag = false;
    private boolean maxFlag = false;
    private boolean meanFlag = false;

    private JFrame frame;
    private JPanel topPanel;
    private JPanel chartPanel;
    private JPanel optionsPanel;
    private JPanel filePathPanel;
    private JTextField filePath;
    private JButton fileSearchButton;
    private JComboBox<String> locations;
    private JComboBox<String> dateTimes;
    private JPanel statsPanel;
    private JCheckBox minCheckbox;
    private JCheckBox maxCheckbox;
    private JCheckBox meanCheckbox;

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
        chartPanel.add(new EnsembleJFreeChart().getChart(), BorderLayout.CENTER);
        chartPanel.repaint();
    }

    public void setDatabase(String absoluteFile) throws Exception {
        db = new SqliteDatabase(absoluteFile, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
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

    private void tryShowingChart(JPanel chartPanel) {
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
        chartPanel.add(ec.getChart(), BorderLayout.CENTER);
        chartPanel.repaint();
    }

    private EnsembleChart createChart() throws Exception {
        if (selectedRid == null || selectedZdt == null) {
            return null;
        }

        Ensemble ensemble = db.getEnsemble(selectedRid, selectedZdt);
        EnsembleChart chart = new EnsembleJFreeChart();
        chart.setXLabel("Date/Time");
        chart.setYLabel(String.join(" ", selectedRid.parameter, ensemble.getUnits()));
        float[][] vals = ensemble.getValues();
        Statistics[] selectedStats = getSelectedStatistics();
        float[][] stats = getStatistics(selectedStats);
        ZonedDateTime[] dates = ensemble.startDateTime();
        addStatisticsToChart(chart, stats, selectedStats, dates);
        boolean randomColor = stats.length <= 0;
        addMembersToChart(chart, vals, dates, randomColor);
        return chart;
    }

    private void addMembersToChart(EnsembleChart chart, float[][] vals, ZonedDateTime[] dates, boolean randomColor) throws ParseException {
        Color c = null;
        if (!randomColor) {
            c = Color.blue;
            int alpha = 50;
            int cInt = (c.getRGB() & 0xffffff) | (alpha << 24);
            c = new Color(cInt, true);

        }
        for (int i = 0; i < vals.length; i++) {
            chart.addLine(new LineSpec(vals[i], dates, new BasicStroke(1.0f), c, "Member " + (i + 1)));
        }

    }

    private void addStatisticsToChart(EnsembleChart chart, float[][] stats, Statistics[] selectedStats, ZonedDateTime[] dates) throws ParseException {
        for (int i = 0; i < selectedStats.length; i++) {
            switch (selectedStats[i]){
                case MIN:
                    chart.addLine(new LineSpec(stats[i], dates, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1.0f, new float[] {6.0f, 6.0f}, 0.0f), Color.BLACK, "MIN"));
                    break;
                case MAX:
                    chart.addLine(new LineSpec(stats[i], dates, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1.0f, new float[] {6.0f, 6.0f}, 0.0f), Color.BLACK, "MAX"));
                    break;
                case MEAN:
                    chart.addLine(new LineSpec(stats[i], dates, new BasicStroke(3.0f), Color.BLACK, "MEAN"));
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
        Create metrics panel.
         */
        statsPanel = new JPanel();
        statsPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)statsPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        statsPanel.setLayout(new GridLayout(0,1));
        minCheckbox = new JCheckBox("Min");
        maxCheckbox = new JCheckBox("Max");
        meanCheckbox = new JCheckBox("Mean");
        statsPanel.add(minCheckbox);
        statsPanel.add(maxCheckbox);
        statsPanel.add(meanCheckbox);

        /*
        Create panel for holding options and metrics panels.
         */
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1));
        topPanel.add(optionsPanel);
        topPanel.add(statsPanel);

        /*
        Create panel for holding chart panel.
         */
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(new EnsembleJFreeChart().getChart());

        /*
        Setup window with options and graph.
         */
        frame = new JFrame();
        frame.setTitle("Ensemble Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(chartPanel, BorderLayout.CENTER);
        frame.pack();

        addActionListeners();
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
                    setDatabase(fileChooser.getSelectedFile().getAbsolutePath());
                    List<RecordIdentifier> rids = db.getEnsembleTimeSeriesIDs();
                    String[] sRids = rids.stream().map(RecordIdentifier::toString).toArray(String[]::new);
                    ComboBoxModel<String> model = new DefaultComboBoxModel<>(sRids);
                    locations.setModel(model);
                    selectedRid = null;
                    selectedZdt = null;
                    showEmptyChart(chartPanel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        locations.addActionListener(e -> {
            setRidFromString(String.valueOf(locations.getSelectedItem()));
            setupDateTimeComboBox(dateTimes);
            setDateTimeFromString(String.valueOf(dateTimes.getSelectedItem()));
            tryShowingChart(chartPanel);
        });

        dateTimes.addActionListener(e -> {
            setDateTimeFromString(String.valueOf(dateTimes.getSelectedItem()));
            tryShowingChart(chartPanel);
        });

        minCheckbox.addActionListener(e -> {
            minFlag = minCheckbox.isSelected();
            tryShowingChart(chartPanel);
        });

        maxCheckbox.addActionListener(e -> {
            maxFlag = maxCheckbox.isSelected();
            tryShowingChart(chartPanel);
        });

        meanCheckbox.addActionListener(e -> {
            meanFlag = meanCheckbox.isSelected();
            tryShowingChart(chartPanel);
        });
    }

    private Statistics[] getSelectedStatistics() {
        List<Statistics> selectedStats = new ArrayList<>();
        if (minFlag) selectedStats.add(Statistics.MIN);
        if (maxFlag) selectedStats.add(Statistics.MAX);
        if (meanFlag) selectedStats.add(Statistics.MEAN);
        return selectedStats.toArray(new Statistics[]{});
    }

    private float[][] getStatistics(Statistics[] wantedStats) {
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(selectedRid);

        MetricCollectionTimeSeries mct = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(
                new MultiStatComputable(wantedStats));

        MetricCollection mc = mct.getMetricCollection(selectedZdt);
        return mc.getValues();
    }


}
