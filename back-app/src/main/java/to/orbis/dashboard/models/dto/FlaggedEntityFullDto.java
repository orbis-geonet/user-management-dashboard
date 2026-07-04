package to.orbis.dashboard.models.dto;

import lombok.*;
import to.orbis.dashboard.models.entity.types.PlaceType;
import to.orbis.dashboard.models.entity.types.PostType;
import to.orbis.dashboard.models.entity.types.ReportedEntityType;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedEntityFullDto {
    private String id;
    private String name;
    private String details;
    private String shareLink;
    private ReportedEntityType type;
    private Long timestamp;
    private Long createTimestamp;
    private Boolean deleted;

    private String reportedMessage;
    private Boolean reportedSolved;
    private Long reportedTime;

    private String imageName;

    private String email;
    private String displayName;

    private PostType postType;
    private List<MediaDto> mediaUrls;

    private PlaceType placeType;
}
