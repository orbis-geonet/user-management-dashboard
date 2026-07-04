package to.orbis.dashboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ImportException extends ResponseStatusException {
    public ImportException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
