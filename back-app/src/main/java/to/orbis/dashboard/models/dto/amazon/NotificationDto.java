package to.orbis.dashboard.models.dto.amazon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationDto {
    String eventType;

    MailNotificationDto mail;

    OpenMessageNotificationDto open;
}
