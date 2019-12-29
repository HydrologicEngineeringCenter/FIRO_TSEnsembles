package hec.firo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class EnsembleCompression {


    /**
     * UnCompress a GZIP a byte array
     * returns an ensemble  (float[][])
     * @param data
     * @return
     */
    public static float[][] UnCompress(byte[] data, int rowCount, int columnCount)
    {
       byte[] bytes = gzipUncompress(data);
       float[][] rval = ConvertFromBytes(bytes,rowCount,columnCount);
       return rval;
    }

    /**
     * Compress an float[]] ensemble into a byte array
     * @return
     */
    public static byte[] Compress(float[][] data)
    {
        byte[] bytes = ConvertToBytes(data);
        byte[] compressed =  gzipCompress(bytes);
        return compressed;
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
    //   https://stackoverflow.com/questions/14619653/how-to-convert-a-float-into-a-byte-array-and-vice-versa/20698700#20698700
    private static byte[] ConvertToBytes(float[][] data)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length*data[0].length * 4).order(ByteOrder.LITTLE_ENDIAN);
        //byteBuffer.clear()
        for (int i = 0; i <data.length ; i++) {
            float[] row = data[i];
            for (int j = 0; j <row.length ; j++) {
                byteBuffer.putFloat(row[i]);
            }
        }
        return byteBuffer.array();
    }

    private static float[][] ConvertFromBytes(byte[] data, int rowCount, int columnCount)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(data);
        float[][] rval = new float[rowCount][columnCount];

        for (int i = 0; i <rowCount ; i++) {
            for (int j = 0; j <columnCount ; j++) {
                rval[i][j] = byteBuffer.getFloat();
            }
        }
        return rval;
    }
}
