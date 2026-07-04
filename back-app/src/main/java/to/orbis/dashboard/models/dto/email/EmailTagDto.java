package to.orbis.dashboard.models.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@FieldNameConstants(asEnum = true)
public class EmailTagDto {
    String id;
    String text;
}
