package hec.timeseries.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import hec.TimeSeriesStorage;
import hec.timeseries.BlockedRegularIntervalTimeSeries;
import hec.timeseries.ReferenceRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;

import java.time.ZoneId;

class Block{
    public double block[];    
    public long start_date_time;
    public Block(int size){
        block = new double[size];
        reset();
    }
    public void reset(){
        for( int i = 0; i < block.length; i++){
            block[i] = Double.NEGATIVE_INFINITY;
        }
    }

    public Block merge_in( Block from ){
        if( from.block.length != block.length ){
            throw new RuntimeException("block size mismatch");
        }
        for( int i = 0; i < from.block.length; i++ ){
            if( from.block[i] != Double.NEGATIVE_INFINITY ){
                block[i] = from.block[i];
            }
        }
        return this;
    }
}

public class BlockedStorage implements TimeSeriesStorage {
    public static final String blk_table_def = "CREATE TABLE %s(block_start_datetime BIGINT primary key NOT NULL, block BLOB NOT NULL)";

    public static byte[] gzipZip(byte inflated[]) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream(inflated.length);
        GZIPOutputStream gzipOS = new GZIPOutputStream(bos /*,65536,false*/);
        gzipOS.write(inflated);
        // You need to close it before using bos
        gzipOS.close();
        return bos.toByteArray();
    }

    public static byte[] gzipUnzip(byte compressed[]) throws Exception{
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gzipIS = new GZIPInputStream(bis);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipIS.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

    public static Block convert_block( byte db_block[] ) throws Exception{
        byte inflated_block[] = gzipUnzip(db_block);
        ByteBuffer byteBuffer = ByteBuffer.allocate(inflated_block.length).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(inflated_block);
        byteBuffer.rewind();
        int number_of_doubles = inflated_block.length/Double.BYTES;
        Block block = new Block(number_of_doubles);
        for( int i = 0; i < number_of_doubles; i++ ){
            block.block[i] = byteBuffer.getDouble();
        }
        return block;
    }

    public static byte[] convert_block( Block blk ) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(blk.block.length*Double.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for( int i = 0; i < blk.block.length; i++ ){
            buffer.putDouble(blk.block[i]);
        }
        return gzipZip(buffer.array());
    }

    public static long block_start( ZonedDateTime zdt ){        
        return block_start_zdt(zdt).toEpochSecond();
    }

    public static ZonedDateTime block_start_zdt( ZonedDateTime zdt ){
        return zdt.withDayOfYear(1)
                  .withHour(0)
                  .withMinute(0)
                  .withSecond(0)
                  .withNano(0);
    }

    public static int block_addr( ZonedDateTime zdt, Duration interval ){
        ZonedDateTime block_start = block_start_zdt(zdt);
        int addr = (int)Duration.between( block_start, zdt).dividedBy(interval.getSeconds()).getSeconds();
        return addr;

    }

    public static ZonedDateTime block_addr_to_zdt( long block_start, Duration interval, int idx ){
        ZonedDateTime block_start_zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(block_start), ZoneId.of("UTC") );        
        return block_start_zdt.plus( interval.multipliedBy(idx));
    }

    public static int block_size(  long block_start, Duration interval ){
        ZonedDateTime leapcheck = ZonedDateTime.ofInstant(Instant.ofEpochSecond(block_start), ZoneId.of("UTC"));
        int days = leapcheck.toLocalDate().isLeapYear() ? 366 : 365;
        int size = (int)(days*86400 / interval.getSeconds()); //seconds in a leapyear / interval
        return size;
    }

    public static Block next_or_new(long block_start, TimeSeriesIdentifier identifier, ResultSet block_rs) throws Exception{
        Block block = null;
        if( block_rs.next ()) {
            block = convert_block( block_rs.getBytes(1) );
            block.start_date_time = block_start;
        } else {            
            block = new Block( block_size(block_start,identifier.interval() ));
            block.start_date_time = block_start;
            block.reset();
        }
        return block;
    }

    @Override
    public void write( Connection connection, String table_name, TimeSeries rts) throws Exception{
            //ResultSet block_rs = null;
            try( PreparedStatement insert_block = 
                    connection.prepareStatement(
                        "insert or replace into " + table_name + "(block_start_datetime, block) values (?,?)"
                    );
                 PreparedStatement read_block =
                    connection.prepareStatement(
                        "select block from " + table_name + " where block_start_datetime = ?"
                    );
            ) {                
                final Block block[] = new Block[1];
                block[0] = null;                
                /*
                    start building a block, 
                */
                rts.applyFunction( (time, value )->{
                    long current_block = block_start(time);
                    if( block[0] == null ){ // try to read in the block, make a new block                        
                        read_block.setLong(1,current_block);                        
                        ResultSet block_rs = read_block.executeQuery();
                        block[0] = next_or_new(current_block, rts.identifier(), block_rs);
                        block_rs.close();
                    } else if( block[0].start_date_time != current_block ){
                        //save the current block, check for the next block or reset
                        long prev_block_start = block[0].start_date_time;
                        read_block.setLong(1, prev_block_start);
                        ResultSet block_rs = read_block.executeQuery();
                        if( block_rs.next() ){
                            Block from_db = convert_block(block_rs.getBytes(1));
                            from_db.start_date_time = prev_block_start;
                            block[0] = from_db.merge_in(block[0]);                            
                        } 
                        block_rs.close();
                        insert_block.setLong(1, block[0].start_date_time);
                        insert_block.setBytes(2, convert_block(block[0]));
                        insert_block.execute();

                        read_block.setLong(1,current_block);
                        block_rs = read_block.executeQuery();
                        block[0] = next_or_new(current_block, rts.identifier(),block_rs);

                    } 
                    
                    // transfer data to the block
                    block[0].block[block_addr(time, rts.identifier().interval())] = value;
                
                    return Double.NEGATIVE_INFINITY;
                });

                //store the last block (by the time it gets here it would have already been read in)               
                insert_block.setLong(1, block[0].start_date_time);
                insert_block.setBytes(2,convert_block(block[0]));
                insert_block.execute();
            } catch(Exception err ){
                throw err;
            } finally {
                
            }
    }

    @Override
    public TimeSeries read(
                                            Connection connection, 
                                            TimeSeriesIdentifier identifier, 
                                            String table_name, 
                                            String subtype, 
                                            ZonedDateTime start, 
                                            ZonedDateTime end) 
                                            throws Exception
    {
        try(
            PreparedStatement read_blocks = connection.prepareStatement(
                "SELECT block_start_datetime,block from " + table_name + " where block_start_datetime between ? and ?"
            );
        ) {
            TimeSeries ts = new BlockedRegularIntervalTimeSeries(identifier);
            read_blocks.setLong(1, start.toEpochSecond());
            read_blocks.setLong(2, end.toEpochSecond());
            ResultSet rs = read_blocks.executeQuery();
            while(rs.next()){
                Block blk = convert_block(rs.getBytes("block"));
                blk.start_date_time = rs.getLong("block_start_datetime");                                    
                for( int i = 0; i < blk.block.length; i++ ){
                    if( blk.block[i] != Double.NEGATIVE_INFINITY ){
                        ts.addRow(block_addr_to_zdt(blk.start_date_time,identifier.interval(),i), blk.block[i]);
                    }
                }
            }
            rs.close();
            return ts;
        } catch( Exception err ){
            throw err;
        } finally {

        }        
    }

    @Override
	public String tableCreate() {
        return blk_table_def;
        
	}
}