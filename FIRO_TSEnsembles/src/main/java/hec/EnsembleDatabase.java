package hec;

import java.time.ZonedDateTime;
import java.util.List;
import hec.ensemble.*;

public interface EnsembleDatabase extends AutoCloseable {
    Ensemble getEnsemble(RecordIdentifier recordID, ZonedDateTime issue_time);
    EnsembleTimeSeries getEnsembleTimeSeries(RecordIdentifier recordID);
    List<ZonedDateTime> getEnsembleIssueDates(RecordIdentifier recordID);
    void write(EnsembleTimeSeries[] etsArray) throws Exception;
    void write(EnsembleTimeSeries ets) throws Exception;
    List<RecordIdentifier> getEnsembleTimeSeriesIDs();
    String getFileName();
}
