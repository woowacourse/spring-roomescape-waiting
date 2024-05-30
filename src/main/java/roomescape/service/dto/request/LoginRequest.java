package roomescape.service.dto.request;

import java.util.stream.Stream;

public record LoginRequest(String email, String password) {
    public LoginRequest {
        validate(email, password);
    }

    private void validate(String... values) {
        if (Stream.of(values).anyMatch(String::isBlank)) {
            throw new IllegalArgumentException();
        }
    }
}
