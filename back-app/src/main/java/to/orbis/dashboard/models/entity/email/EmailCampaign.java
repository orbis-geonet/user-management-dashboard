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
import java.util.Set;

@Data
@ToString
@NoArgsConstructor
@Document(collection = "emailCampaigns")
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants(asEnum = true)
public class EmailCampaign extends Entity {

    String emailCampaignKey;
    String name;
    EmailCampaignStatus status;
    Set<String> tagIds;
    Instant cratedTime;
    Integer sendOpenEmailIn;
    String mailSubjectFirst;
    String mailBodyFirst;
    String mailBodyFileNameFirst;
    Boolean mailBodyFileFirst;
    String error;
    String templateFirstName;
    String templateFirstAmazonId;
    Boolean useAllEmails;
    Boolean useOpen;
    Boolean useNotOpen;
    String parentEmailCampaignKey;

    String mailSubjectSecond;
    String mailBodySecond;
    String mailBodyFileNameSecond;
    Boolean mailBodyFileSecond;
    String templateSecondName;
    String templateSecondAmazonId;

    String timeZone;
    Instant startDate;
    Instant remindDate;
    Instant nextSendDate;

    Boolean sendReminder;
    Boolean sendSecondEmail;
    Boolean autoSend;
}
