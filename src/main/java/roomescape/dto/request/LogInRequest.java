package roomescape.dto.request;

import static roomescape.dto.request.exception.InputValidator.validateNotBlank;
import static roomescape.dto.request.exception.InputValidator.validateNotNull;

public record LogInRequest(String email, String password) {

    public LogInRequest {
        validateNotNull(email, password);
        validateNotBlank(email, password);
    }
}
