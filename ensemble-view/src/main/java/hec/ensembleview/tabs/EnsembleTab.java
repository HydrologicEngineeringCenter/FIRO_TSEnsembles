package hec.ensembleview.tabs;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;

import java.time.ZonedDateTime;

public interface EnsembleTab {
    void setDatabase(EnsembleDatabase db);
    void setRecordIdentifier(RecordIdentifier rid);
    void setZonedDateTime(ZonedDateTime zdt);
}
