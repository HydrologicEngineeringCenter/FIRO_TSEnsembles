

UPDATE version set version = '20210922';

CREATE TABLE IF NOT EXISTS metrics_timeseries(
  id integer not null primary key,
  location text,
  parameter_name text,
  units text,
  metric_type text
);

CREATE TABLE IF NOT EXISTS metrics(
id integer not null primary key,
metriccollection_timeseries_id integer,
issue_datetime text,
start_datetime text,
member_length  integer not null,
member_count integer not null,
compression text,
interval_seconds integer,
statistics text,
byte_value_array BLOB,
FOREIGN KEY(metriccollection_timeseries_id) REFERENCES metrics_timeseries(id)
);


CREATE view IF NOT EXISTS  view_metriccollection  AS
Select mts.id as metriccollection_timeseries_id, location,parameter_name,units,metric_type,
        issue_datetime, start_datetime,
       member_length, member_count, compression, interval_seconds, statistics, byte_value_array
from metrics_timeseries mts join  metrics m
  on  m.metriccollection_timeseries_id = mts.id;


