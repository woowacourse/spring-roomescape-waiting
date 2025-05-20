package roomescape.user.application;

import roomescape.user.ui.dto.UserResponse;

import java.util.List;

public interface UserFacade {

    List<UserResponse> getAll();
}
