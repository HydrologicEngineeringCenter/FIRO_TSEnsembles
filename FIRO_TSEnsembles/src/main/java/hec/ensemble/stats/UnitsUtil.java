package hec.ensemble.stats;

import javax.measure.Unit;

import static javax.measure.MetricPrefix.*;
import static si.uom.NonSI.TONNE;
import static systems.uom.common.USCustomary.*;
import static systems.uom.common.USCustomary.HOUR;
import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.unit.Units.*;

public class UnitsUtil {
    public static Unit<?> convert(String units) {
        switch (units.toLowerCase()) {
            case "kg.m-2.s-1":
            case "kg/m2s":
            case "mm/s":
                return MILLI(METRE).divide(SECOND);
            case "mm hr^-1":
            case "mm/hr":
                return MILLI(METRE).divide(HOUR);
            case "mm/day":
            case "mm/d":
                return MILLI(METRE).divide(DAY);
            case "kg.m-2":
            case "kg/m^2":
            case "kg m^-2":
            case "mm":
                return MILLI(METRE);
            case "in":
            case "inch":
            case "inches":
                return INCH;
            case "celsius":
            case "degrees c":
            case "deg c":
            case "c":
                return CELSIUS;
            case "degc-d":
                return CELSIUS.multiply(DAY);
            case "fahrenheit":
            case "degf":
            case "deg f":
            case "f":
                return FAHRENHEIT;
            case "kelvin":
            case "k":
                return KELVIN;
            case "watt/m2":
                return WATT.divide(SQUARE_METRE);
            case "j m**-2":
                return JOULE.divide(SQUARE_METRE);
            case "kph":
                return KILO(METRE).divide(HOUR);
            case "%":
                return PERCENT;
            case "hpa":
                return HECTO(PASCAL);
            case "m":
                return METRE;
            case "cfs":
            case "ft3/s":
            case "ft^3/s":
                return CUBIC_FOOT.divide(SECOND);
            case "cms":
            case "m3/s":
            case "m^3/s":
                return CUBIC_METRE.divide(SECOND);
            case "cm":
            case "m3":
            case "m^3":
                return CUBIC_METRE;
            case "kcm":
            case "km3":
            case "km^3":
                return KILO(CUBIC_METRE);
            case "kcfs":
                return KILO(CUBIC_FOOT.divide(SECOND));
            case "kcms":
                return KILO(CUBIC_METRE.divide(SECOND));
            case "tonne":
            case "tonnes":
            case "metric ton":
                return TONNE;
            default:
                return ONE;
        }
    }

    public static String getOutputUnitString(Unit<?> unit) {
        if (unit.equals(CUBIC_FOOT.divide(SECOND))) {
            return "ACRE-FT";
        } else if (unit.equals(KILO(CUBIC_FOOT.divide(SECOND)))) {
            return "1000 ACRE-FT";
        } else if (unit.equals(CUBIC_METRE.divide(SECOND))) {
            return "M3";
        } else if (unit.equals(KILO(CUBIC_METRE.divide(SECOND)))) {
            return "1000 M3";
        } else if (unit.equals(INCH)) {
            return "IN";
        }        else {
            return unit.getName().toUpperCase();
        }
    }
}
