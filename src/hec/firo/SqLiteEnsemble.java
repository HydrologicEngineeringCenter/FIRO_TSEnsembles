package hec.firo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/// <summary>
  /// Reads/Writes Ensemble data to SQL tables
  /// each ensemble member is written to a blob
  /// with optional compressions
  /// </summary>
  public class SqLiteEnsemble
  {






    static byte[] ConvertToBytes(float[,] ensemble, bool compress, ref byte[] uncompressed)
    {
      int width = ensemble.GetLength(1);
      int height = ensemble.GetLength(0);

      if (uncompressed == null || uncompressed.Length != ensemble.Length)
        uncompressed = new byte[width * height * sizeof(float)];

      Buffer.BlockCopy(ensemble, 0, uncompressed, 0, uncompressed.Length);

      if (!compress)
        return uncompressed;
      var compressed = Compress(uncompressed);
      return compressed;
    }

    public static Watershed Read(String watershedName, DateTime startTime, DateTime endTime, String fileName)
    {
      SQLiteServer server = GetServer(fileName);
      var rval = new Watershed(watershedName);

      var sql = "select * from " + TableName +
        " WHERE issue_date >= '" + startTime.ToString(DateTimeFormat) + "' "
        + " AND issue_date <= '" + endTime.ToString(DateTimeFormat) + "' "
        + " AND watershed = '" + watershedName + "' ";
      sql += " order by watershed,issue_date,location_name";

      var table = server.Table(TableName, sql);
      if (table.Rows.Count == 0)
      {
        throw new Exception("no data");
      }
      DateTime prevIssueDate = Convert.ToDateTime(table.Rows[0]["issue_date"]);
      DateTime currentDate = Convert.ToDateTime(table.Rows[0]["issue_date"]);
      float[,] values = null;
      foreach (DataRow row in table.Rows)
      {
        currentDate = Convert.ToDateTime(row["issue_date"]);

        var times = GetTimes(row);
        GetValues(row, ref values);

        rval.AddForecast(row["location_name"].ToString(),
                                             currentDate,
                                             values,
                                             times);

      }
      return rval;
    }
    private static DateTime[] GetTimes(DataRow row)
    {
      DateTime t = Convert.ToDateTime(row["timeseries_start_date"]);
      int count = Convert.ToInt32(row["member_length"]);
      var rval = new DateTime[count];
      for (int i = 0; i < count; i++)
      {
        rval[i] = t;
        t = t.AddHours(1); // hardcode hourly
      }
      return rval;
    }

    //https://stackoverflow.com/questions/7013771/decompress-byte-array-to-string-via-binaryreader-yields-empty-string
    static byte[] Decompress(byte[] data)
    {
      // Was previously a GZip stream
      using (var compressedStream = new MemoryStream(data))
      using (var zipStream = new DeflateStream(compressedStream, CompressionMode.Decompress))
      using (var resultStream = new MemoryStream())
      {
        zipStream.CopyTo(resultStream);
        return resultStream.ToArray();
      }
    }

    private static void GetValues(DataRow row, ref float[,] data)
    {
      int compressed = Convert.ToInt32(row["compressed"]);
      var rval = new List<List<float>>();
      int member_count = Convert.ToInt32(row["member_count"]);
      int member_length = Convert.ToInt32(row["member_length"]);

      byte[] byte_values = (byte[])row["byte_value_array"];

      if (compressed != 0)
      {
        byte_values = Decompress(byte_values);
      }

      if (data == null || data.GetLength(0) != member_count || data.GetLength(1) != member_length)
        data = new float[member_count, member_length];

      var numBytesPerMember = byte_values.Length / member_count;

      Buffer.BlockCopy(byte_values, 0, data, 0, data.Length * sizeof(float));

      //for (int i = 0; i < member_count; i++)
      //{
      //  var floatValues = new float[member_length];
      //  Buffer.BlockCopy(byte_values, i * numBytesPerMember, floatValues, 0, numBytesPerMember);
      //  var values = new List<float>();
      //  values.AddRange(floatValues);
      //  rval.Add(values);
      //}

      // return rval;
    }



    static byte[] Compress(byte[] bytes)
    {
      using (var msi = new MemoryStream(bytes))
      using (var mso = new MemoryStream())
      {
        //var mode = CompressionMode.Compress;
        // using (var gs = new GZipStream(mso, mode))

        // Fastest deflatestream compression to match Alex's internal HDF5 deflate
        using (var gs = new DeflateStream(mso, CompressionLevel.Fastest))
        {
          //msi.CopyTo(gs);
          CopyTo(msi, gs);
        }

        return mso.ToArray();
      }
    }
    private static void CopyTo(Stream src, Stream dest)
    {
      byte[] bytes = new byte[4096];

      int cnt;

      while ((cnt = src.Read(bytes, 0, bytes.Length)) != 0)
      {
        dest.Write(bytes, 0, cnt);
      }
    }



    /// <summary>
    /// Returns and empty timeseries_hourly table for storing blobs
    /// </summary>
    /// <param name="server"></param>
    /// <returns></returns>
    static DataTable GetBlobTable(Reclamation.Core.BasicDBServer server)
    {
      String sql = SqLiteEnsembleWriter.GetCreateTableSQL(TableName);
      server.RunSqlCommand(sql);

      //server.RunSqlCommand("DELETE from " + TableName);
      return server.Table(TableName, "select * from " + TableName + " where 1=0");
    }
  }

