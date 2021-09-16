package hec;

import java.time.ZonedDateTime;
import java.util.List;
import hec.ensemble.*;

public interface EnsembleDatabase extends AutoCloseable {
    Ensemble getEnsemble(RecordIdentifier timeseriesID, ZonedDateTime issue_time);
    EnsembleTimeSeries getEnsembleTimeSeries(RecordIdentifier timeseriesID);
    List<ZonedDateTime> getEnsembleIssueDates(RecordIdentifier timeseriesID);
    void write(EnsembleTimeSeries[] etsArray) throws Exception;
    void write(EnsembleTimeSeries ets) throws Exception;
    List<RecordIdentifier> getTimeSeriesIDs();
}
