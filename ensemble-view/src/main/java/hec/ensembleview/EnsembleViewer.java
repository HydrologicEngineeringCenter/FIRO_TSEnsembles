package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.Ensemble;

import java.time.ZonedDateTime;
import java.util.List;

public class EnsembleViewer {
    private SqliteDatabase db;

    public static void main(String[] args) throws Exception {
        EnsembleViewer ev = new EnsembleViewer("C:\\Projects\\FIRO_TSEnsembles\\FIRO_TSEnsembles\\src\\test\\resources\\database\\importCsvToDatabase.db");
        List<RecordIdentifier> rid = ev.db.getEnsembleTimeSeriesIDs();
        List<ZonedDateTime> zdt = ev.db.getEnsembleIssueDates(rid.get(0));
        System.out.println(rid.get(0));

        Ensemble ensemble = ev.db.getEnsemble(rid.get(0), zdt.get(0));

        EnsembleChart chart = new EnsembleJFreeChart("Ensemble Viewer Example");
        chart.setTitle(rid.get(0).toString());
        chart.setYLabel("Flow (cfs)");
        chart.setXLabel("Date/Time");
        float[][] vals = ensemble.getValues();
        ZonedDateTime[] dates = ensemble.startDateTime();
        for (int i = 0; i < vals.length; i++) {
            chart.addLine(vals[i], dates, "Member " + (i + 1));
        }
        chart.showPlot();
    }

    public EnsembleViewer(String database) throws Exception {
        db = new SqliteDatabase(database, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);
    }


}
