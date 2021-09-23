package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.Ensemble;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.List;

public class EnsembleViewer {
    private SqliteDatabase db;
    private EnsembleChart ec;

    private RecordIdentifier selectedRid = null;
    private ZonedDateTime selectedZdt = null;

    public static void main(String[] args) {
        EnsembleViewer ev = new EnsembleViewer();
        EnsembleChart emptyChart = new EnsembleJFreeChart();

        /*
        Create panel that holds file name, location, and date/time information.
         */
        JPanel topPanel = new JPanel();
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Options", TitledBorder.LEFT, TitledBorder.TOP));
        System.out.println(((TitledBorder)topPanel.getBorder()).getTitleFont().getFontName());
        ((TitledBorder)topPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        GridLayout experimentLayout = new GridLayout(0,2);
        topPanel.setLayout(experimentLayout);

        /*
        Create file select area.
         */
        topPanel.add(new JLabel("File"));
        JPanel filePathPanel = new JPanel();
        filePathPanel.setLayout(new GridLayout(0,2));
        JTextField filePath = new JTextField();
        filePath.setEditable(false);
        filePathPanel.add(filePath);
        JButton fileSearchButton = new JButton();
        fileSearchButton.setText("Choose File...");
        filePathPanel.add(fileSearchButton);
        topPanel.add(filePathPanel);

        /*
        Create location combo box.
         */
        topPanel.add(new JLabel("Location"));
        JComboBox<String> locations = new JComboBox<>();
        topPanel.add(locations);

        /*
        Create date/time list combo box.
         */
        topPanel.add(new JLabel("Date/Time"));
        JComboBox<String> dateTimes = new JComboBox<>();
        topPanel.add(dateTimes);

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
                ev.ec = ev.createChart(filePath.getText(),
                        String.valueOf(locations.getSelectedItem()),
                        String.valueOf(dateTimes.getSelectedItem()));
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

    private EnsembleChart createChart(String fileName, String location, String dateTime) throws Exception {
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


}
