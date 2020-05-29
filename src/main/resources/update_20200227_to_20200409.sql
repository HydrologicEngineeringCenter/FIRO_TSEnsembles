UPDATE version set version = '20200409';

alter table catalog rename column units to meta_info;

ALTER TABLE catalog RENAME TO catalog_bak;
CREATE TABLE catalog(
  id integer not null primary key,
  name text,
  datatype int references table_types(id),
  meta_info text,
  CONSTRAINT uniqueness UNIQUE (name,datatype,meta_info)
);

INSERT INTO catalog SELECT id,name,datatype,meta_info from catalog_bak;
DROP TABLE catalog_bak;

CREATE TABLE IF NOT EXISTS timeseries_information(
  catalog_id int references catalog(id),
  timeseries_subtype text
)