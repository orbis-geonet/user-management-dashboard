package to.orbis.dashboard.models.entity.email;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import to.orbis.dashboard.models.entity.Entity;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

import java.time.Instant;

import static to.orbis.dashboard.models.entity.types.EmailCampaignStatus.READY_TO_SEND;

@Data
@ToString
@NoArgsConstructor
@Document(collection = "emailCampaignsInfo")
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants(asEnum = true)
public class EmailCampaignInfo extends Entity {
    String emailKey;
    String mail;
    String name;
    String companyName;
    String phoneNumber;
    EmailCampaignStatus status = READY_TO_SEND;
    String error;
    Instant openTime;
    Instant sendOpenEmailTime;
    Instant lastSendTime;
    String amazonMessageId;
    String emailCampaignKey;
    String emailCampaignInfoKey;
    Boolean unsubscribed;
}
