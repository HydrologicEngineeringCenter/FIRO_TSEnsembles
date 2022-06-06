package hec.dss.ensemble;

import hec.EnsembleDatabase;
import hec.MetricDatabase;
import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
import hec.io.TimeSeriesCollectionContainer;
import hec.io.TimeSeriesContainer;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.Statistics;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

/**
 * DssDatabase implements EnsembleDatabase.
 * It is used to work with time-series of ensembles.
 * Data is stored in a DSS file, using the Collections and Version features of the DSS F-Part tag.
 */
public class DssDatabase implements EnsembleDatabase,MetricDatabase {
    private String dssFileName;
    Catalog catalog;
    static DateTimeFormatter dssDateFormat = DateTimeFormatter.ofPattern("ddMMMyyyy HHmm");
    static DateTimeFormatter startDateformatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
    static DateTimeFormatter issueDateformatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public DssDatabase(String dssFileName){
        this.dssFileName= dssFileName;
    }

    private Catalog getCatalog(){
        if( this.catalog == null)
            catalog  = new Catalog(this.dssFileName);
        return catalog;
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

    private String buildStatPathName(RecordIdentifier timeSeriesIdentifier, Duration interval, ZonedDateTime startDateTime, ZonedDateTime issueDate, Statistics stat) {
        DSSPathname path = new DSSPathname();
        path.setAPart("");
        path.setBPart(timeSeriesIdentifier.location);
        path.setCPart(timeSeriesIdentifier.parameter);
        path.setDPart("");
        path.setEPart(getEPart((int)interval.toMinutes()));
        path.setFPart(buildFpart(startDateTime, issueDate) + stat.toString());
        return path.toString();
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
        return String.format("C:%06d", member)
                +"|T:"+startDateformatter.format(t)
                +"|V:"+issueDateformatter.format(v)+"|";
    }

    private String buildFpart(ZonedDateTime t, ZonedDateTime v) {

        return "T:" + startDateformatter.format(t)
                + "|V:" + issueDateformatter.format(v) + "|";
    }

    public Ensemble getEnsemble(RecordIdentifier recordID, ZonedDateTime issue_time){
        List<DSSPathname> paths = catalog.getPaths(recordID, issue_time);
        TimeSeriesCollectionContainer tscc = getEnsembleCollection(paths);
        float[][] vals = getEnsembleValues(tscc);
        ZonedDateTime startDate = getStartDate(tscc);
        Duration interval = Duration.ofMinutes(hec.heclib.dss.HecTimeSeries.getIntervalFromEPart(paths.get(0).ePart()));

        return new Ensemble(
                issue_time,
                vals,
                startDate,
                interval,
                tscc.get(0).units
        );
    }

    private float[][] getEnsembleValues(TimeSeriesCollectionContainer tscc) {
        float[][] vals = new float[tscc.size()][];

        for (int i = 0; i < tscc.size(); i++) {
            vals[i] = convertDoublesToFloats(tscc.get(i).values);
        }

        return vals;
    }

    private ZonedDateTime getStartDate(TimeSeriesCollectionContainer tscc) {
        HecTime time = tscc.get(0).getStartTime();
        System.out.println(time.toString());
        return ZonedDateTime.of(time.year(), time.month(), time.day(), time.hour(),
                time.minute(), time.second(), 0,
                TimeZone.getTimeZone(tscc.locationTimezone).toZoneId());
    }

    private TimeSeriesCollectionContainer getEnsembleCollection(List<DSSPathname> paths) {
        TimeSeriesCollectionContainer tscc = new TimeSeriesCollectionContainer();
        HecTimeSeries dss = new HecTimeSeries();
        dss.setDSSFileName(this.dssFileName);

        for (int i = 0; i < paths.size(); i++) {
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.setFullName(paths.get(i).pathname());
            dss.read(tsc, true);
            tscc.add(tsc);
        }

        dss.done();
        return tscc;
    }

    public EnsembleTimeSeries getEnsembleTimeSeries(RecordIdentifier recordID){
        EnsembleTimeSeries ets = new EnsembleTimeSeries(recordID, "", "", "");
        List<ZonedDateTime> zdts = catalog.rids.get(recordID);

        for (ZonedDateTime zdt : zdts) {
            Ensemble e = getEnsemble(recordID, zdt);
            ets.addEnsemble(e);
        }

        return ets;
    }

    /**
     * Return list of ZonedDateTime for a RecordIdentifier
     * from DSS f-part T: tag
     * TO DO?  mapping to T: of DSS (consider renaming to getEnsembleStartDates()
     *
     * @param recordID
     * @return
     */
    public java.util.List<java.time.ZonedDateTime> getEnsembleIssueDates(RecordIdentifier recordID) {
        return getCatalog().getEnsembleStartDates(recordID);
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
        // close resources
        dss.close();
    }
    private static TimeSeriesCollectionContainer loadContainers(EnsembleTimeSeries ets){

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
        if (e.getTimeCount() > 0)
            dateStr = e.startDateTime()[0].format(dssDateFormat);
        return new HecTime(dateStr);
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

    private static float[] convertDoublesToFloats(double[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        float[] output = new float[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = (float)input[i];
        }
        return output;
    }

    /**
     * getEnsembleTimeSeriesIDs returns a list of RecordIdentifier's
     *
     * Limitations:
     *    - Assuming Interval is consistent for the whole DSS file
     *    - Assuming fpart beyond C:,T:, V: is ignored
     * @return
     */
    public java.util.List<RecordIdentifier> getEnsembleTimeSeriesIDs(){
        return getCatalog().getEnsembleTimeSeriesIDs();
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
        for (MetricCollectionTimeSeries mcts : metricsArray)
            write(mcts);
    }

    @Override
    public void write(hec.metrics.MetricCollectionTimeSeries metrics) throws Exception {
        HecTimeSeries dss = new HecTimeSeries(dssFileName);

        for (MetricCollection mc : metrics) {
            Statistics[] stats = mc.getMetricStatistics();
            for (int i = 0; i < stats.length; i++) {
                TimeSeriesContainer tsc = new TimeSeriesContainer();
                tsc.values = convertFloatsToDoubles(mc.getValues()[i]);
                tsc.numberValues = tsc.values.length;
                HecTime time = getHecStartTime(mc.getStartDateTime());
                tsc.setStartTime(time);
                tsc.fullName = buildStatPathName(metrics.getTimeSeriesIdentifier(),
                        mc.getInterval(),
                        mc.getStartDateTime(),
                        mc.getIssueDate(),
                        stats[i]);
                dss.write(tsc);
            }

        }

        dss.done();
    }

    private HecTime getHecStartTime(ZonedDateTime startDateTime) {
        String dateStr = "";
        dateStr = startDateTime.format(dssDateFormat);
        return new HecTime(dateStr);
    }

    @Override
    public java.util.List<hec.RecordIdentifier> getMetricTimeSeriesIDs() {
        return getCatalog().getMetricTimeSeriesIDs();
    }

    /*
        MetricCollectTimeSeries = {
            items: [
                Date1: {
                    configuration: {
                        interval: "1Hour",
                        start_date: day1,
                        issue_date: some date
                    },
                    metrics: [
                        [5, 6, 7], <-- "MAX"
                        [1, 2, 3], <-- "MIN"
                        [3, 4, 5]  <-- "MEAN"
                    ],
                    metric_statistics: [MAX, MIN, MEAN]
                },
                Date2: {

                },
                .
                .
                .
            ]
        }

        Max Time Series = {
            times: [day1, day2, day3],
            values: [5, 6, 7]
        }
         */


    // Using Time Series Path
        /*

        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|MAX/
        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|MIN/
        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|MEAN/

        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131104-1200|V:20131103-120000|MAX/
        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131104-1200|V:20131103-120000|MIN/
        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131104-1200|V:20131103-120000|MEAN/

        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131105-1200|V:20131103-120000|MAX/
        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131105-1200|V:20131103-120000|MIN/
        //Kanektok.BCAC1/flow/01Nov2013/1Hour/T:20131105-1200|V:20131103-120000|MEAN/

        1. Get compute data.
        2. Get dates associated with data.
        3. Combine data with dates into time series container.
        4. Write time series container to disk.
         */


    // Using Paired Data Path
    /*

     */
}