UPDATE version set version = '20200409';

alter table catalog rename column units to meta_info;
alter table catalog add constraint catalog_unique_name UNIQUE (name,datatype,meta_info);

CREATE TABLE IF NOT EXISTS timeseries_information(
  catalog_id int references catalog(id),
  timeseries_subtype text;
)