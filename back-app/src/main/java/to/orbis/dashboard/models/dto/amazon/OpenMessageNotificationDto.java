package to.orbis.dashboard.models.dto.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMessageNotificationDto {
    Instant timestamp;
}
