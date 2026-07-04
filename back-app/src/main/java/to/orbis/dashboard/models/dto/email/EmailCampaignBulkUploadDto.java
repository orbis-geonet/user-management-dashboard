package to.orbis.dashboard.models.dto.email;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class EmailCampaignBulkUploadDto {
    private MultipartFile file;
} 