package hec.ensembleview;

import hec.RecordIdentifier;

public class ParameterFilter {
    /**
     * Checks if the cumulative option is applicable for the given parameter.
     * Parameters like elevation, temperature, and moisture deficit are not
     * meaningful as cumulative values.
     *
     * @param parameter the parameter name to check
     * @return true if cumulative is allowed, false if it should be disabled
     */
    public static boolean isCumulativeApplicable(String parameter) {
        switch (parameter.toUpperCase()) {
            case "MOISTURE DEFICIT":
            case "ELEV":
            case "ELEVATION":
            case "TEMPERATURE-AIR":
                return false;
            default:
                return true;
        }
    }

    public static RecordIdentifier checkParameter(RecordIdentifier recordIdentifier) {

        String parameter = recordIdentifier.parameter;
        switch (parameter.toUpperCase()) {
            case "FLOW":
            case "MOISTURE DEFICIT":
            case "SWE":
            case "ELEV":
            case "ELEVATION":
            case "PRECIP-INC":
            case "PRECIP-LWASS":
            case "TEMPERATURE-AIR":
            case "BULK-FLOW-COMBINE":
            case "SEDIMENT LOAD-COMBINE":
            case "SEDIMENT LOAD-COMBINE-CLAY":
            case "SEDIMENT LOAD-COMBINE-SAND":
            case "SEDIMENT LOAD-COMBINE-SILT":
            case "SEDIMENT LOAD-COMBINE-GRAVEL":
            case "SEDIMENT LOAD-OUT":
            case "SEDIMENT LOAD-CLAY":
            case "SEDIMENT LOAD-SAND":
            case "SEDIMENT LOAD-SILT":
            case "SEDIMENT LOAD-GRAVEL":
            case "SEDIMENT LOAD-OUT-CLAY":
            case "SEDIMENT LOAD-OUT-SAND":
            case "SEDIMENT LOAD-OUT-SILT":
            case "SEDIMENT LOAD-OUT-GRAVEL":
            case "SEDIMENT VOLUME-COMBINE":
            case "SEDIMENT VOLUME-COMBINE-CLAY":
            case "SEDIMENT VOLUME-COMBINE-SAND":
            case "SEDIMENT VOLUME-COMBINE-SILT":
            case "SEDIMENT VOLUME-COMBINE-GRAVEL":
            case "SEDIMENT VOLUME":
            case "SEDIMENT VOLUME-OUT":
            case "SEDIMENT VOLUME-CLAY":
            case "SEDIMENT VOLUME-SAND":
            case "SEDIMENT VOLUME-SILT":
            case "SEDIMENT VOLUME-GRAVEL":
            case "SEDIMENT VOLUME-OUT-CLAY":
            case "SEDIMENT VOLUME-OUT-SAND":
            case "SEDIMENT VOLUME-OUT-SILT":
            case "SEDIMENT VOLUME-OUT-GRAVEL":
            case "BULK FLOW":
            case "BULK FLOW-OUT":
            case "BULK FLOW-COMBINE":
                return recordIdentifier;
            default:
                return null;
        }
    }
}
