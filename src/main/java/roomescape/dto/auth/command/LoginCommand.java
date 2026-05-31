package roomescape.dto.auth.command;

import roomescape.dto.auth.request.LoginRequest;

public record LoginCommand(
        String username,
        String password
) {
    public static LoginCommand from(LoginRequest request) {
        return new LoginCommand(request.username(), request.password());
    }
}