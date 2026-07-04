package to.orbis.dashboard.exceptions;

public class CampaignSendingException extends RuntimeException {
    public CampaignSendingException(String reason) {
        super(reason);
    }
}
