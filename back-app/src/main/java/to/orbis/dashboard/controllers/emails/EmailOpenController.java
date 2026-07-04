package to.orbis.dashboard.controllers.emails;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.amazon.MessageNotificationDto;
import to.orbis.dashboard.models.dto.amazon.NotificationConfirmationDto;
import to.orbis.dashboard.services.admin.email.EmailCampaignService;
import to.orbis.dashboard.services.admin.email.EmailService;
import to.orbis.dashboard.utils.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
@RequestMapping("/api/v1/public/emails")
public class EmailOpenController {

    private final JsonUtil jsonUtil;
    private final EmailService emailService;
    private final EmailCampaignService emailCampaignService;

    @GetMapping("/unsubscribe/{id}")
    public void unsubscribe(
            @PathVariable String id,
            @RequestParam String emailInfoId
    ) {
        log.info("unsubscribe: id={} emailInfoId={}", id, emailInfoId);
        emailService.unsubscribe(id, emailInfoId);
    }

    @SneakyThrows
//    @PostMapping("/handleEvent")
    public void getMessageOpenEvent(
            HttpServletRequest request
    ) {
        String type = request.getHeader("x-amz-sns-message-type");
        String body = request.getReader().lines().collect(Collectors.joining());
        log.info("getMessageOpenEvent: type={} body={}", type, body);
        switch (type) {
            case "SubscriptionConfirmation": {
                NotificationConfirmationDto subscriptionConfirmation = jsonUtil.readFromString(body, new TypeReference<>(){});
                log.info("getMessageOpenEvent: subscriptionConfirmation={}", subscriptionConfirmation);
                emailCampaignService.confirmAmazonUrl(subscriptionConfirmation);
                break;
            }
            case "Notification": {
                MessageNotificationDto mailEvent = jsonUtil.cleanContextAndReadFromString(body, new TypeReference<>(){});
                log.info("getMessageOpenEvent: mailEvent={}", mailEvent);
                emailCampaignService.handleAmazonSesEvent(mailEvent.getMessage());
                break;
            }
        }
    }
}
