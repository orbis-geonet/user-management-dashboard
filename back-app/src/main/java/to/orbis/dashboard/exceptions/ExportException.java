package to.orbis.dashboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExportException extends ResponseStatusException {
    public ExportException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
