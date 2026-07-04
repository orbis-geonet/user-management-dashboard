package to.orbis.dashboard.utils.mappers;

import org.mapstruct.*;
import to.orbis.dashboard.models.dto.email.EmailDto;
import to.orbis.dashboard.models.dto.list.EmailListDto;
import to.orbis.dashboard.models.entity.email.Email;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfo;

@Mapper(componentModel = "spring",
        disableSubMappingMethodsGeneration = true,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EmailMapper {

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    @Mapping(target = "tags", ignore = true)
    EmailListDto toEmailListDto(Email source);

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    EmailDto toEmailDto(Email source);

    @Mapping(target = "id", ignore = true)
    Email toEmail(EmailDto source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge( Email source, @MappingTarget Email target);

    @Mapping(target = "id", source = "emailKey")
    @Mapping(target = "emailKey", source = "emailKey")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "mail", source = "mail")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "webSite", ignore = true)
    @Mapping(target = "cratedTime", ignore = true)
    @Mapping(target = "lastOpenEmailTime", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "unsubscribed", ignore = true)
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "lastCallDate", ignore = true)
    @Mapping(target = "nextCallDate", ignore = true)
    EmailDto toEmail(EmailCampaignInfo source);
}
