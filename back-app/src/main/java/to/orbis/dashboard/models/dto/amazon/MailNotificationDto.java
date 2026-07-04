package to.orbis.dashboard.models.dto.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailNotificationDto {
    String messageId;
}
