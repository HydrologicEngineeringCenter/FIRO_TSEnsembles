package hec.firo;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *  Read/Write Ensembles to a ODBC database
 */
public class EnsembleDatabase {

    public static String DateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    private static String TableName = "timeseries_blob";

    String FileName;
    Connection _connection;
    public EnsembleDatabase(String fileName) throws Exception
    {
      FileName= fileName;
      Properties prop = new Properties();
      //prop.setProperty("shared_cache", "false");
        //  Synchronous=Off;Pooling=True;Journal Mode=Off";  // dangerous but faster.
        //
      _connection =  DriverManager.getConnection("jdbc:sqlite:"+FileName,prop);
      _connection.setAutoCommit(false);
        CreateTable();
    }


    public void Write(Watershed watershed, boolean compress)
    {
        try {

            int index = GetMaxID();
            byte[] uncompressed = null;
            for(Location loc : watershed.Locations)
            {
                for(Forecast f : loc.Forecasts)
                {
                    index++;
                    float[][] data = f.Ensemble;
                     InsertEnsemble(index, f.IssueDate, watershed.Name, loc.Name, f.TimeStamps[0],
                            data[0].length, data.length, compress, ConvertToBytes(f.Ensemble, compress, ref uncompressed));
                }

            }
            _connection.commit();
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {

        }

    }
    static DateTimeFormatter _formatter = DateTimeFormatter.ofPattern(DateTimeFormat);
    private static String FormatDate(LocalDateTime t)
    {
     return t.format(_formatter);
    }

    private void InsertEnsemble(int id, LocalDateTime issue_date, String watershed, String location_name,
                               LocalDateTime timeseries_start_date,int member_length, int member_count,boolean compressed,
                               byte[] byte_value_array) throws Exception
    {
        String sql = "INSERT INTO timeseries_blob ([id], [issue_date], [watershed], [location_name], "+
                " [timeseries_start_date], [member_length], [member_count], [compressed], [byte_value_array]) VALUES "+
                "(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement cmd = _connection.prepareStatement(sql);

        cmd.setInt(1, id);
        cmd.setString(2,FormatDate(issue_date));
        cmd.setString(3,watershed);
        cmd.setString(4,location_name);
        cmd.setString(5, FormatDate(timeseries_start_date));
        cmd.setInt(6, member_length);
        cmd.setInt(7, member_count);
        cmd.setBoolean(8, compressed  );
        cmd.setBytes(9, byte_value_array);
        cmd.execute();
    }


    private int GetMaxID()
    {
        String sql = "SELECT max(id) FROM "+TableName;
        int rval = -1;
        try {
            Statement stmt  = _connection.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                rval = rs.getInt("id");
                return rval;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return rval;
    }

    private void  CreateTable()throws Exception
    {
        String sql = "CREATE TABLE IF NOT EXISTS " + TableName
                + " ( id integer not null primary key,"
                + "    issue_date datetime, "
                + "   watershed NVARCHAR(100) ,"
                + "   location_name NVARCHAR(100) ,"
                + "   timeseries_start_date datetime ,"
                + "   member_length integer    ,"
                + "   member_count integer    ,"
                + "   compressed integer    ,"
                + "  byte_value_array BLOB NULL )";
        PreparedStatement cmd = _connection.prepareStatement(sql);
        cmd.execute();
    }



//https://stackoverflow.com/questions/14777800/gzip-compression-to-a-byte-array
    private static byte[] gzipCompress(byte[] uncompressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length);
             GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
            gzipOS.write(uncompressedData);
            // You need to close it before using bos
            gzipOS.close();
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static byte[] gzipUncompress(byte[] compressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPInputStream gzipIS = new GZIPInputStream(bis)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }



    // assuming float[][] is not jagged
    //https://stackoverflow.com/questions/4635769/how-do-i-convert-an-array-of-floats-to-a-byte-and-back
    private static byte[] ConvertToBytes(float[][] data)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length*data[0].length * 4);

        FloatBuffer fBuffer = byteBuffer.asFloatBuffer();
        for (int i = 0; i <data.length ; i++) {
            fBuffer.put(data[i],);
        }

        byte[] array = byteBuffer.array();

        for (int i=0; i < array.length; i++)
        {
            System.out.println(i + ": " + array[i]);
        }

    }
}
