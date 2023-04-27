from hec.script import Constants
from hec.model import RunTimeStep
from hec.heclib.util import HecTime
from hec import SqliteDatabase
import os

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

SILENT = False
BAD_CHARS = "\/()., !@#$%^&*" # this list is incomplete but should suffice
def svNameFormatter(recID, statistic):
    # this function creates the name to save each SV to.
    # any rules about ResSim SV names should be applied here to ensure consistency
    name = "%s_%s" % (recID.toString(), statistic)
    for bc in BAD_CHARS:
        name = name.replace(bc, "")
    return name

def hasSV(network, svName):
    return not network.getStateVariable(svName) is None

def dimensions(array):
    # returns tuple of dimensions
    # assume 2-d rectangular array coming from Java, otherwise this will fail or be misleading with a list of lists.
    return((len(array), len(array[0])))

def getMetricValue(mcts, issueDate):
    mc = mcts.getMetricCollection(issueDate)
    mcVals = mc.getValues()
    # check dimensions, do something? if they are not 1,1
    if dimensions(mcVals) != (1,1):
        print("mcVals dimensions = (%d,%d)")
    mcVal = mcVals[0][0]
    return mcVal

def rtsOf(issueDate, rtw):
    # Convert issue date into an RTS
    ht = HecTime()
    minutesPastMidnight = issueDate.getMinute() + issueDate.getHour()*60
    ht.setYearMonthDay(issueDate.getYear(), issueDate.getMonthValue(), issueDate.getDayOfMonth(), minutesPastMidnight)
    idRTS = RunTimeStep(rtw)
    idRTS.setStep(rtw.getStepAtTime(ht))
    return idRTS

def getLastIssueDate(rts, issueDateList):
    # return the last valid issue date for the current RTS
    # TODO: validate this method does what is intended
    rtw = rts.getRunTimeWindow()
    prev_idate = issueDateList[0]
    for idate in issueDateList:
        rts_i = rtsOf(idate, rtw)
        # loop until we find the next idate
        if rts.getStep() < rts_i.getStep():
            # then return the previous idate
            return prev_idate
        prev_idate = idate
    return idate

def populateSV(sv, ensembleDB, recID, stat):
    # for each timestep in compute, retrive value from ensemble TS and apply to SV
    # ... method for matching values is repeating last value until new timestep found.
    # ? How do we match timezones, midnight values, etc.  Convert all to GMT?
    # any method to match timestamps.
    mcts = ensembleDB.getMetricCollectionTimeSeries(recID, stat)
    issueDates = mcts.getIssueDates()
    if not SILENT: sv.getSystem().printMessage(issueDates.toString())
    rtw = sv.getSystem().getRssRun().getRunTimeWindow()
    nModelSteps = rtw.getNumSteps()
    curIssueDate = None

    rts = RunTimeStep(rtw)
    for intStep in range(nModelSteps):
        rts.setStep(intStep)
        # do something to align issueDates and rts, assumes more RTSes than issue dates, and first RTS after first issuedate
        curIssueDate = getLastIssueDate(rts, issueDates)
        if not SILENT: sv.getSystem().printMessage("%s: %s" % (curIssueDate.toString(), rts.dateTimeString()))
        mcVal = getMetricValue(mcts, curIssueDate)
        sv.setValue(rts, mcVal)

def initStateVariable(currentVariable, network):
    rssAltFilename = network.getRssRun().getAltPath()
    eventFolder = rssAltFilename.split("rss")[0]
    if not SILENT: network.printErrorMessage("looking for ensembles.db file in %s" % eventFolder)
    ensembleDB = SqliteDatabase(os.path.join(eventFolder,"ensembles.db"), SqliteDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE)
    statistics = ensembleDB.getMetricStatistics()
    # for each location and statistic, check if an SV exists
    for recID in statistics.keySet():
        if not SILENT: network.printMessage(recID.toString())
        for stat in statistics[recID]:
            if not SILENT: network.printMessage(stat)
            svName = svNameFormatter(recID, stat)
            # if an SV matching the formatted name exists, echo
            if hasSV(network, svName):
                network.printMessage("found SV: %s" % svName)
                sv = network.getStateVariable(svName)
                populateSV(sv, ensembleDB, recID, stat)
            else:
                network.printWarningMessage("failed to find SV: %s" % svName)

    ensembleDB.close() # important!


    # return Constants.TRUE if the initialization is successful and Constants.FALSE if it failed.
    # Returning Constants.FALSE will halt the compute.
    return Constants.TRUE