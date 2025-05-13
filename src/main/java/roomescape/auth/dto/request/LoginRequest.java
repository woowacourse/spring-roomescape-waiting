package roomescape.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "이메일이 입력되지 않았다.") String email,
        @NotNull(message = "패스워드가 입력되지 않았다.") String password) {

}
