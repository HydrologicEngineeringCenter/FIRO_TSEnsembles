package hec.ensemble.stats;

import tech.units.indriya.function.RationalConverter;

import javax.measure.Dimension;
import javax.measure.IncommensurableException;
import javax.measure.Unit;

import static javax.measure.MetricPrefix.KILO;
import static systems.uom.common.USCustomary.ACRE_FOOT;
import static systems.uom.common.USCustomary.CUBIC_FOOT;
import static tech.units.indriya.unit.Units.SECOND;

public final class ConvertUnits {
    private ConvertUnits() {
    }

    static Unit<?> convertStringUnits(String units) {
        return UnitsUtil.convert(units);
    }

    static double getAccumulationConversionFactor(Unit<?> unit) throws IncommensurableException {
        if (unit.equals(CUBIC_FOOT.divide(SECOND)) || unit.equals(KILO(CUBIC_FOOT.divide(SECOND)))) {
            RationalConverter rationalConverter = (RationalConverter) unit.getConverterToAny(ACRE_FOOT.divide(SECOND));

            double dividend = rationalConverter.getDividend().doubleValue();
            double divisor = rationalConverter.getDivisor().doubleValue();

            return dividend/divisor;
        }
        return 1;
    }

    static boolean isUnitsRateUnit(Unit<?> unit) {
        Dimension test = unit.getDimension();
        return test.toString().contains("/[T]");
    }
}
