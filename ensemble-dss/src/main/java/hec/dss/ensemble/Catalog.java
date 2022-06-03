package hec.dss.ensemble;

import hec.RecordIdentifier;
import hec.heclib.dss.DSSPathname;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Catalog {

    String dssFilename;
    Pattern startTimePattern = Pattern.compile("T:(\\d{8}-\\d{4})");
    DateTimeFormatter startTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
    ZoneId timeZone;
    HashMap<RecordIdentifier, List<DSSPathname>> pathnames = new HashMap<>();
    HashMap<RecordIdentifier, List<ZonedDateTime>> rids = new HashMap<>();

    public Catalog(String dssFilename){
        this(dssFilename, "GMT");
    }

    public Catalog(String dssFilename, String timeZone) {
        this.dssFilename = dssFilename;
        this.timeZone = TimeZone.getTimeZone(timeZone).toZoneId();
        setupEnsembleCatalog();
    }

    private void setupEnsembleCatalog() {
        hec.heclib.dss.HecDssCatalog dss = new hec.heclib.dss.HecDssCatalog(dssFilename);
        dss.setUseCollectionGroups(true);
        hec.heclib.dss.CondensedReference[] catalog = dss.getCondensedCatalog("/*/*/*/*/*/*/");

        // Get all collections in dss file
        for (hec.heclib.dss.CondensedReference condensedReference : catalog) {
            DSSPathname path = new DSSPathname(condensedReference.getNominalPathname());

            // Add record identifier for collection
            if (DSSPathname.isaCollectionPath(path.toString())) {
                String location = path.bPart();
                String parameter = path.cPart();
                RecordIdentifier rid = new RecordIdentifier(location, parameter);

                // Add record identifier if it doesn't exist
                if (!rids.containsKey(rid)) {
                    rids.put(rid, new ArrayList<>());
                    pathnames.put(rid, new ArrayList<>());
                }

                // No version handling for paths at the moment
                pathnames.get(rid).add(path);

                // Get zoned date time from path
                ZonedDateTime zdt = getStartDateTime(path);

                // Add zoned data time to record identifier if it doesn't exist
                if (!rids.get(rid).contains(zdt))
                    rids.get(rid).add(zdt);

            }
        }
    }

    public List<RecordIdentifier> getEnsembleTimeSeriesIDs() {
        return new ArrayList<>(rids.keySet());
    }

    public java.util.List<java.time.ZonedDateTime> getEnsembleStartDates(RecordIdentifier rid) {
        return rids.get(rid);
    }

    public List<DSSPathname> getPaths(RecordIdentifier recordID, ZonedDateTime startTime) {
        List<DSSPathname> allPaths = pathnames.get(recordID);
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

        return null;
    }
}
