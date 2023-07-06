package hec.ensembleview;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensembleview.mappings.ChartTypeStatisticsMap;
import hec.ensembleview.tabs.ChartTab;
import hec.ensembleview.tabs.EnsembleTab;
import hec.ensembleview.tabs.SingleValueSummaryTab;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnsembleViewer {
    public EnsembleDatabase db;

    private RecordIdentifier selectedRid = null;
    private ZonedDateTime selectedZdt = null;

    private JFrame frame;
    private JPanel topPanel;
    private final List<TabSpec> tabs = new ArrayList<>();
    private JPanel optionsPanel;
    private JPanel filePathPanel;
    private JTextField filePath;
    private JButton fileSearchButton;
    private JComboBox<RecordIdentifier> locations;
    private JComboBox<ZonedDateTime> dateTimes;
    private JTabbedPane tabPane;

    public static void main(String[] args) {
        EnsembleViewer ev = new EnsembleViewer();
        ev.setVisible(true);
    }

    public void setVisible(boolean value) {
        frame.setVisible(value);
    }

    private void setDateTimeFromString(String date) {
        selectedZdt = getZonedDateTimeFromString(selectedRid, date);
    }

    private void setupDateTimeComboBox() {
        dateTimes.removeAllItems();
        List<ZonedDateTime> zdts = db.getEnsembleIssueDates(selectedRid);
        for (ZonedDateTime date : zdts)
            dateTimes.addItem(date);
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

        ChartTab tab = ((ChartTab)tabs.get(tabPane.getSelectedIndex()).panel);
        tab.tryShowingChart();
    }

    public EnsembleViewer() {
        /*
        Create panel that holds file name, location, and date/time information.
         */
        optionsPanel = new JPanel();
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        optionsPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Options", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)optionsPanel.getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
        GridLayout experimentLayout = new GridLayout(3,2);
        optionsPanel.setLayout(experimentLayout);

        /*
        Create file select area.
         */
        JLabel text = new JLabel("File");
        text.setFont(DefaultSettings.setSegoeFontText());
        optionsPanel.add(text);
        filePathPanel = new JPanel();
        filePathPanel.setLayout(new GridLayout(1,2));
        filePath = new JTextField();
        filePath.setFont(DefaultSettings.setSegoeFontText());
        filePath.setEditable(false);
        filePathPanel.add(filePath);
        fileSearchButton = new JButton();
        fileSearchButton.setText("Choose File...");
        fileSearchButton.setBackground(new Color(244, 242, 245));
        filePathPanel.add(fileSearchButton);
        optionsPanel.add(filePathPanel);

        /*
        Create location combo box.
         */
        optionsPanel.add(new JLabel("Location"));
        locations = new JComboBox<>();
        locations.setBackground(new Color(244,242,245));
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
        tabPane.setFont(DefaultSettings.setSegoeFontText());

    }

    private void addActionListeners() {
        /*
        Add listeners to file path button, locations combo box, date/time combo box, and statistics combo boxes.
         */
        fileSearchButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Database File", "db", "dss"));
            if (fileChooser.showOpenDialog(filePathPanel) == 0)
            {
                dateTimes.removeAllItems();
                locations.removeAllItems();

                String fileName = fileChooser.getSelectedFile().getAbsolutePath();
                filePath.setText(fileName);
                try {
                    int index = fileName.lastIndexOf('.');
                    String extension = fileName.substring(index + 1);
                    if (extension.equals("dss")) {
                        db = new DssDatabase(fileName);
                    } else if (extension.equals("db")) {
                        db = new SqliteDatabase(fileName, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
                    } else {
                        throw new Exception("File not supported");
                    }

                    for (TabSpec tab : tabs) {
                        EnsembleTab et = (EnsembleTab)tab.panel;
                        et.setDatabase(db);
                    }

                    locations.removeAllItems();
                    List<RecordIdentifier> rids = db.getEnsembleTimeSeriesIDs();
                    for (RecordIdentifier rid : rids)
                        locations.addItem(rid);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        locations.addActionListener(e -> {
            selectedRid = (RecordIdentifier)locations.getSelectedItem();
            setupDateTimeComboBox();
            selectedZdt = (ZonedDateTime)dateTimes.getSelectedItem();
            // update rid and zdt for each tab when new location is selected
            for (TabSpec tab : tabs) {
                EnsembleTab et = (EnsembleTab)tab.panel;
                et.setRecordIdentifier(selectedRid);
                et.setZonedDateTime(selectedZdt);
            }
            tryShowingChart();
        });

        dateTimes.addActionListener(e -> {
            setDateTimeFromString(String.valueOf(dateTimes.getSelectedItem()));
            // update zdt for each tab when new location is selected
            for (TabSpec tab : tabs) {
                EnsembleTab et = (EnsembleTab)tab.panel;
                et.setZonedDateTime(selectedZdt);
            }
            tryShowingChart();
        });

        tabPane.addChangeListener(e -> tryShowingChart());

    }
}
