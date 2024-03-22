package hec.dss.ensemble;

import hec.RecordIdentifier;
import hec.heclib.dss.DSSPathname;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Catalog {
    private static final ZonedDateTime defaultIssueDate = ZonedDateTime.now();
    String dssFilename;
    Pattern startTimePattern = Pattern.compile("T:(\\d{8}-\\d{4})");
    DateTimeFormatter startTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
    ZoneId timeZone;
    HashMap<RecordIdentifier, List<ZonedDateTime>> rids = new HashMap<>();
    HashMap<RecordIdentifier, List<DSSPathname>> pathNames = new HashMap<>();
    HashMap<RecordIdentifier, List<ZonedDateTime>> metricRids = new HashMap<>();
    HashMap<RecordIdentifier, List<DSSPathname>> metricPathNames = new HashMap<>();

    public Catalog(String dssFilename){
        this(dssFilename, "GMT");
    }

    public Catalog(String dssFilename, String timeZone) {
        this.dssFilename = dssFilename;
        this.timeZone = TimeZone.getTimeZone(timeZone).toZoneId();
        buildEnsembleCatalog();
    }

    private void buildEnsembleCatalog() {
        hec.heclib.dss.HecDssCatalog dss = new hec.heclib.dss.HecDssCatalog(dssFilename);
        dss.setUseCollectionGroups(true);
        hec.heclib.dss.CondensedReference[] catalog = dss.getCondensedCatalog("/*/*/*/*/*/*/");

        // Get all collections in dss file
        for (hec.heclib.dss.CondensedReference condensedReference : catalog) {
            DSSPathname path = new DSSPathname(condensedReference.getNominalPathname());

            // Add record identifier for collection
            if (DSSPathname.isaCollectionPath(path.toString())) {
                addCollection(path);
            }
            else {
                boolean isMetricTimeseries = MetricPathTools.isMetricTimeSeries(path.cPart());
                boolean isMetricPairedData = MetricPathTools.isMetricPairedData(path.cPart());
                if (isMetricPairedData){
                    addMetric(path);
                }
                else if (isMetricTimeseries){
                    addMetric(path);
                }
            }
        }

        dss.done();
    }


    private void addMetric(DSSPathname path) {
        String location = path.bPart();
        String parameter = path.cPart();
        RecordIdentifier rid = new RecordIdentifier(location, parameter);

        // Add record identifier if it doesn't exist
        if (!metricRids.containsKey(rid)) {
            metricRids.put(rid, new ArrayList<>());
            metricPathNames.put(rid, new ArrayList<>());
        }

        // No version handling for paths at the moment
        metricPathNames.get(rid).add(path);

        // Get zoned date time from path
        ZonedDateTime zdt = getStartDateTime(path);

        // Add zoned data time to record identifier if it doesn't exist
        if (!metricRids.get(rid).contains(zdt))
            metricRids.get(rid).add(zdt);
    }

    private void addCollection(DSSPathname path) {
        String location = path.bPart();
        String parameter = path.cPart();
        RecordIdentifier rid = new RecordIdentifier(location, parameter);

        // Add record identifier if it doesn't exist
        if (!rids.containsKey(rid)) {
            rids.put(rid, new ArrayList<>());
            pathNames.put(rid, new ArrayList<>());
        }

        // No version handling for paths at the moment
        pathNames.get(rid).add(path);

        // Get zoned date time from path
        ZonedDateTime zdt = getStartDateTime(path);

        // Add zoned data time to record identifier if it doesn't exist
        if (!rids.get(rid).contains(zdt))
            rids.get(rid).add(zdt);
    }

    public List<RecordIdentifier> getEnsembleTimeSeriesIDs() {
        return new ArrayList<>(rids.keySet());
    }

    public List<RecordIdentifier> getMetricIDs() {
        return new ArrayList<>(metricRids.keySet());
    }

    public java.util.List<java.time.ZonedDateTime> getEnsembleStartDates(RecordIdentifier rid) {
        return rids.get(rid);
    }

    public List<DSSPathname> getPaths(RecordIdentifier recordID, ZonedDateTime startTime) {
        List<DSSPathname> allPaths = pathNames.get(recordID);
        List<DSSPathname> rval = new ArrayList<>();

        for (DSSPathname path : allPaths) {
            ZonedDateTime zdt = getStartDateTime(path);
            if (zdt != null && zdt.equals(startTime))
                rval.add(path);
        }

        return rval;
    }

    public List<DSSPathname> getMetricPaths(RecordIdentifier recordID, ZonedDateTime startTime) {
        List<DSSPathname> allPaths = metricPathNames.get(recordID);
        List<DSSPathname> rval = new ArrayList<>();

        for (DSSPathname path : allPaths) {
            ZonedDateTime zdt = getStartDateTime(path);
            if (zdt != null && zdt.equals(startTime))
                rval.add(path);
        }

        return rval;
    }

    private ZonedDateTime getStartDateTime(DSSPathname path) {
        Matcher matcher = startTimePattern.matcher(path.fPart());

        if (matcher.find()){
            String s = matcher.group(1);
            LocalDateTime localDateTime = LocalDateTime.parse(s, startTimeFormat);
            return localDateTime.atZone(TimeZone.getTimeZone("GMT").toZoneId());
        }

        return defaultIssueDate;
    }

    public void update() {
        rids.clear();
        metricRids.clear();
        pathNames.clear();
        metricPathNames.clear();
        buildEnsembleCatalog();
    }



    public List<ZonedDateTime> getMetricIssueDates(RecordIdentifier rid) {
        return metricRids.get(rid);
    }

    public List<RecordIdentifier> getMetricPairedDataIDs() {
        List<RecordIdentifier> res = new ArrayList<>();
        Set<RecordIdentifier> allMetricRids = metricRids.keySet();
        for (RecordIdentifier rid : allMetricRids) {
            List<DSSPathname> paths = metricPathNames.get(rid);
            for (DSSPathname path : paths) {
                if (MetricPathTools.isMetricPairedData(path.cPart())) {
                    res.add(rid);
                    break;
                }
            }
        }
        return res;
    }

    public List<RecordIdentifier> getMetricTimeSeriesIDs() {
        List<RecordIdentifier> res = new ArrayList<>();
        Set<RecordIdentifier> allMetricRids = metricRids.keySet();
        for (RecordIdentifier rid : allMetricRids) {
            List<DSSPathname> paths = metricPathNames.get(rid);
            for (DSSPathname path : paths) {
                if (MetricPathTools.isMetricTimeSeries(path.cPart())) {
                    res.add(rid);
                    break;
                }
            }
        }
        return res;
    }
}
