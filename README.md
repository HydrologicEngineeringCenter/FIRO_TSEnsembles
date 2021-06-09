# FIRO_TSEnsembles

[![Build status](https://ci.appveyor.com/api/projects/status/kbb4m4pn5pe7bo37?svg=true)](https://ci.appveyor.com/project/ktarbet/firo-tsensembles)

![GitHub Action](https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml/badge.svg)


This is a project intended to develop a set of tools to read in data from the River Forecast Centers and convert it into a sqlite database. This database will be used as a format to share data on disk between plugins in HEC-WAT and possibly CWMS.


Download the latest Build here: 

https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml

alternate location
https://ci.appveyor.com/project/ktarbet/firo-tsensembles/build/artifacts


## ResSim Setup

The firo-tsensembles.jar has the following dependencies:
jdbc-api-1.4.jar
sqlite-jdbc-3.30.1.jar

copy these jars to  HEC-ResSim\jar\ext


The hard way is to put these in the class path.  This can be done by editing C:\Programs\HEC-ResSim-3.5.0.116\HEC-ResSim.config.
(example location)
```
addjars C:\FIRO_TSEnsembles\libs

```

sample data ResSim.db 
https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/tree/master/FIRO_TSEnsembles/src/test/resources/database


need to update path to ResSim.db in the script.
"C:/project/FIRO_TSEnsembles/src/test/resources/database/ResSim.db"

where the above libs directory contains firo-tsensembles.jar  and its dependencies
The dependencies can be downloaed 
https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/releases/tag/v0.1



## Issue
You may see an error such as this, where virius protection program is preventing the sqlitejdbc.dll from running in the default temp directory.

java.lang.UnsatisfiedLinkError: C:\Users\happy\AppData\Local\Temp\1\sqlite-3.30.1-db89cb97-d483-489a-b248-956d0783d9d5-sqlitejdbc.dll: Access is denied

## Workaound
add this line below to HEC-ResSim.config

vmparam -Dorg.sqlite.tmpdir=c:\temp

