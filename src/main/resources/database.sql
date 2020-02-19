--drop table if exists ensemble_timeseries;
--drop table if exists ensemble;
--drop view if exists view_ensemble;

CREATE TABLE IF NOT EXISTS table_types(
  id integer not null primary key,
  name text,
  description text,
  table_prefix text  
);

CREATE TABLE IF NOT EXISTS catalog(
  id integer not null primary key,
  name text,
  datatype int references table_types(id),
  units text
);



CREATE TABLE IF NOT EXISTS paired_data_information(
  catalog_id int references catalog(id)
);

INSERT INTO table_types(name,table_prefix,description)
  VALUES ('Paired Data', 'paired_data_','
  Simple paired data, can have any number of independant 
  variables, column names will be:
  indep1[,indepN]* dep1[,depM]*
  
  the table name will end up being
  paired_data_iNdM
  where N and M are the number of independant and dependant
  values respectively

  ');



CREATE TABLE IF NOT EXISTS ensemble_timeseries
      ( id integer not null primary key,
       location NVARCHAR(100), 
       parameter_name NVARCHAR(100), 
       units NVARCHAR(100), 
       data_type NVARCHAR(100), 
       version NVARCHAR(100),
       catalog_id int references catalog(id)
				 );
CREATE UNIQUE INDEX if not EXISTS idx_u_ensemble_timeseries ON ensemble_timeseries(location,parameter_name);

				 
CREATE TABLE IF NOT EXISTS ensemble
      ( id integer not null primary key,
	  ensemble_timeseries_id integer NOT NULL,
      issue_datetime datetime, 
      start_datetime datetime, 
      member_length integer  ,
      member_count integer  ,
      compression  NVARCHAR(100),
      interval_seconds integer ,
      byte_value_array BLOB NULL ,
	  FOREIGN KEY(ensemble_timeseries_id) REFERENCES ensemble_timeseries(id)
				 );


CREATE view IF NOT EXISTS  view_ensemble  AS
Select ETS.id as ensemble_timeseries_id, location,parameter_name,units,version, issue_datetime, start_datetime, 
member_length, member_count, compression, interval_seconds, byte_value_array
  from ensemble_timeseries ETS join  ensemble E
on  E.ensemble_timeseries_id = ETS.id;
