package hec.collections.storage;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import hec.collections.Collection;
import hec.collections.CollectionIdentifier;
import hec.collections.TimeSeriesCollection;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;

public class BasicCollectionStorage implements CollectionStorage {

    @Override
    public void write(Connection connection, Collection collection_src, String table_name) throws Exception {
        TimeSeriesCollection collection = (TimeSeriesCollection) collection_src;
        ResultSet id_rs = null;
        ArrayList<Integer> ids = new ArrayList<>();
        try (PreparedStatement insert_collection_entry = connection
                .prepareStatement("insert into collections(collection_id,catalog_id) values (?,?)");
                PreparedStatement catalog_id = connection
                        .prepareStatement("select id from view_catalog where entry = ?");
                PreparedStatement delete_collection = connection
                        .prepareStatement("delete from collections where collection_id = ?");) {
            catalog_id.setString(1, collection.identifier().catalogName());
            id_rs = catalog_id.executeQuery();
            int collection_id = id_rs.getInt("id");
            id_rs.close();

            delete_collection.setInt(1, collection_id);
            delete_collection.execute();
            id_rs.close();

            for (TimeSeries ts : collection) {
                catalog_id.setString(1, ts.identifier().catalogName());
                id_rs = catalog_id.executeQuery();
                ids.add(id_rs.getInt("id"));
            }

            for (Integer id : ids) {
                insert_collection_entry.setInt(1, collection_id);
                insert_collection_entry.setInt(2, id);
                insert_collection_entry.addBatch();
            }

            insert_collection_entry.executeBatch();

        } catch (Exception err) {
            throw err;
        } finally {
            if (id_rs != null)
                id_rs.close();
        }

    }

    @Override
    public String tableCreate() {

        return null;
    }

    @Override
    public Collection read(Connection connection, CollectionIdentifier identifier, String table_name,
            ZonedDateTime start, ZonedDateTime end, TimeSeriesRetriever retriever) throws Exception {    
        ResultSet rs = null;
        try(
                PreparedStatement select_collection_id = connection.prepareStatement(
                    "select id from view_catalog where entry = ?"
                );
                PreparedStatement select_member_identifiers = connection.prepareStatement(
                    "SELECT name,meta_info from catalog where id in (select b.catalog_id from collections b where b.catalog_id=?)"
                );
        ) {
            select_collection_id.setString(1,identifier.catalogName());
            rs = select_collection_id.executeQuery();
            rs.next();
            int collection_id = rs.getInt("id");
            rs.close();

            TimeSeriesCollection collection = new TimeSeriesCollection(identifier);

            select_member_identifiers.setInt(1,collection_id);
            rs = select_member_identifiers.executeQuery();
            while(rs.next()){
                String name = rs.getString("name");
                String meta = rs.getString("meta_info");
                
                collection.addMember(retriever.retrieve((TimeSeriesIdentifier)TimeSeriesIdentifier.fromCatalogEntry(name, meta),start,end));
            }


            return collection;
        } catch( Exception err ){
            throw err;
        } finally {
            if( rs != null ) rs.close();
        }
    }

}