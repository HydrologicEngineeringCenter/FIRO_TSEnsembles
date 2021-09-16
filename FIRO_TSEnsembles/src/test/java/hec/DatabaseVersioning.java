package hec;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;


public class DatabaseVersioning{

    @Test
    public void Original_db_can_update_to_newest() throws Exception{        
        File testdb = get_test_file("/database/ResSimTest_20200101.db");                
        try(VersionableDatabase db = new JdbcDatabase(testdb.getCanonicalPath(), JdbcDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);){
            assertEquals( get_latest_version(db.getVersions()), db.getVersion() );
        } catch( Exception err ){
            fail(err);
        }
        
        testdb.delete();
    }

    private File get_test_file( String test_database_resource ) throws Exception{
        InputStream testdb_stream = this.getClass().getResourceAsStream(test_database_resource);
        Path tmpdbfile = Files.createTempFile("updatetests", "db");
        FileOutputStream file = new FileOutputStream(tmpdbfile.toFile());
        byte buffer[] = new byte[4096];
        int length;
        while((length = testdb_stream.read(buffer,0,4096))> 0){
            file.write(buffer,0,length);
        }
        file.close();
        return tmpdbfile.toFile();
    }

    private String get_latest_version( List<String> versions) throws Exception{
        return versions.get(versions.size()-1);
    }

}