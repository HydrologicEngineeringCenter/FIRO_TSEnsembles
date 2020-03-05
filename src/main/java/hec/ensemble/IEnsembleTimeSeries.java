package hec.ensemble;

import java.time.ZonedDateTime;

interface IEnsembleTimeSeries {

    public Ensemble getEnsemble(ZonedDateTime issueDate);
    public String getUnits();
    public String getDataType();
    public String getVersion();
    public TimeSeriesIdentifier getTimeSeriesIdentifier();

}
