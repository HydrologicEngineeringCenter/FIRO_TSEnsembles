# FIRO_TSEnsembles

[![GitHub Action](https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml/badge.svg)](https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml)


This is a project intended to develop a set of tools to read in data from the River Forecast Centers and convert it into a sqlite database. This database will be used as a format to share data on disk between plugins in HEC-WAT and possibly CWMS.


Download the latest Build here: 

https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml


## Standalone ResSim Setup

Download the zip file [here](https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/releases/download/beta-0.1/beta-0.1-FIRO_TSEnsembles.zip)

unzip and copy the jars to  HEC-ResSim\jar\ext

Edit Hec-ResSim.config setting HasGlobalVariables to true and temp directory for SQLite as shown below.

```
#:------------------------------------------------------:
#:Set the following parameter to "true" to turn on      :
#:Global Variables                                      :
#:------------------------------------------------------:
vmparam -DHasGlobalVariables=true

vmparam -Dorg.sqlite.tmpdir=c:\temp
```


Another way is to put these in the class path.   If multiple programs are using the Jars such as both HEC-WAT and HEC-ResSim the jars can be placed in a shared location.
This can be done by editing C:\Programs\HEC-ResSim-3.5.0.116\HEC-ResSim.config.
(example location)
```
addjars C:\FIRO_TSEnsembles\libs

```

## Sample Data

sample data ResSim.db 
https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/tree/master/FIRO_TSEnsembles/src/test/resources/database


## Issue
You may see an error such as this, where virius protection program is preventing the sqlitejdbc.dll from running in the default temp directory.

java.lang.UnsatisfiedLinkError: C:\Users\happy\AppData\Local\Temp\1\sqlite-3.30.1-db89cb97-d483-489a-b248-956d0783d9d5-sqlitejdbc.dll: Access is denied

## Workaound
add this line below to HEC-ResSim.config

vmparam -Dorg.sqlite.tmpdir=c:\temp

