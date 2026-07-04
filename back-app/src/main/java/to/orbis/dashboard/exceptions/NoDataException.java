package to.orbis.dashboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NoDataException extends ResponseStatusException {
    public NoDataException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
