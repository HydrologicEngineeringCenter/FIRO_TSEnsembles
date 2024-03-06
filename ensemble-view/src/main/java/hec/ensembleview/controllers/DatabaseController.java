package hec.ensembleview.controllers;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.ParameterFilter;
import hec.ensembleview.viewpanels.OptionsPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseController {
    private final Logger logger = Logger.getLogger(DatabaseController.class.getName());
    private JPanel filePathPanel;
    private JComboBox<ZonedDateTime> dateTimes;
    private JComboBox<RecordIdentifier> locations;
    private JTextField filePath;
    private DatabaseHandlerService databaseHandlerService;
    private boolean isNewLoad;  //checks if new database is added. If database is new, set to true

    public DatabaseController(OptionsPanel optionsPanel) {
        initiateDatabaseListener(optionsPanel);
    }

    private void initiateDatabaseListener(OptionsPanel optionsPanel) {
        optionsPanel.setDatabaseListener(new DatabaseListener() {
            @Override
            public void initialize(JPanel filePathPanel, JComboBox<ZonedDateTime> dateTimes, JComboBox<RecordIdentifier> locations, JTextField filePath) {
                setFilePathPanel(filePathPanel);
                setDateTimes(dateTimes);
                setLocations(locations);
                setFilePath(filePath);
                setFilePathAndInitialRidZdt();
            }

            @Override
            public void setIsNewLoad(boolean newLoad) {
                isNewLoad = newLoad;
            }

            @Override
            public void setFilePathPanel(JPanel filePath) {
                filePathPanel = filePath;
            }

            @Override
            public void setSelectedRid() {
                RecordIdentifier selectedRid = (RecordIdentifier)locations.getSelectedItem();
                if(selectedRid == null) {
                    return;
                }
                locations.setToolTipText(selectedRid.toString());
                databaseHandlerService.setDbHandlerRecordIdentifier(selectedRid);
                resetZdt(selectedRid);
            }

            @Override
            public void setZdt() {
                // If "T" tag does not exist in dss fPart, disable combo box
                dateTimes.setEnabled(DatabaseHandlerService.getInstance().getIsIssueDateAvailableForDss());

                ZonedDateTime selectedZdt = (ZonedDateTime) dateTimes.getSelectedItem();
                if(selectedZdt == null) {
                    return;
                }
                databaseHandlerService.setDbHandlerZonedDateTime(selectedZdt);
            }

            @Override
            public boolean getIsNewLoad() {
                return isNewLoad;
            }
        });
    }

    private void setDateTimes(JComboBox<ZonedDateTime> dateTimes) {
        this.dateTimes = dateTimes;
    }

    private void setLocations(JComboBox<RecordIdentifier> locations) {
        this.locations = locations;
    }

    private void setFilePath(JTextField filePath) {
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
                for (RecordIdentifier rid : rids) {
                    RecordIdentifier filteredRid = ParameterFilter.checkParameter(rid);
                    if (filteredRid != null) {
                        locations.addItem(filteredRid);  // add the location to the combo box
                    }
                }
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

    private void resetZdt(RecordIdentifier selectedRid) {
        setZdtList(selectedRid);  //update zdt to new list
        databaseHandlerService.setDbHandlerZonedDateTime((ZonedDateTime) dateTimes.getSelectedItem());
    }

    private void setZdtList(RecordIdentifier selectedRid) {
        setupDateTimeComboBox(selectedRid);
    }
}

