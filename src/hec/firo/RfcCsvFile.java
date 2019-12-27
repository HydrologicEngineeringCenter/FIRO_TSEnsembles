package hec.firo;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class RfcCsvFile
  {
    public String FileName;
    public List<String> LocationNames ;

    public Date[] TimeStamps;

    private float[][] Data;

    /// <summary>
    /// index to start of each location in Data
    /// </summary>
    Map<String,Integer> locationStart = new HashMap<String, Integer>();
    /// <summary>
    /// index to end of each location in Data
    /// </summary>
    Map<String,Integer>  locationEnd = new HashMap<String, Integer>();
    
    private String[] header;

    // example:
    /*
     GMT,PLLC1,PLLC1,PLLC1,PLLC1,PLLC1,PLLC1,PLLC1,PLLC1
     ,QINE,QINE,QINE,QINE,QINE,QINE,QINE,QINE,QINE,QINE,
     2015-03-17 12:00:00,1.0728949,1.0728949,1.0728949,1
     2015-03-17 13:00:00,1.1079977,1.0526596,1.05326,1.0
     2015-03-17 14:00:00,1.1431005,1.0323889,1.033625,1.
     2015-03-17 15:00:00,1.1782385,1.0121536,1.01399,1.0
     2015-03-17 16:00:00,1.2133415,0.9919184,0.9943551,0
     2015-03-17 17:00:00,1.2484442,0.9716478,0.9747201,0
     2015-03-17 18:00:00,1.2835469,0.9514125,0.9550852,0
     2015-03-17 19:00:00,1.2741178,0.9471394,0.9483401,0
     2015-03-17 20:00:00,1.2646536,0.942831,0.941595,0.9
     2015-03-17 21:00:00,1.2552245,0.9385579,0.9348852,0
     2015-03-17 22:00:00,1.2457602,0.9342495,0.9281401,0
     2015-03-17 23:00:00,1.2363312,0.92997646,0.921395,0
     2015-03-18 00:00:00,1.226867,0.92566806,0.9146499,0
     2015-03-18 01:00:00,1.2062078,0.928246,0.9163803,0.
     2015-03-18 02:00:00,1.1855487,0.9307887,0.9181461,0
     2015-03-18 03:00:00,1.1648897,0.93336666,0.91987646
     2015-03-18 04:00:00,1.1441953,0.93590933,0.9216069,
     2015-03-18 05:00:00,1.1235362,0.93845195,0.9233726,
     */

    /// <summary>
    /// CSV file format from California Nevada River Forecast Center
    /// https://www.cnrfc.noaa.gov/
    /// 
    /// First column is date/time
    /// </summary>
    /// <param name="fileName"></param>
    public RfcCsvFile(String fileName)
    {
      this.FileName = fileName;
      Path filePath = new File(fileName).toPath();
      Charset charset = Charset.defaultCharset();
      List<String> stringList = null;
      try {
        stringList = Files.readAllLines(filePath, charset);
      } catch (IOException e) {
        e.printStackTrace();
      }
      String[] rows = stringList.toArray(new String[ stringList.size()]);
      ParseHeader(rows[0]);
      ParseData(rows);
    }

    private float[][] _ensemble;
    /// <summary>
    /// Returns 2-D array where each row is an ensemble member
    /// note: this is an axis swap from the CSV on disk
    /// </summary>
    /// <param name="locationName"></param>
    /// <param name="swapAxis">when true rows represent time steps</param>
    /// <returns></returns>
    public float[][] GetEnsemble(String locationName)
    {
      int idx1 = locationStart.get(locationName);
      int idx2 = locationEnd.get(locationName);

      int memberCount = idx2 - idx1 + 1; // height
      int timeCount = TimeStamps.length; // width

        if (_ensemble == null || _ensemble.length != memberCount
           || _ensemble[0].length != timeCount)
          _ensemble = new float[memberCount][timeCount];


      for (int m = 0; m < memberCount; m++)
      {
        for (int t = 0; t < timeCount; t++)
        {
          _ensemble[m][t] = Data[m + idx1][ t];
        }
      }
//     not working.. for (int i = 0; i <memberCount; i++) {
//        System.arraycopy(Data[idx1+i],i*timeCount,_ensemble[i],0,timeCount);
//      }

      return _ensemble;
    }

    /// <summary>
    /// Parse data swaping axis
    /// rows represent timesteps
    /// columns represent locations
    /// </summary>
    /// <param name="rows"></param>
    private void ParseData(String[] rows)
    {
      int idx2 = FindLastRowIndex(rows);
      int idx1 = 2; // data starts after two header lines
      int rowCount = idx2 - idx1 + 1;
      int columnCount = header.length - 1; // date column will not be part of data
      TimeStamps = new Date[rowCount];
      Data = new float[columnCount][rowCount]; // swap axis
      for (int rowIdx = 0; rowIdx < rowCount; rowIdx++)
      {
        String[] values = rows[rowIdx+idx1].split(",");
        TimeStamps[rowIdx] = ParseDateTime(values[0]); // first column is DateTime
        for (int columnIdx = 0; columnIdx < columnCount; columnIdx++)
        {
          // if (columnIdx >= values.Length)
          //  Console.WriteLine("Error: was file truncated? " + FileName);
          float f = Float.parseFloat(values[columnIdx + 1]);
          Data[columnIdx][ rowIdx] = f;
        }
      }
    }
    /// <summary>
    /// find last row of data.
    /// some files have empty lines at the bottom.
    /// </summary>
    /// <param name="rows"></param>
    /// <returns></returns>
    private int FindLastRowIndex(String[] rows)
    {
      for (int i = rows.length-1; i>0;  i--)
      {
        if (rows[i].trim() != "")
          return i;
      }
      return -1;
    }


    private void ParseHeader(String line)
    {
     
      header = line.split(",");
      String currHeader = "";
    
      LocationNames = new ArrayList<>();
      //first data element in header is timezone.
      for (int i = 1; i < header.length; i++)
      {
        if (!currHeader.equals(header[i]))
        {
          currHeader = header[i];
          LocationNames.add(currHeader);
          locationStart.put(currHeader,i-1);
        }
        else
        {
          locationEnd.put(currHeader, i-1);
        }
      }
    }
 static SimpleDateFormat _formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public static Date ParseDateTime(String dt)
    {
      // 11/3/2013 12:00
      Date rval = null;
      try {
        rval = _formatter.parse(dt);
      } catch (ParseException e) {
        e.printStackTrace();
      }

      return rval;
      //return LocalDateTime.parse(dt);
      //string[] dateTime = dt.Split(' ');
      //string[] yyyymmdd = dateTime[0].Split('-');
      //string[] hhmmss = dateTime[1].Split(':');
      //DateTime output = new DateTime(int.Parse(yyyymmdd[0]), int.Parse(yyyymmdd[1]), int.Parse(yyyymmdd[2]), int.Parse(hhmmss[0]), int.Parse(hhmmss[1]), int.Parse(hhmmss[2]));
      //return output;
    }
  }
