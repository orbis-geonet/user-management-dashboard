package to.orbis.dashboard.utils.mappers;

import org.mapstruct.*;
import to.orbis.dashboard.models.dto.email.EmailCampaignCopyDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignInfoDto;
import to.orbis.dashboard.models.dto.list.EmailCampaignListDto;
import to.orbis.dashboard.models.entity.email.EmailCampaign;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfo;

import java.time.Instant;
import java.util.Objects;

@Mapper(componentModel = "spring",
        disableSubMappingMethodsGeneration = true,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EmailCampaignMapper {

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    @Mapping(target = "tags", ignore = true)
    EmailCampaignListDto toEmailCampaignListDto(EmailCampaign source);

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    @Mapping(target = "emailCount", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "emails", ignore = true)
    @Mapping(target = "statistic", ignore = true)
    @Mapping(target = "startDate", expression = "java(to.orbis.dashboard.utils.DateUtils.getTimeForDto(source.getStartDate(), source.getTimeZone()))")
    @Mapping(target = "remindDate", expression = "java(to.orbis.dashboard.utils.DateUtils.getTimeForDto(source.getRemindDate(), source.getTimeZone()))")
    EmailCampaignDto toEmailCampaignDto(EmailCampaign source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentEmailCampaignKey", ignore = true)
    @Mapping(target = "nextSendDate", ignore = true)
    @Mapping(target = "startDate", expression = "java(to.orbis.dashboard.utils.DateUtils.getTime(source.getStartDate(), source.getTimeZone()))")
    @Mapping(target = "remindDate", expression = "java(to.orbis.dashboard.utils.DateUtils.getTime(source.getRemindDate(), source.getTimeZone()))")
    EmailCampaign toEmailCampaign(EmailCampaignDto source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(EmailCampaign source, @MappingTarget EmailCampaign target);

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    EmailCampaignInfoDto toEmailCampaignInfoDto(EmailCampaignInfo source);

    @Mapping(target = "id", expression = "java(new org.bson.types.ObjectId(source.getId()))")
    EmailCampaignInfo toEmailCampaignInfo(EmailCampaignInfoDto source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void merge(EmailCampaignInfo source, @MappingTarget EmailCampaignInfo target);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailCampaignKey", ignore = true)
    @Mapping(target = "name", expression = "java(source.getName() + \"-copy\")")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "tagIds", source = "source.tagIds")
    @Mapping(target = "cratedTime", expression = "java(java.time.Instant.now())")
    @Mapping(target = "sendOpenEmailIn", source = "source.sendOpenEmailIn")
    @Mapping(target = "mailSubjectFirst", source = "source.mailSubjectFirst")
    @Mapping(target = "mailBodyFirst", source = "source.mailBodyFirst")
    @Mapping(target = "mailBodyFileNameFirst", source = "source.mailBodyFileNameFirst")
    @Mapping(target = "mailBodyFileFirst", source = "source.mailBodyFileFirst")
    @Mapping(target = "error", ignore = true)
    @Mapping(target = "templateFirstName", source = "source.templateFirstName")
    @Mapping(target = "templateFirstAmazonId", source = "source.templateFirstAmazonId")
    @Mapping(target = "useAllEmails", source = "source.useAllEmails")
    @Mapping(target = "useOpen", source = "emailCampaignCopyDto.copyOpened")
    @Mapping(target = "useNotOpen", source = "emailCampaignCopyDto.copyNotOpened")
    @Mapping(target = "parentEmailCampaignKey", source = "source.emailCampaignKey")
    @Mapping(target = "mailSubjectSecond", source = "source.mailSubjectSecond")
    @Mapping(target = "mailBodySecond", source = "source.mailBodySecond")
    @Mapping(target = "mailBodyFileNameSecond", source = "source.mailBodyFileNameSecond")
    @Mapping(target = "mailBodyFileSecond", source = "source.mailBodyFileSecond")
    @Mapping(target = "templateSecondName", source = "source.templateSecondName")
    @Mapping(target = "templateSecondAmazonId", source = "source.templateSecondAmazonId")
    @Mapping(target = "timeZone", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "remindDate", ignore = true)
    EmailCampaign cloneEmailCampaign(EmailCampaign source, EmailCampaignCopyDto emailCampaignCopyDto);

    default Long toLong(Instant instant) {
        return Objects.nonNull(instant) ? instant.toEpochMilli() : null;
    }

    default Instant toInstant(Long time) {
        return Objects.nonNull(time) ? Instant.ofEpochMilli(time) : null;
    }

}
