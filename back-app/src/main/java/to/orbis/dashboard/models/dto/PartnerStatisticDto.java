package to.orbis.dashboard.models.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants(asEnum = true)
@FieldDefaults(makeFinal = false, level = AccessLevel.PROTECTED)
public class PartnerStatisticDto {
    String columnName;
    Integer number;
}
