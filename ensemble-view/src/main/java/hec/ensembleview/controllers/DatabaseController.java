package hec.ensembleview.controllers;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensembleview.DatabaseHandlerService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseController {
    Logger logger = Logger.getLogger(DatabaseController.class.getName());
    private static final DatabaseController instance = new DatabaseController();
    private JPanel filePathPanel;
    private JComboBox<ZonedDateTime> dateTimes;
    private JComboBox<RecordIdentifier> locations;
    private JTextField filePath;
    private DatabaseHandlerService databaseHandlerService;
    boolean isNewLoad;  //checks if new database is added. If database is new, set to true

    private DatabaseController() {
    }

    public void initialize(JPanel filePathPanel, JComboBox<ZonedDateTime> dateTimes, JComboBox<RecordIdentifier> locations,
                           JTextField filePath) {
        setFilePathPanel(filePathPanel);
        setDateTimes(dateTimes);
        setLocations(locations);
        setFilePath(filePath);
        setFilePathAndInitialRidZdt();
    }

    public static DatabaseController getInstance() {
        return instance;
    }

    public void setFilePathPanel(JPanel filePathPanel) {
        this.filePathPanel = filePathPanel;
    }

    public void setDateTimes(JComboBox<ZonedDateTime> dateTimes) {
        this.dateTimes = dateTimes;
    }

    public void setLocations(JComboBox<RecordIdentifier> locations) {
        this.locations = locations;
    }

    public void setFilePath(JTextField filePath) {
        this.filePath = filePath;
    }

    private void setFilePathAndInitialRidZdt() {
        databaseHandlerService = DatabaseHandlerService.getInstance();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Database File", "db", "dss"));

        if (fileChooser.showOpenDialog(filePathPanel) == 0) {
            dateTimes.removeAllItems();
            locations.removeAllItems();
            isNewLoad = true;

            String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            filePath.setText(fileName);
            try {
                int index = fileName.lastIndexOf('.');
                String extension = fileName.substring(index + 1);
                if (extension.equals("dss")) {
                    DssDatabase db = new DssDatabase(fileName);
                    databaseHandlerService.setDatabase(db);
                } else if (extension.equals("db")) {
                    SqliteDatabase db = new SqliteDatabase(fileName, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
                    databaseHandlerService.setDatabase(db);
                } else {
                    logger.log(Level.SEVERE, "File type not supported by Ensemble UI");
                }

                locations.removeAllItems();
                List<RecordIdentifier> rids = databaseHandlerService.getEnsembleDatabase().getEnsembleTimeSeriesIDs();
                for (RecordIdentifier rid : rids)
                    locations.addItem(rid);  // add the location to the combo box

                RecordIdentifier selectedRid = (RecordIdentifier)locations.getSelectedItem();
                databaseHandlerService.setDbHandlerRecordIdentifier(selectedRid);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setupDateTimeComboBox(RecordIdentifier selectedRid) {
        dateTimes.removeAllItems();
        List<ZonedDateTime> zdts = databaseHandlerService.getEnsembleDatabase().getEnsembleIssueDates(selectedRid);
        for (ZonedDateTime date : zdts)
            dateTimes.addItem(date);
    }

    public void setZdt() {
        ZonedDateTime selectedZdt = (ZonedDateTime) dateTimes.getSelectedItem();
        if(selectedZdt == null) {
            return;
        }
        databaseHandlerService.setDbHandlerZonedDateTime(selectedZdt);
    }

    public void setSelectedRid() {
        RecordIdentifier selectedRid = (RecordIdentifier)locations.getSelectedItem();
        if(selectedRid == null) {
            return;
        }
        databaseHandlerService.setDbHandlerRecordIdentifier(selectedRid);
        resetZdt(selectedRid);
    }

    public void setIsNewLoad(boolean isNewLoad) {
        this.isNewLoad = isNewLoad;
    }

    public boolean getIsNewLoad() {
        return this.isNewLoad;
    }

    private void resetZdt(RecordIdentifier selectedRid) {
//        databaseHandlerService.setDbHandlerZonedDateTime(null);  //reset zdt to empty
        setZdtList(selectedRid);  //update zdt to new list
        databaseHandlerService.setDbHandlerZonedDateTime((ZonedDateTime) dateTimes.getSelectedItem());
    }

    private void setZdtList(RecordIdentifier selectedRid) {
        setupDateTimeComboBox(selectedRid);
    }
}

