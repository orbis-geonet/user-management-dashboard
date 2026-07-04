package to.orbis.dashboard.models.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FollowUserShortDto {
    private String id;
    private String followId;
    private String email;
    private String userKey;
    private boolean accepted;
    private boolean seen;
}
