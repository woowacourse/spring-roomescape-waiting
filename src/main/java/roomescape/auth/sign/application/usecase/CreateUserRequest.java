package roomescape.auth.sign.application.usecase;

import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.user.application.dto.SignUpRequest;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

public record CreateUserRequest(UserName name,
                                Email email,
                                Password password) {

    public static CreateUserRequest from(final SignUpRequest signUpRequest, final Password password) {
        return new CreateUserRequest(
                signUpRequest.name(),
                signUpRequest.email(),
                password
        );
    }

    public User toDomain() {
        return User.withoutId(name, email, password, UserRole.NORMAL);
    }
}
