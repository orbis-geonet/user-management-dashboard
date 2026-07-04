package to.orbis.dashboard.tasks.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostingServiceResponse {
    private boolean postingResult;
    private String postingMessage;
}
