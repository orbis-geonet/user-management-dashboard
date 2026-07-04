package to.orbis.dashboard.utils.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import to.orbis.dashboard.models.dto.openstreetmap.OpenMapPlace;
import to.orbis.dashboard.models.dto.openstreetmap.PlaceInfoDto;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OpenMapMapper {

    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "cityDistrict", source = "address.cityDistrict")
    @Mapping(target = "suburb", source = "address.suburb")
    @Mapping(target = "municipality", source = "address.municipality")
    @Mapping(target = "county", source = "address.county")
    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "stateDistrict", source = "address.stateDistrict")
    PlaceInfoDto toPlaceInfo(OpenMapPlace source);
}
