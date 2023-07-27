package hec.ensembleview.controllers;

import hec.RecordIdentifier;

import javax.swing.*;
import java.time.ZonedDateTime;

public interface DatabaseListener {
    void initialize(JPanel filePathPanel, JComboBox<ZonedDateTime> dateTimes, JComboBox<RecordIdentifier> locations,
                    JTextField filePath);

    void setIsNewLoad(boolean newLoad);
    void setFilePathPanel(JPanel filePath);
    void setSelectedRid();
    void setZdt();

    boolean getIsNewLoad();



}
