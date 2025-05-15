package roomescape.user.application.service;

import roomescape.auth.sign.application.dto.CreateUserRequest;
import roomescape.user.domain.User;

public interface UserCommandService {

    User create(CreateUserRequest request);
}
