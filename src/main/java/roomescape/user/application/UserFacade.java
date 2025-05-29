package roomescape.user.application;

import roomescape.user.application.dto.UserResponse;

import java.util.List;

public interface UserFacade {

    List<UserResponse> getAll();
}
