package to.orbis.dashboard.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.services.admin.email.EmailCampaignBulkUploadService;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCampaignTasks {
    private final EmailCampaignBulkUploadService emailCampaignBulkUploadService;

    private final static int MAX_PAGE_SIZE = 20;

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.SECONDS)
    public void emailCampaignTask() {
        PageRequest page = PageRequest.of(0, MAX_PAGE_SIZE);

        emailCampaignBulkUploadService.runAutoSending(page);
    }

}
