package hec.ensemble;

import hec.JdbcDatabase;
import hec.EnsembleDatabase;
import hec.RecordIdentifier;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an ensemble test database for use with ResSim scripting
 *
 */
public class EnsembleUtility {

    public static void main(String[] args)throws Exception {
        if( args.length != 4)
        {
            System.out.println("EnsembleUtility hefs_cache_dir numDays numLocations output.db");
            System.out.println("Example:  ");
            System.out.println("EnsembleUtility c:/temp/hefs_cache 30 1 ResSim.db");
            return;
        }
        int numDays = Integer.parseInt(args[1].trim());
        int numLocations = Integer.parseInt(args[2].trim());

        createTestDatabase(args[0],numDays, numLocations,args[3]);
        readModifyWrite(args[3]);

    }

    /**
     * Read some data from 2015,  re-label as starting in 1996 for testing ResSim
     * @param hefs_dir
     * @param filename
     * @return
     * @throws Exception
     */
    static private EnsembleDatabase createTestDatabase(String hefs_dir, int numberOfDates, int numLocations,
                                                       String filename) throws Exception {

        File f = new File(filename);
        if( f.exists())
           f.delete();
        ZonedDateTime issueDate1 = ZonedDateTime.of(2015, 1, 1, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        CsvEnsembleReader csvReader = new CsvEnsembleReader(hefs_dir);
        EnsembleTimeSeries[] ets = csvReader.Read("RussianNapa", issueDate1, issueDate2);
        EnsembleDatabase db = new JdbcDatabase(filename, JdbcDatabase.CREATION_MODE.CREATE_NEW);
        ZonedDateTime startDateTime = ZonedDateTime.of(1996, 12, 24, 12, 0, 0, 0, ZoneId.of("GMT"));

        // modify start/issue dates/ location
        EnsembleTimeSeries etsOut = null;
        List<EnsembleTimeSeries> outList = new ArrayList<EnsembleTimeSeries>();
        for (int i = 0; i <Math.min(ets.length,numLocations) ; i++) {
            EnsembleTimeSeries a = ets[i];
            RecordIdentifier tsid = a.getTimeSeriesIdentifier();
            if( i == 0) {
                tsid = new RecordIdentifier("Coyote.fake_forecast","flow");
            }
            List<ZonedDateTime> dates = a.getIssueDates();

            etsOut = new EnsembleTimeSeries(tsid, a.getUnits(), a.getDataType(), a.getVersion());
            ZonedDateTime newIssueDate = startDateTime;
            for (int j = 0; j < a.getCount(); j++) {
                Ensemble e = a.getEnsemble(dates.get(j));
                Ensemble e2 = new Ensemble(newIssueDate, e.getValues(), newIssueDate, e.getInterval(),e.getUnits());
                newIssueDate = newIssueDate.plusDays(1);
                etsOut.addEnsemble(e2);
            }
            outList.add(etsOut);
        }
        db.write(outList.toArray(new EnsembleTimeSeries[0]));
        return db;
    }

    /**
     * Reads an ensemble database into memory,
     * modifies ensemble data, then writes to a temporary file
     * logs time for reading and writing.
     *
     * @param fileName
     * @throws Exception
     */
    static void readModifyWrite(String fileName) throws Exception {
        long startTime = System.nanoTime();
        EnsembleDatabase db = new JdbcDatabase(fileName, JdbcDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
        List<RecordIdentifier> locations = db.getEnsembleTimeSeriesIDs();

        ArrayList<EnsembleTimeSeries> etsList = new ArrayList<>();
        int count = 0;
        for(RecordIdentifier tsid: locations) {
            System.out.println(tsid.toString());
            EnsembleTimeSeries etsr = db.getEnsembleTimeSeries(tsid);
            EnsembleTimeSeries modifiedEts = new EnsembleTimeSeries(tsid,
                    etsr.getUnits(),etsr.getDataType(),etsr.getVersion());

            for( Ensemble e : etsr){
                for(float[] v : e.getValues()){
                    for (int i = 0; i <v.length ; i++) {
                        v[i] = v[i]+10 ;// offset by 10
                    }
                }
                modifiedEts.addEnsemble(e);
                count ++;
            }
            etsList.add(modifiedEts);
        }

        long endTime = System.nanoTime();
        double seconds = (endTime - startTime)/1000000.0/1000.;
        System.out.println("Time To Read/Modify : "+seconds+" s");
        File f = File.createTempFile("delete_me",".sqlite");
        String outputPath = f.getName();
        f.delete();

        startTime = System.nanoTime();
        JdbcDatabase dbaseOut = new JdbcDatabase(outputPath, JdbcDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
        dbaseOut.write(etsList.toArray(new EnsembleTimeSeries[0]));
        endTime = System.nanoTime();
        seconds = (endTime - startTime)/1000000.0/1000.0;
        System.out.println("Write back to disk : "+seconds+" s");

        // f.delete();
    }
}
