package to.orbis.dashboard.models.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddUserDto {
    private String type;
    private String goalId;
    private String userKey;
}
