package to.orbis.dashboard.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MediaUploadResponseDto {
    private MediaUploadStatus mediaUploadStatus;
    private String fileName;
    private String errorMessage;
}
