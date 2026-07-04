package to.orbis.dashboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EntityException extends ResponseStatusException {
    public EntityException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
