package to.orbis.dashboard.models.dto.openstreetmap;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class PlaceInfoDto {
    private String city;
    private String cityDistrict;
    private String stateDistrict;
    private String suburb;
    private String municipality;
    private String county;
    private String country;
}
