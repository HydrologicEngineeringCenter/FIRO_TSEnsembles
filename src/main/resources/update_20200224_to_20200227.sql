

UPDATE version set version = '20200227';

DROP view view_ensemble;

CREATE view IF NOT EXISTS  view_ensemble  AS
Select ETS.id as ensemble_timeseries_id, location,parameter_name,data_type,units,version, issue_datetime, start_datetime,
       member_length, member_count, compression, interval_seconds, byte_value_array
from ensemble_timeseries ETS join  ensemble E
  on  E.ensemble_timeseries_id = ETS.id;


