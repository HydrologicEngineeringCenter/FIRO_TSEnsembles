UPDATE version SET version = '20230718';

DROP INDEX if EXISTS idx_u_ensemble_timeseries;
CREATE UNIQUE INDEX if not EXISTS idx_u_ensemble_timeseries ON ensemble_timeseries(location,parameter_name,version);