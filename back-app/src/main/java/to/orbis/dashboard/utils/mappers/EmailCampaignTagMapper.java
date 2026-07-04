package to.orbis.dashboard.utils.mappers;

import org.mapstruct.*;
import to.orbis.dashboard.models.dto.email.EmailCampaignDto;
import to.orbis.dashboard.models.dto.email.EmailCampaignTagDto;
import to.orbis.dashboard.models.dto.list.EmailCampaignTagListDto;
import to.orbis.dashboard.models.entity.email.EmailCampaignTag;

@Mapper(componentModel = "spring",
        disableSubMappingMethodsGeneration = true,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EmailCampaignTagMapper {

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    @Mapping(target = "tagName", source = "name")
    EmailCampaignTagListDto toEmailCampaignTagListDto(EmailCampaignTag source);

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    @Mapping(target = "tagName", source = "name")
    EmailCampaignTagDto toEmailCampaignTagDto(EmailCampaignTag source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "tagName")
    EmailCampaignTag toEmailCampaignTag(EmailCampaignTagDto source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(EmailCampaignDto source, @MappingTarget EmailCampaignDto target);
}
