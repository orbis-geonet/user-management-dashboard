package to.orbis.dashboard.utils.mappers;

import org.bson.types.ObjectId;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import to.orbis.dashboard.models.dto.UserSerImportDto;
import to.orbis.dashboard.models.entity.UserSet;

import java.time.Instant;

@Mapper(componentModel = "spring",
        imports = {ObjectId.class, Instant.class},
        disableSubMappingMethodsGeneration = true,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserSetMapper {

    @Mapping(target = "id", expression = "java(source.getId().toHexString())")
    UserSerImportDto toUserSerImportDto(UserSet source);

    @Mapping(target = "id", expression = "java(createId(source.getId()))")
    @Mapping(target = "uploadFileName", source = "fileName")
    @Mapping(target = "timestamp", expression = "java(Instant.now())")
    UserSet toUserSet(UserSerImportDto source, String fileName);

    void merge(@MappingTarget UserSet target, UserSet source);

    default ObjectId createId(String id) {
        if (id == null || id.isEmpty()) {
            return new ObjectId();
        } else {
            try {
                return new ObjectId(id);
            } catch (Exception e) {
                return new ObjectId();
            }

        }
    }
}
