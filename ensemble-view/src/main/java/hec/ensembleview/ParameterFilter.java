package hec.ensembleview;

import hec.RecordIdentifier;

public class ParameterFilter {
    public static RecordIdentifier checkParameter(RecordIdentifier recordIdentifier) {
        String parameter = recordIdentifier.parameter;
        switch (parameter.toUpperCase()) {
            case "FLOW":
            case "MOISTURE DEFICIT":
            case "SWE":
            case "PRECIP-INC":
            case "PRECIP-LWASS":
            case "TEMPERATURE-AIR":
            case "BULK-FLOW-COMBINE":
            case "SEDIMENT LOAD-COMBINE":
            case "SEDIMENT LOAD-OUT":
            case "SEDIMENT VOLUME-OUT":
            case "BULK FLOW-OUT":
            case "BULK FLOW-COMBINE":
                return recordIdentifier;
            default:
                return null;
        }
    }
}
