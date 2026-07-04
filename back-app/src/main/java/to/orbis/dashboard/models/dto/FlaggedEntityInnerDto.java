package to.orbis.dashboard.models.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedEntityInnerDto {
    private String id;
    private String postTitle;
    private String postDetails;
    private String postKey;
    private String name;
    private String description;
    private String groupKey;
    private String emailUser;
    private String displayNameUser;
    private String userKey;
    private Instant timestamp;
    private Instant createTimestamp;
    private Instant reportedTime;
    private Boolean deleted;
    private Boolean reportedSolved;
    private String placeKey;
}
