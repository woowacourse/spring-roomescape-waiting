package roomescape.dto.user.command;

import roomescape.dto.user.request.CreateUserRequest;

public record CreateUserCommand(
        String username,
        String password,
        String name
) {
    public static CreateUserCommand from(CreateUserRequest request) {
        return new CreateUserCommand(request.username(), request.password(), request.name());
    }
}