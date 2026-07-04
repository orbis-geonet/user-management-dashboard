package to.orbis.dashboard.models.dto.email;

import lombok.Data;
import to.orbis.dashboard.models.dto.OneFieldDto;
import to.orbis.dashboard.models.dto.statistic.CampaignStatisticResultDto;
import to.orbis.dashboard.models.entity.types.EmailCampaignStatus;

import java.util.List;
import java.util.Set;

@Data
public class EmailCampaignDto {
    String id;
    String emailCampaignKey;
    String name;
    EmailCampaignStatus status;
    String error;
    List<EmailCampaignTagDto> tags;
    Set<OneFieldDto> tagsOneField;
    Set<EmailCampaignInfoDto> emails;
    Long emailCount;
    Long cratedTime;
    Integer sendOpenEmailIn;
    String mailSubjectFirst;
    String mailBodyFirst;
    String mailBodyFileNameFirst;
    Boolean mailBodyFileFirst;
    String mailSubjectSecond;
    String mailBodySecond;
    String mailBodyFileNameSecond;
    Boolean mailBodyFileSecond;
    CampaignStatisticResultDto statistic;
    Boolean useAllEmails;
    Boolean useOpen;
    Boolean useNotOpen;
    String parentEmailCampaignKey;
    String timeZone;
    String startDate;
    String remindDate;
    Boolean sendReminder;
    Boolean sendSecondEmail;
    Boolean autoSend;
}
