package hec.dss.ensemble;

public class Catalog {

    String dssFilename;
    public Catalog(String dssFilename){
        this.dssFilename = dssFilename;
        // be smart, build sets, maps, etc..
    }
    public java.util.List<hec.RecordIdentifier> getEnsembleTimeSeriesIDs() {
        hec.heclib.dss.HecDssCatalog dss = new hec.heclib.dss.HecDssCatalog(dssFilename);

        hec.heclib.dss.CondensedReference[] catalog = dss.getCondensedCatalog("/*/*/*/*/*/*/");
        for (int i = 0; i <catalog.length ; i++) {
            String p = catalog[i].getFirstPathname();
        }

        // Get B=ri.location, C=ri.parameter
        // need unique (B,C,F,E-extra if applicable)

        return null;
    }

    public java.util.List<java.time.ZonedDateTime> getEnsembleStartDates() {
        return null;
    }
}
