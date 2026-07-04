package to.orbis.dashboard.models.dto.email;

import lombok.Data;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

import java.time.Instant;

import static to.orbis.dashboard.models.entity.types.EmailCampaignStatus.READY_TO_SEND;

@Data
public class EmailCampaignInfoDto {
    String id;
    String emailKey;
    String mail;
    String name;
    String companyName;
    String phoneNumber;
    EmailCampaignStatus status = READY_TO_SEND;
    Long openTime;
    Long sendOpenEmailTime;
    String amazonMessageId;
    String emailCampaignKey;
    String emailCampaignInfoKey;
}
