# FIRO_TSEnsembles

[![Build status](https://ci.appveyor.com/api/projects/status/kbb4m4pn5pe7bo37?svg=true)](https://ci.appveyor.com/project/ktarbet/firo-tsensembles)

This is a project intended to develop a set of tools to read in data from the River Forecast Centers and convert it into a sqlite database. This database will be used as a format to share data on disk between plugins in HEC-WAT and possibly CWMS.


Download the latest Build here: 

 https://ci.appveyor.com/project/ktarbet/firo-tsensembles/build/artifacts


## ResSim Setup

The firo-tsensembles.jar has the following dependencies:
jdbc-api-1.4.jar
sqlite-jdbc-3.30.1.jar

These need to be in the class path.  This can be done by editing C:\Programs\HEC-ResSim-3.5.0.116\HEC-ResSim.config.
(example location)
```
addjars C:\FIRO_TSEnsembles\libs

```
where the above libs directory contains firo-tsensembles.jar  and its dependencies
The dependencies can be downloaed 
https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles/releases/tag/v0.1
