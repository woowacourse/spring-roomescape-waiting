package roomescape.dto.request;

import static roomescape.dto.InputValidator.validateNotBlank;
import static roomescape.dto.InputValidator.validateNotNull;

public record LoginRequest(String email, String password) {

    public LoginRequest {
        validateNotNull(email, password);
        validateNotBlank(email, password);
    }
}
