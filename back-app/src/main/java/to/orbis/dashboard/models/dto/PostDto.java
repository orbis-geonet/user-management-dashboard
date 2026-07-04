package to.orbis.dashboard.models.dto;

import lombok.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import to.orbis.dashboard.models.entity.Post;
import to.orbis.dashboard.models.entity.RichLinkData;
import to.orbis.dashboard.models.entity.types.PostType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private String id;
    private String postKey;
    private PostType type;
    private String title;
    private String details;
    private String address;
    private String shareLink;
    private String fullShareLink;

    private UserShortDto user;
    private GroupShortDto group;
    private PlaceShortDto place;

    private PointDto coordinates;

    private List<MediaDto> mediaUrls;

    private long timestamp;
    private long plannedTime;
    private long plannedEndTime;

    private boolean reported;
    private String reportedMessage;
    private Boolean reportedSolved;
    private Long reportedTime;

    private boolean deleted;

    private Long commentsNumber;
}
