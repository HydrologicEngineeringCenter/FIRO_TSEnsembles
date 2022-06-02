package hec.dss.ensemble;

import hec.RecordIdentifier;
import hec.heclib.dss.DSSPathname;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Catalog {

    String dssFilename;
    HashMap<RecordIdentifier, List<ZonedDateTime>> rids = new HashMap<>();

    public Catalog(String dssFilename){
        this.dssFilename = dssFilename;
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

                // Get start date from F part
                Pattern pattern = Pattern.compile("T:(\\d{8}-\\d{4})");
                Matcher matcher = pattern.matcher(path.fPart());
                if (matcher.find()) {
                    String s = matcher.group(1);
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
                    LocalDateTime localDateTime = LocalDateTime.parse(s, format);
                    ZonedDateTime zdt = localDateTime.atZone(ZoneOffset.systemDefault());

                    // Add record identifier if it doesn't exist
                    if (!rids.containsKey(rid))
                        rids.put(rid, new ArrayList<>());

                    // Add zoned data time to record identifier if it doesn't exist
                    if (!rids.get(rid).contains(zdt))
                        rids.get(rid).add(zdt);
                }
            }
        }
    }

    public List<RecordIdentifier> getEnsembleTimeSeriesIDs() {
        return new ArrayList<>(rids.keySet());
    }

    public java.util.List<java.time.ZonedDateTime> getEnsembleStartDates(RecordIdentifier rid) {
        return rids.get(rid);
    }
}
