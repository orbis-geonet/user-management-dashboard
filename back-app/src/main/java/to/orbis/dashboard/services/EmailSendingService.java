package to.orbis.dashboard.services;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.config.property.EmailSendingConfiguration;
import to.orbis.dashboard.models.dto.email.EmailMessage;
import to.orbis.dashboard.models.entity.email.EmailCampaignInfo;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendingService {
    private final JavaMailSender javaMailSender;

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    private final EmailSendingConfiguration configuration;

    @SneakyThrows
    public void sendHtmlEmail(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setText(emailMessage.getBody(), emailMessage.isHtmlBody()); // Use this or above line.
        helper.setTo(emailMessage.getTo());
        helper.setSubject(emailMessage.getSubject());
        helper.setFrom(configuration.getFrom());
        javaMailSender.send(mimeMessage);
        log.info("sendEmail: mail was send. from={} to={} subject={}", configuration.getFrom(), emailMessage.getTo(), emailMessage.getSubject());
    }

    public String createTemplate(String templateName, EmailMessage emailMessage) {
        var template = new Template();
        template.setTemplateName(templateName);
        if (emailMessage.isHtmlBody()) {
            template.setHtmlPart(emailMessage.getBody());
        } else {
            template.setTextPart(emailMessage.getBody());
        }
        template.setSubjectPart(emailMessage.getSubject());

        var request = new CreateTemplateRequest();
        request.setTemplate(template);

        var result = amazonSimpleEmailService.createTemplate(request);

        return result.getSdkResponseMetadata().getRequestId();
    }

    public String sendOneMessage(String templateName, EmailCampaignInfo emailCampaignInfo){
        var destination = new Destination();
        if (configuration.isTestMode()) {
            destination.setToAddresses(List.of(configuration.getTestModeReceiver()));
        } else {
            destination.setToAddresses(List.of(emailCampaignInfo.getMail()));
        }
        var templateData = createTemplateData(emailCampaignInfo);

        var request = new SendTemplatedEmailRequest();
        request.setTemplate(templateName);
        request.setDestination(destination);
        request.setTemplateData(templateData);
        request.setSource(String.format("\"%s\" <%s>", configuration.getFromName(), configuration.getFrom()));
        request.setConfigurationSetName(configuration.getAmazonConfigurationSetName());

        return amazonSimpleEmailService.sendTemplatedEmail(request).getMessageId();
    }

    private String createTemplateData(
            EmailCampaignInfo emailCampaignInfo
    ) {
        var name = (Objects.isNull(emailCampaignInfo.getName()) || emailCampaignInfo.getName().isEmpty()) ?
                configuration.getDefaultName() :
                String.format(" %s", emailCampaignInfo.getName());

        var companyNameWithPrefix = (Objects.isNull(emailCampaignInfo.getCompanyName()) || emailCampaignInfo.getCompanyName().isEmpty()) ?
                configuration.getDefaultCompanyName() :
                String.format(", vi que você trabalha na organização %s", emailCampaignInfo.getCompanyName());;

        var companyName = (Objects.isNull(emailCampaignInfo.getCompanyName()) || emailCampaignInfo.getCompanyName().isEmpty()) ?
                configuration.getDefaultCompanyName() : emailCampaignInfo.getCompanyName();

        var unsubscribeUrl = configuration.getUnsubscribeLink() + emailCampaignInfo.getEmailKey() + "?emailInfoId=" + emailCampaignInfo.getId().toHexString();

        return String.format(
                "{ \"name\":\"%s\", \"companyNameWithPrefix\":\"%s\", \"companyName\":\"%s\",  \"unsubscribeUrl\": \"%s\" }",
                name, companyNameWithPrefix, companyName, unsubscribeUrl
        );
    }
}
