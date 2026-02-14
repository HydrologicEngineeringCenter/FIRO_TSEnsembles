UPDATE version SET version = '20250506';



ALTER TABLE metrics_timeseries ADD COLUMN version text;

DROP VIEW IF EXISTS view_metriccollection;

CREATE VIEW view_metriccollection AS
SELECT mts.id AS metriccollection_timeseries_id, location, parameter_name, units, version, metric_type,
    issue_datetime, start_datetime, member_length, member_count, compression, interval_seconds, statistics, byte_value_array
FROM metrics_timeseries mts JOIN metrics m
ON m.metriccollection_timeseries_id = mts.id;

--DROP INDEX if EXISTS idx_u_metric_timeseries;
--CREATE UNIQUE INDEX if not EXISTS idx_u_metric_timeseries ON metrics_timeseries(location,parameter_name,version);