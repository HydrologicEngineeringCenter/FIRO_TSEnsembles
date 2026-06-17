# FIRO_TSEnsembles

[![GitHub Action](https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml/badge.svg)](https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml)


This is a project intended to develop a set of tools to read in data from the River Forecast Centers and convert it into a sqlite database. This database will be used as a format to share data on disk between plugins in HEC-WAT and possibly CWMS.


Download the latest Build here: 

https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/actions/workflows/gradle.yml


## Running from the source with gradle

```bat
echo setup Java env
set JAVA_HOME=C:\Programs\Java\java-11-openjdk-11.0.9.11-3
set path=%JAVA_HOME%\bin;%PATH%
::  from the FIRO_TSEnsembles directory
gradlew run
```




## Standalone ResSim Setup

Install ResSim  currently: 3.5.0.283 

Download the zip file [here](https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/releases/download/beta-0.1/beta-0.1-FIRO_TSEnsembles.zip)

unzip and copy the jars to  HEC-ResSim\jar\ext

Edit Hec-ResSim.config verify HasGlobalVariables to true and temp directory for SQLite as shown below.

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

## Release Versions and Git Tags

Versioning is driven by a single git tag on the `master` branch. To cut a release, create a tag of the
form `vX.Y` (for example, `v1.1`) on the commit in `master` you want to publish. That one tag versions
all three artifacts (TS-Ensembles, Ensemble-View, and Dss-Ensembles) uniformly: the leading `v` is
stripped, so a `v1.1` tag publishes every jar as version `1.1`.

Any build without a matching `vX.Y` tag is published as a `-SNAPSHOT`.

## Sample Data

sample data ResSim.db 
https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/tree/master/FIRO_TSEnsembles/src/test/resources/database


## Issue
You may see an error such as this, where virius protection program is preventing the sqlitejdbc.dll from running in the default temp directory.

java.lang.UnsatisfiedLinkError: C:\Users\happy\AppData\Local\Temp\1\sqlite-3.30.1-db89cb97-d483-489a-b248-956d0783d9d5-sqlitejdbc.dll: Access is denied

## Workaound
add this line below to HEC-ResSim.config

vmparam -Dorg.sqlite.tmpdir=c:\temp

