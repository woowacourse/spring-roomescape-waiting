package roomescape.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.user.application.dto.UserResponse;
import roomescape.user.application.service.UserQueryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFacadeImpl implements UserFacade {

    private final UserQueryService userQueryService;

    @Override
    public List<UserResponse> getAll() {
        return UserResponse.from(
                userQueryService.getAll());
    }
}
