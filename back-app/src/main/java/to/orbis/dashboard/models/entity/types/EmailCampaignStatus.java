package to.orbis.dashboard.models.entity.types;

public enum EmailCampaignStatus {
    DRAFT,
    READY_TO_SEND,
    STARTING,
    STOPPING,
    STOPPED,
    VERIFICATION_EMAIL_LIST,
    SEND_FIRST_REMINDING,
    SEND_SECOND_REMINDING,
    OPENED,
    OPENED_EMAIL_SENT,
    OPENED_EMAIL_CANNOT_SENT,
    CLICK_UNSUBSCRIBE,
    CLICK_LINK,
    SEND,
    FINISH,
    ERROR;

    public static EmailCampaignStatus getNextStatus(EmailCampaignStatus previousType) {
        switch (previousType) {
            case READY_TO_SEND:
                return SEND_FIRST_REMINDING;
            case SEND_FIRST_REMINDING:
                return SEND_SECOND_REMINDING;
            case SEND_SECOND_REMINDING:
                return FINISH;
            default:
                return ERROR;
        }
    }
}
