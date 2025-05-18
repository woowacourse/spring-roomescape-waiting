package roomescape.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;
import roomescape.user.ui.dto.UserResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserFacadeImplTest {

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private UserFacadeImpl userFacade;

    @Test
    @DisplayName("모든 사용자를 조회한다")
    void getAll() {
        List<User> users = List.of(
                createUser(1L, "user1", "user1@example.com", "useruser"),
                createUser(2L, "user2", "user2@example.com", "useruser")
        );
        given(userQueryService.getAll()).willReturn(users);

        List<UserResponse> result = userFacade.getAll();

        assertThat(result).hasSize(2);
        then(userQueryService).should(times(1)).getAll();
    }

    private User createUser(Long id, String name, String email, String password) {
        return User.withId(
                UserId.from(id),
                UserName.from(name),
                Email.from(email),
                Password.fromEncoded(password),
                UserRole.NORMAL
        );
    }
}
