package roomescape.user.application.service;

import roomescape.auth.sign.application.usecase.CreateUserRequest;
import roomescape.user.domain.User;

public interface UserCommandService {

    User create(CreateUserRequest request);
}
