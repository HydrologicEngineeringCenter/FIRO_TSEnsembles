package hec.dss.ensemble;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.MetricDatabase;
import hec.ensemble.EnsembleTimeSeries;

import hec.heclib.dss.HecDss;
import hec.heclib.dss.HecDssCatalog;
import hec.heclib.dss.HecTimeSeries;
import hec.io.TimeSeriesCollectionContainer;
import hec.io.TimeSeriesContainer;
import hec.heclib.util.HecTime;

/**
 * DssDatabase implements EnsembleDatabase.
 * It is used to work with time-series of ensembles.
 * Data is stored in a DSS file, using the Collections and Version features of the DSS F-Part tag.
 */
public class DssDatabase implements EnsembleDatabase,MetricDatabase {
    private String dssFileName;
    public DssDatabase(String dssFileName){
        this.dssFileName= dssFileName;
    }

    private static String getEPart(int minutes){
        int[] status = new int[1];
        String ePart = hec.heclib.util.Heclib.getEPartFromInterval( minutes, status );
        return ePart;
    }

    // blank A/location/parameter//interval/C:000003|T:YYYYMMDD-hhmm|V:YYYYMMDD-hhmmss| f-part extra  /

    /**
     * translation of ensemble information to a DSS Path
     * @param e
     * @param memberNumber
     * @param recordID
     * @return
     */
    private static String buildDssPath(Ensemble e,int memberNumber, RecordIdentifier recordID ){

        int minutes = (int)e.getInterval().toMinutes();
        String ePart = getEPart(minutes);

        return "//"+recordID.location+"/"+recordID.parameter+"/"
                + "/"+ePart+"/"+buildFpart(memberNumber,
                  e.getStartDateTime(),e.getIssueDate())+"/";
    }

    /**
     * Builds F part of DSS path
     *
     * C - value is 6 alphanumeric characters
     * T - T: timestamp where timestamp = time of forecast format YYYYMMDD-hhmm
     * N - string where string is the forecast name
     * V - timestamp where timestamp = version time YYYYMMDD-hhmmss
     * R - string where string is the CWMS Run ID.
     *     value is even number of characters and each 2-character pair
     *     must be either "--" or an upper or lower case alphabetic
     *     character followed by a digit character (not restricted to '0')

     * @param t - time stamp saved in T: tag in F part
     * @param v - version of ensemble - (issue_date)
     * @param  member - ensemble member number to be in C: tag in F part
     * @return
     */
    private static String buildFpart(int member , ZonedDateTime t, ZonedDateTime v){
        DateTimeFormatter Tformatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        DateTimeFormatter Vformatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return String.format("C:%06d", member)
                +"|T:"+Tformatter.format(t)
                +"|V:"+Vformatter.format(t)+"|";
    }

    public Ensemble getEnsemble(RecordIdentifier recordID, ZonedDateTime issue_time){
        TimeSeriesCollectionContainer tscc = new TimeSeriesCollectionContainer();
        String dssPath = "" ; // to DO ... buildDssPath()
        tscc.setFullName(dssPath);
        HecTimeSeries dss = new hec.heclib.dss.HecTimeSeries(this.dssFileName);
        // try reading TimeSeriesCollectionContainer,
        // we can loop over collection and read individual members if necessary.
        dss.read(tscc,false);

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
        HecTimeSeries dss = new hec.heclib.dss.HecTimeSeries(dssFileName);
        //translate EnsembleTimeSeries into TimeSeriesCollectionContainer
        TimeSeriesCollectionContainer containers = loadContainers(ets);
        // write TimeSeriesCollectionContainer to DSS file
        dss.write(containers);
//        for (int i = 0; i < containers.size(); i++) {
  //          TimeSeriesContainer tsc = containers.get(i);
            //hec.heclib.dss.HecDataManager.setMessageLevel(15);
//            dss.write(tsc);
    //    }

        // close resources
        dss.close();
    }
    private TimeSeriesCollectionContainer loadContainers(EnsembleTimeSeries ets){

        TimeSeriesCollectionContainer rval = new hec.io.TimeSeriesCollectionContainer();
        for (Ensemble e: ets){
            float[][] values = e.getValues();
            for (int row = 0; row <values.length ; row++) {
                TimeSeriesContainer tsc = new TimeSeriesContainer();
                tsc.setValues(convertFloatsToDoubles(values[row]));
                String path = buildDssPath(e,(row+1),ets.getTimeSeriesIdentifier());
                tsc.setFullName(path);
                tsc.units = e.getUnits();
                tsc.setType(ets.getDataType());
                tsc.setStartTime(getHecStartTime(e));
                rval.add(tsc);
            }
        }
        rval.finishedAdding();
        return rval;
    }


    private static HecTime getHecStartTime(Ensemble e) {
        String dateStr = "";
        DateTimeFormatter dssDateFormat = DateTimeFormatter.ofPattern("ddMMMyyyy HHmm");
        if (e.getTimeCount() > 0)
            dateStr = e.startDateTime()[0].format(dssDateFormat);
        HecTime rval = new HecTime(dateStr);
        return rval;
    }

    /**
     * convert array of floats to array of doubles
     * //https://stackoverflow.com/questions/2019362/how-to-convert-array-of-floats-to-array-of-doubles-in-java
     * @param input
     * @return
     */

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
        // read dss catalog
        HecDssCatalog  dss = new HecDssCatalog(dssFileName);
        dss.setUseCollectionGroups(true);
        hec.heclib.dss.CondensedReference[] catalog = dss.getCondensedCatalog("/*/*/*/*/*/*/");
        for (int i = 0; i <catalog.length ; i++) {
            String p = catalog[i].getFirstPathname();

        }

        // Get B=ri.location, C=ri.parameter
        // need unique (B,C,F,E-extra if applicable)

    return null;
    }


    @Override
    public void close() throws Exception {

    }

    @Override
    public hec.metrics.MetricCollection getMetricCollection(hec.RecordIdentifier timeseriesID, java.time.ZonedDateTime issue_time) {
        return null;
    }

    @Override
    public hec.metrics.MetricCollectionTimeSeries getMetricCollectionTimeSeries(hec.RecordIdentifier timeseriesID) {
        return null;
    }

    @Override
    public java.util.List<java.time.ZonedDateTime> getMetricCollectionIssueDates(hec.RecordIdentifier timeseriesID) {
        return null;
    }

    @Override
    public void write(hec.metrics.MetricCollectionTimeSeries[] metricsArray) throws Exception {

    }

    @Override
    public void write(hec.metrics.MetricCollectionTimeSeries metrics) throws Exception {

    }

    @Override
    public java.util.List<hec.RecordIdentifier> getMetricTimeSeriesIDs() {
        return null;
    }
}