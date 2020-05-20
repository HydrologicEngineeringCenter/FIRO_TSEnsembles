UPDATE version set version = '20200507';

CREATE TABLE IF NOT EXISTS collection_information(
  catalog_id integer not null primary key,
  name text unique,
  subtype text
);

CREATE TABLE IF NOT EXISTS collections(
  collection_id integer references collection_information(id),
  catalog_id integer references catalog(id),
  UNIQUE(collection_id,catalog_id)
);

INSERT INTO table_types(name,table_prefix,description)
  VALUES 
  ('Time Series','timeseries_','
  Any TimeSeries Data. Various objects will be stored in different way.
  
  The timeseries information table contains a further description of 
  how this timeseries is actually stored.  
  '),
  (
    'Collection','collection_',
    'A grouping of objects within this database. 
    Often the same datatype, such as timeseries. 
    However, this is not a requirement'    
  );

CREATE VIEW IF NOT EXISTS view_catalog AS
select id, 
       datatype || '|' || name || '|' || meta_info as entry
from
      catalog;