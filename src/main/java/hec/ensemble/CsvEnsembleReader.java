package hec.ensemble;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CsvEnsembleReader {


    String path; // path to csv files

    public CsvEnsembleReader(String path) {
        this.path = path;
    }


    /// <summary>
    /// Reads list of values
    /// </summary>
    /// <param name="watershedName"></param>
    /// <param name="issueDate"></param>
    /// <returns></returns>
    RfcCsvFile Read(String watershedName, ZonedDateTime issueDate)
    {
        //https://www.cnrfc.noaa.gov/csv/2019092312_RussianNapa_hefs_csv_hourly.zip


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String fileName = formatter.format(issueDate) + "_";
        fileName += watershedName;
        fileName += "_hefs_csv_hourly";

        //String csvFileName =
          Path p = Paths.get(path, fileName + ".csv");
          File f = p.toFile();
        if ( f.exists())
        {
            Logger.log("Found " + f.toString() + " in cache.  Reading...");
            RfcCsvFile csv = new RfcCsvFile(p.toString());
            return csv;
        }
        else
        {
            Logger.logWarning("Warning: " + p.toString() + " not found, skipping");
            return null;
        }
    }

    /**
     * Reads EnsembleTimeSeries grouped by LocationName
     * @param watershedName
     * @param startDate
     * @param endDate
     * @return
     */
    public EnsembleTimeSeries[] Read(String watershedName, ZonedDateTime startDate, ZonedDateTime endDate)
    {
        if (!ValidDates(startDate, endDate))
            return null;
        Map<String,EnsembleTimeSeries> locationMap = new HashMap<String, EnsembleTimeSeries>();
        ZonedDateTime t = startDate;

        while( t.isBefore(endDate.plusHours(1)))
        {
            RfcCsvFile csv = Read(watershedName, t);

            if (csv != null)
            {
                for (String locName : csv.getLocationNames())
                {
                    EnsembleTimeSeries ets=null;
                    ZonedDateTime t1 = csv.TimeStamps[0];
                    if( locationMap.containsKey(locName)) {
                        ets = locationMap.get(locName);
                    }
                    else {
                        TimeSeriesIdentifier tsid= new TimeSeriesIdentifier(watershedName+"."+locName,"flow");
                        ets = new EnsembleTimeSeries(tsid, "","",csv.FileName);
                        locationMap.put(locName, ets);
                    }

                    ets.addEnsemble(t,csv.getEnsemble(locName),t1,csv.getInterval());
                }
            }
            t = t.plusDays(1);
        }
        EnsembleTimeSeries[] rval = new EnsembleTimeSeries[locationMap.size()];
        int i=0;
        for (EnsembleTimeSeries ets: locationMap.values()) {
            rval[i]=ets;
            i++;
        }
        return rval;
    }

    boolean ValidDates(ZonedDateTime startDate, ZonedDateTime endDate)
    {
        if (startDate.getHour() != 12)
        {
            Logger.logError("start time must be 12");
            return false;
        }
        if (endDate.getHour() != 12)
        {
            Logger.logError("end time must be 12");
            return false;
        }

        if (startDate.isAfter(endDate))
        {
            Logger.logError("end date should be after start date");
            return false;
        }
        return true;
    }
}
