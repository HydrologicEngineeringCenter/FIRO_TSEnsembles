package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.Ensemble;
import hec.stats.MultiStatComputable;
import hec.stats.Statistics;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.time.ZonedDateTime;
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

    public static void main(String[] args) {
        EnsembleViewer ev = new EnsembleViewer();
        EnsembleChart emptyChart = new EnsembleJFreeChart();

        /*
        Create panel that holds file name, location, and date/time information.
         */
        JPanel optionsPanel = new JPanel();
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        optionsPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Options", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)optionsPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        GridLayout experimentLayout = new GridLayout(0,2);
        optionsPanel.setLayout(experimentLayout);

        /*
        Create file select area.
         */
        optionsPanel.add(new JLabel("File"));
        JPanel filePathPanel = new JPanel();
        filePathPanel.setLayout(new GridLayout(0,2));
        JTextField filePath = new JTextField();
        filePath.setEditable(false);
        filePathPanel.add(filePath);
        JButton fileSearchButton = new JButton();
        fileSearchButton.setText("Choose File...");
        filePathPanel.add(fileSearchButton);
        optionsPanel.add(filePathPanel);

        /*
        Create location combo box.
         */
        optionsPanel.add(new JLabel("Location"));
        JComboBox<String> locations = new JComboBox<>();
        optionsPanel.add(locations);

        /*
        Create date/time list combo box.
         */
        optionsPanel.add(new JLabel("Date/Time"));
        JComboBox<String> dateTimes = new JComboBox<>();
        optionsPanel.add(dateTimes);

        /*
        Create metrics panel.
         */
        JPanel metricsPanel = new JPanel();
        metricsPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)metricsPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        metricsPanel.setLayout(new GridLayout(0,1));
        JCheckBox minCheckbox = new JCheckBox("Min");
        JCheckBox maxCheckbox = new JCheckBox("Max");
        JCheckBox meanCheckbox = new JCheckBox("Mean");
        metricsPanel.add(minCheckbox);
        metricsPanel.add(maxCheckbox);
        metricsPanel.add(meanCheckbox);

        /*
        Create panel for holding options and metrics panels.
         */
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1));
        topPanel.add(optionsPanel);
        topPanel.add(metricsPanel);

        /*
        Create panel for holding chart panel.
         */
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(emptyChart.getChart());

        /*
        Setup window with options and graph.
         */
        JFrame frame = new JFrame();
        frame.setTitle("Ensemble Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(chartPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        /*
        Add listeners to file path button, locations combo box, and date/time combo box.
         */
        fileSearchButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SQLite Database File", "db"));
            if (fileChooser.showOpenDialog(filePathPanel) == 0)
            {
                filePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                try {
                    ev.setDatabase(fileChooser.getSelectedFile().getAbsolutePath());
                    List<RecordIdentifier> rids = ev.db.getEnsembleTimeSeriesIDs();
                    String[] sRids = rids.stream().map(RecordIdentifier::toString).toArray(String[]::new);
                    ComboBoxModel<String> model = new DefaultComboBoxModel<>(sRids);
                    locations.setModel(model);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        locations.addActionListener(e -> {
            ev.selectedRid = ev.getRecordIdentifierFromString(String.valueOf(locations.getSelectedItem()));
            ev.selectedZdt = null;
            String[] zdts = ev.db.getEnsembleIssueDates(ev.selectedRid).stream().map(ZonedDateTime::toString).toArray(String[]::new);
            ComboBoxModel<String> model = new DefaultComboBoxModel<>(zdts);
            dateTimes.setModel(model);
            chartPanel.removeAll();
            chartPanel.revalidate();
            chartPanel.setLayout(new BorderLayout());
            chartPanel.add(emptyChart.getChart(), BorderLayout.CENTER);
            chartPanel.repaint();
        });

        dateTimes.addActionListener(e -> {
            ev.selectedZdt = ev.getZonedDateTimeFromString(ev.selectedRid, String.valueOf(dateTimes.getSelectedItem()));
            try {
                ev.ec = ev.createChart(filePath.getText());
                if (ev.ec == null){
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            chartPanel.removeAll();
            chartPanel.revalidate();
            chartPanel.setLayout(new BorderLayout());
            chartPanel.add(ev.ec.getChart(), BorderLayout.CENTER);
            chartPanel.repaint();
        });

        minCheckbox.addActionListener(e -> ev.minFlag = minCheckbox.isSelected());

        maxCheckbox.addActionListener(e -> ev.maxFlag = minCheckbox.isSelected());

        meanCheckbox.addActionListener(e -> ev.meanFlag = minCheckbox.isSelected());
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

    private EnsembleChart createChart(String fileName) throws Exception {
        if (Objects.equals(fileName, "") || selectedRid == null || selectedZdt == null) {
            return null;
        }

        Ensemble ensemble = db.getEnsemble(selectedRid, selectedZdt);

        EnsembleChart chart = new EnsembleJFreeChart();
        chart.setXLabel("Date/Time");
        chart.setYLabel(String.join(" ", selectedRid.parameter, ensemble.getUnits()));
        float[][] vals = ensemble.getValues();
        ZonedDateTime[] dates = ensemble.startDateTime();
        for (int i = 0; i < vals.length; i++) {
            chart.addLine(vals[i], dates, "Member " + (i + 1));
        }

        return chart;
    }

    public EnsembleViewer(String database) throws Exception {
        db = new SqliteDatabase(database, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
    }

    public EnsembleViewer() {

    }

    private float[][] getStatistics(Ensemble ensemble) {


        Statistics[] wantedStats = new Statistics[] {Statistics.MIN, Statistics.MAX, Statistics.MEAN};
        float[][] retrievedStats = ensemble.multiComputeForTracesAcrossTime(
                new MultiStatComputable(
                        wantedStats));

        float[][] returnStats = new float[wantedStats.length][retrievedStats.length];
        for (int i = 0; i < retrievedStats.length; i++){
            for (int j = 0; j < wantedStats.length; j++){
                returnStats[j][i] = retrievedStats[i][j];
            }
        }

        return returnStats;
    }


}
