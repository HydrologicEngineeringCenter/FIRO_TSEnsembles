CREATE TABLE version(
  version text not null primary key
);

INSERT INTO version(version) values ('20200507');


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

  '),
  ('Ensemble Time Series',NULL, '
    The original ensemble time series test.
  
  '),
  ('Collection','collection','
  
  ')
  ;

alter table ensemble_timeseries add column catalog_id int references catalog(id);

