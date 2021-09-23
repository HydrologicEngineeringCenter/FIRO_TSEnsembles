package hec.ensembleview;

import com.sun.prism.impl.Disposer;
import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.Ensemble;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class EnsembleViewer {
    private SqliteDatabase db;
    private EnsembleChart ec;

    private RecordIdentifier selectedRid = null;
    private ZonedDateTime selectedZdt = null;

    public static void main(String[] args) {
        EnsembleViewer ev = new EnsembleViewer();
        EnsembleChart chart = new EnsembleJFreeChart();

        JPanel topPanel = new JPanel();
        GridLayout experimentLayout = new GridLayout(0,2);
        topPanel.setLayout(experimentLayout);

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

        topPanel.add(new JLabel("Location"));
        JComboBox<String> locations = new JComboBox<>();
        topPanel.add(locations);

        topPanel.add(new JLabel("Date/Time"));
        JComboBox<String> dateTimes = new JComboBox<>();
        topPanel.add(dateTimes);

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(chart.getChart());
        frame.add(chartPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

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
            chartPanel.add(chart.getChart(), BorderLayout.CENTER);
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
