package hec.dss.ensemble;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.heclib.dss.HecDss;
import hec.io.TimeSeriesCollectionContainer;
import hec.io.TimeSeriesContainer;


/**
 * DssDatabase implements EnsembleDatabase.
 * It is used to work with time-series of ensembles.
 * Data is stored in a DSS file, using the Collections and Version features of the DSS F-Part tag.
 */
public class DssDatabase implements EnsembleDatabase{
    private String dssFileName;
    public DssDatabase(String dssFileName){
        this.dssFileName= dssFileName;
    }

    private static String buildDssPath(RecordIdentifier recordID, java.time.ZonedDateTime issue_time){
        // to do define translation
        return "//"+recordID.location+"/"+recordID.parameter+"///"+buildFpart(issue_time);
    }

    /**
     * Builds F part of DSS path
     * We are not including C:000001-- TimeSeriesCollectionContainer will do that
     * @param version
     * @return
     */
    private static String buildFpart(java.time.ZonedDateTime version){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "V:"+formatter.format(version);
    }
    /* We may not need this version that includes member/collection C:000001
    private static String buildFpart(int member , java.time.ZonedDateTime t){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "C:"+String.format("C:%06d", member)+"|V:"+formatter.format(t);
    }*/

    public Ensemble getEnsemble(RecordIdentifier recordID, ZonedDateTime issue_time){
        Ensemble rval = null; //new hec.ensemble.Ensemble();
        //this.location = location;
        //this.parameter = parameter;
        // DSS.get(/recordID.location/,issue_time)

        return rval;
    }
    public EnsembleTimeSeries getEnsembleTimeSeries(RecordIdentifier recordID){
    return null;
    }
    public java.util.List<java.time.ZonedDateTime> getEnsembleIssueDates(RecordIdentifier recordID) {
        return null;
    }
    public void write(EnsembleTimeSeries[] etsArray) throws Exception{
        for (EnsembleTimeSeries ets: etsArray){
            write(ets);
        }
    }
    public void write(EnsembleTimeSeries ets) throws Exception{
        // get access to DSS
        HecDss dss = HecDss.open(dssFileName);
        //translate EnsembleTimeSeries into TimeSeriesCollectionContainer
        TimeSeriesCollectionContainer containers = loadContainers(ets);
        // write TimeSeriesCollectionContainer to DSS file
        dss.write(containers);
        // close resources
        dss.close();
        System.out.println("done.");
    }
    private TimeSeriesCollectionContainer loadContainers(EnsembleTimeSeries ets){

        TimeSeriesCollectionContainer rval = new hec.io.TimeSeriesCollectionContainer();
        for (Ensemble e: ets){
            float[][] values = e.getValues();
            for (int row = 0; row <values.length ; row++) {
                TimeSeriesContainer tsc = new TimeSeriesContainer();
                tsc.values = convertFloatsToDoubles(values[row]);
                tsc.setFullName(buildDssPath(ets.getTimeSeriesIdentifier(),e.getIssueDate()));
                //tsc. other STUFF>>>
                rval.add(tsc);
            }
        }
        rval.finishedAdding();
        return rval;
    }



    //https://stackoverflow.com/questions/2019362/how-to-convert-array-of-floats-to-array-of-doubles-in-java
    private static double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }

    /**
     * getEnsembleTimeSeriesIDs returns a list of RecordIdentifier's
     *
     * @return
     */
    public java.util.List<RecordIdentifier> getEnsembleTimeSeriesIDs(){
    return null;
    }


    @Override
    public void close() throws Exception {

    }
}