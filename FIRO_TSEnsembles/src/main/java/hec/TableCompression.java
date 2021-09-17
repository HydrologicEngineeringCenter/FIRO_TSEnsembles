package hec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

 public class TableCompression {


    /**
     * UnPack a GZIP a byte array
     * returns a 2d Array of float (float[rows][columns]) that is not jagged
     * @param data input byte array
     * @param rowCount number of rows to unpack to
     * @param columnCount number of columns to unpack to
     * @param compression compression method ('gzip' is only method supported)
     * @return ensemble data (each row is the same size)
     */
    public static float[][] UnPack(byte[] data, int rowCount, int columnCount, String compression)
    {
        byte[] bytes;
        if( compression.equals("gzip"))
            bytes = gzipUncompress(data);
        else
            bytes = data;
       float[][] rval = ConvertFromBytes(bytes,rowCount,columnCount);
       return rval;
    }

    /**
     * Pack an 2d float array (float[][]) into a 1d byte array blob
     * if compression is not defined the data is not compressed.
     *
     * @param data float[][] array to be compressed
     * @param compression method for compression
     * @return float array compressed with input compression
     */
    public static byte[] Pack(float[][] data, String compression)
    {

        byte[] bytes = ConvertToBytes(data);
        if( !compression.equals("gzip"))
            return bytes;
        byte[] compressed =  gzipCompress(bytes);
        return compressed;
    }


    //https://stackoverflow.com/questions/14777800/gzip-compression-to-a-byte-array
    private static byte[] gzipCompress(byte[] uncompressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length);
             GZIPOutputStream gzipOS = new GZIPOutputStream(bos /*,65536,false*/)) {
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

//https://www.evanjones.ca/software/java-bytebuffers.html
// You want to allocate a single direct ByteBuffer, and reuse it for all I/O to and from a particular channel.
    // assuming float[][] is not jagged
    //   https://stackoverflow.com/questions/14619653/how-to-convert-a-float-into-a-byte-array-and-vice-versa/20698700#20698700
    private static byte[] ConvertToBytes(float[][] data)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length*data[0].length * 4).order(ByteOrder.LITTLE_ENDIAN);
        //byteBuffer.clear()
        for (int i = 0; i <data.length ; i++) {
            float[] row = data[i];
            //FloatBuffer fb = FloatBuffer.wrap(row);
            for (int j = 0; j <row.length ; j++) {
                byteBuffer.putFloat(row[j]);
            }
        }
        return byteBuffer.array();
    }

    private static float[][] ConvertFromBytes(byte[] data, int rowCount, int columnCount)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(data);
        byteBuffer.rewind();
        float[][] rval = new float[rowCount][columnCount];

        for (int i = 0; i <rowCount ; i++) {
            for (int j = 0; j <columnCount ; j++) {
                rval[i][j] = byteBuffer.getFloat();
            }
        }
        return rval;
    }
}
