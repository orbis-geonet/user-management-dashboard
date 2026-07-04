package to.orbis.dashboard.models.entity.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import to.orbis.dashboard.exceptions.EnumValidationException;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum PeriodStatisticType {
    DAY, WEEK, MONTH;

    @JsonCreator
    public static PeriodStatisticType create(String value) throws EnumValidationException {
        if (value == null || value.isEmpty())
            return null;

        try {
            return PeriodStatisticType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new EnumValidationException("Valid values for post type are: "+
                    Arrays.stream(PeriodStatisticType.values()).map(Enum::name).collect(Collectors.joining(", ")));
        }
    }
}
