package roomescape.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;
import roomescape.user.ui.dto.UserResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserQueryService userQueryService;

    @Override
    public List<UserResponse> getAll() {
        return UserResponse.from(
                userQueryService.getAll());
    }
}
