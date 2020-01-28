#####
##### STATE VARIABLE SCRIPT INITIALIZATION SECTION
#####

from hec.script import Constants
from hec.ensemble import JdbcEnsembleTimeSeriesDatabase
from hec.ensemble import EnsembleTimeSeries
from hec.ensemble import TimeSeriesIdentifier
from hec.ensemble import Ensemble
#
# initialization function. optional.
# set up tables and other things that only need to be performed once at the start of the compute.
#
# variables that are passed to this script during the compute initialization:
# 	currentVariable - the StateVariable that holds this script
# 	network - the ResSim network
#
# throw a hec.rss.lang.StopComputeException from anywhere in the script to
# have ResSim stop the compute.
# from hec.rss.lang import StopComputeException
# raise StopComputeException("the reason to stop the compute")
#
def initStateVariable(currentVariable, network):
	# return Constants.TRUE if the initialization is successful and Constants.FALSE if it failed.  
	# Returning Constants.FALSE will halt the compute.
	db = JdbcEnsembleTimeSeriesDatabase("C:/project/FIRO_TSEnsembles/ResSim.db",False)
	tsid = TimeSeriesIdentifier("Coyote.fake_forecast","flow")
	ets = db.getEnsembleTimeSeries(tsid)  
	currentVariable.varPut("EnsembleTS", ets)
	return Constants.TRUE

#####
##### STATE VARIABLE SCRIPT COMPUTATION SECTION
#####

from java.time import  ZoneId
from java.time import  ZonedDateTime

# This Script demonstrates using ensembles (FIRO_TSEnsembles.jar) within ResSim
# https://github.com/HydrologicEngineeringCenter/FIRO_TSEnsembles

# The ResSim environment needs FIRO_TSEnsembles.jar and sqlite-jdbc-3.30.1.jar to be in the class path

# The Hec-ResSim.config can be modified to add to the class path

# addjars ..\FIRO_TSEnsembles\build\libs


# computeReleaseInfo reads an ensemble to determine reservoir release criteria
#
def computeReleaseInfo(t):
  ets = currentVariable.varGet("EnsembleTS")
  e = ets.getEnsemble(t, 25)  # 25 hours tolerance for forecast  

  if e is None:
    raise Exception("Error: forecast not found for date: "+ t )
    
  print "ensemble loaded"
  issueDate = e.getIssueDate()
  currentVariable.varPut("lastEnsembleTimestamp",issueDate)
  currentVariable.varPut("nextEnsembleTimestamp",issueDate.plusDays(1))
  computeVolumes(e,7) # compute 7 day volume
  
  return t.getDayOfMonth() %2
  
  
def getZonedDateTime(currentRuntimestep):
  jd = currentRuntimestep.getHecTime().getJavaDate(0) # zero timezone offset
  t = ZonedDateTime.ofInstant(jd.toInstant(),ZoneId.of("America/Los_Angeles"))
  return  t


# compute volume for the first steps
# for each ensemble member
def computeVolumes(ensemble, steps):
 
  values = ensemble.getValues()
  numMembers =len(values) 
  numSteps = min(steps,len(values[0]))
 
  rval = [0]* numMembers
 
  print "numMembers =",numMembers
  print "numSteps =",numSteps

  for member in range(0,numMembers-1):
    for i in range(0,numSteps-1):
      rval[member] += values[member][i]


  
# no return values are used by the compute from this script.
#
# variables that are available to this script during the compute:
# 	currentVariable - the StateVariable that holds this script
# 	currentRuntimestep - the current RunTime step 
# 	network - the ResSim network

# The following represents an undefined value in a time series
# 	Constants.UNDEFINED

# to set the StateVariable's value use:
# 	currentVariable.setValue(currentRuntimestep, newValue)
# where newValue is the value you want to set it to.


 
t = getZonedDateTime(currentRuntimestep)
 
isFirst = not currentVariable.varExists("lastEnsembleTimestamp")
nextEnsembleTimestamp = currentVariable.varGet("nextEnsembleTimestamp")

if isFirst or t.compareTo(nextEnsembleTimestamp) >= 0 :
  R = computeReleaseInfo(t)
  currentVariable.varPut("ReleaseInfo",R)
  print "R=",R

  print t
else:
  R = currentVariable.varGet("ReleaseInfo")
	
if R is None:
 raise StopComputeException("Error: missing ensemble forecast")

 

tempValue = R
currentVariable.setValue(currentRuntimestep, tempValue)

#####
##### STATE VARIABLE SCRIPT CLEANUP SECTION
#####

from hec.script import Constants
#
# script to be run only once, at the end of the compute. optional.

# variables that are available to this script during the compute:
# 	currentVariable - the StateVariable that holds this script
# 	network - the ResSim network

# The following represents an undefined value in a time series:
# 	Constants.UNDEFINED
#
# throw a hec.rss.lang.StopComputeException from anywhere in the script to
# have ResSim stop the compute.
# from hec.rss.lang import StopComputeException
# raise StopComputeException("the reason to stop the compute")

# add your code here...
