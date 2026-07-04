package to.orbis.dashboard.models.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PlaceShortDto {
    private String id;
    private String name;
    private String description;
    private String placeKey;
    private boolean deleted;
}
