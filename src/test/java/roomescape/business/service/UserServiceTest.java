package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Users;
import roomescape.exception.business.InvalidCreateArgumentException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private Users users;

    @InjectMocks
    private UserService sut;

    @Test
    void 사용자_등록이_성공적으로_이루어진다() {
        // given
        String name = "테스트유저";
        String email = "test@example.com";
        String password = "password123";

        when(users.existByEmail(email)).thenReturn(false);

        // when
        sut.register(name, email, password);

        // then
        verify(users).existByEmail(email);
        verify(users).save(any(User.class));
    }

    @Test
    void 이미_존재하는_이메일로_사용자_등록_시_예외가_발생한다() {
        // given
        String name = "테스트유저";
        String email = "test@example.com";
        String password = "password123";

        when(users.existByEmail(email)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.register(name, email, password))
                .isInstanceOf(InvalidCreateArgumentException.class);

        verify(users).existByEmail(email);
        verify(users, never()).save(any(User.class));
    }
}
