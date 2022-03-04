package hec.ensembleview;

import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.ensembleview.mappings.StatisticsStringMap;
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
    private EnsembleViewerModel model;
    private EnsembleChart ec;

    private RecordIdentifier selectedRid = null;
    private ZonedDateTime selectedZdt = null;

    private JFrame frame;
    private JPanel topPanel;
    private JPanel chartPanel;
    private JPanel optionsPanel;
    private JPanel filePathPanel;
    private JTextField filePath;
    private JButton fileSearchButton;
    private JComboBox<String> locations;
    private JComboBox<String> dateTimes;
    private StatisticsPanel statsPanel;

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
        String[] zdts = model.db.getEnsembleIssueDates(selectedRid).stream().map(ZonedDateTime::toString).toArray(String[]::new);
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

    public void setModel(String dbFile) throws Exception {
        model = new EnsembleViewerModel(dbFile);
    }

    private RecordIdentifier getRecordIdentifierFromString(String stringRID){
        List<RecordIdentifier> rids = model.db.getEnsembleTimeSeriesIDs();
        for (RecordIdentifier rid : rids) {
            if (Objects.equals(rid.toString(), stringRID)) {
                return rid;
            }
        }
        return null;
    }

    private ZonedDateTime getZonedDateTimeFromString(RecordIdentifier rid ,String stringZDT){
        List<ZonedDateTime> zdts = model.db.getEnsembleIssueDates(rid);
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
        chartPanel.add(ec.generateChart(), BorderLayout.CENTER);
        chartPanel.repaint();
    }

    private EnsembleChart createChart() throws Exception {
        if (selectedRid == null || selectedZdt == null) {
            return null;
        }

        Ensemble ensemble = model.db.getEnsemble(selectedRid, selectedZdt);
        EnsembleChart chart = new EnsembleChartAcrossTime();
        chart.setXLabel("Date/Time");
        chart.setYLabel(String.join(" ", selectedRid.parameter, ensemble.getUnits()));
        float[][] vals = ensemble.getValues();
        EnsembleViewStat[] selectedStats = getSelectedStatistics();
        ZonedDateTime[] dates = ensemble.startDateTime();
        addStatisticsToChart(chart, selectedStats, dates);
        boolean randomColor = selectedStats.length <= 0;
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
            chart.addLine(new LineSpec(0, vals[i], dates, new BasicStroke(1.0f), c, "Member " + (i + 1)));
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


    private void addStatisticsToChart(EnsembleChart chart, EnsembleViewStat[] stats, ZonedDateTime[] dates) throws ParseException {
        for (EnsembleViewStat selectedStat : stats) {
            switch (selectedStat.getStatType()) {
                case MIN:
                case MAX:
                    chart.addLine(new LineSpec(0, model.computeCheckBoxStat(selectedStat.getStatType(), selectedRid, selectedZdt),
                            dates, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1.0f, new float[]{6.0f, 6.0f}, 0.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEAN:
                    chart.addLine(new LineSpec(0, model.computeCheckBoxStat(selectedStat.getStatType(), selectedRid, selectedZdt),
                            dates, new BasicStroke(3.0f), Color.BLACK, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case MEDIAN:
                    chart.addLine(new LineSpec(0, model.computeCheckBoxStat(selectedStat.getStatType(), selectedRid, selectedZdt),
                            dates, new BasicStroke(3.0f), Color.BLUE, StatisticsStringMap.map.get(selectedStat.getStatType())));
                    break;
                case PERCENTILE:
                    float[] percentiles = ((TextBoxStat) selectedStat).getTextFieldValue();
                    DecimalFormat df = new DecimalFormat("0.0");

                    for(int i = 0; i < percentiles.length; i++) {

                        chart.addLine(new LineSpec(0, model.computeTextBoxStat(selectedStat.getStatType(), selectedRid, selectedZdt, new float[] {(percentiles[i])}),
                                dates, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                1.0f, new float[]{6.0f, 6.0f}, 0.0f), randomColor(i+1), StatisticsStringMap.map.get(selectedStat.getStatType()) + " " + df.format(percentiles[i]*100) + "%"));
                    }
                    break;
                case MAXAVERAGEDURATION:
                    chart.addLine(new LineSpec(0, model.computeTextBoxStat(selectedStat.getStatType(), selectedRid, selectedZdt, ((TextBoxStat) selectedStat).getTextFieldValue()),
                            dates, new BasicStroke(3.0f), Color.PINK, StatisticsStringMap.map.get(selectedStat.getStatType())));

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
        Create statistics panel.
         */
        statsPanel = new StatisticsPanel();

        /*
        Create panel for holding options and metrics panels.
         */
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1));
        topPanel.add(optionsPanel);
        topPanel.add(statsPanel.getPanel());

        /*
        Create panel for holding chart panel.
         */
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(new EnsembleChartAcrossTime().generateChart());

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
                    setModel(fileChooser.getSelectedFile().getAbsolutePath());
                    List<RecordIdentifier> rids = model.db.getEnsembleTimeSeriesIDs();
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

        Statistics[] stats = Statistics.values();
        for (Statistics stat : stats) {
            EnsembleViewStat cb = statsPanel.getStat(stat);
            cb.addActionListener(e -> tryShowingChart(chartPanel));
        }
    }

    private EnsembleViewStat[] getSelectedStatistics() {
        List<EnsembleViewStat> selectedStats = new ArrayList<>();
        Statistics[] stats = Statistics.values();
        for (Statistics stat : stats) {
            EnsembleViewStat selectedStat = statsPanel.getStat(stat);
            if (selectedStat.hasInput()) {
                selectedStats.add(selectedStat);
            }
        }
        return selectedStats.toArray(new EnsembleViewStat[]{});
    }

}
