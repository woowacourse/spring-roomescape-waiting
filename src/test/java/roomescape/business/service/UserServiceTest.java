package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.UserDto;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.UserName;
import roomescape.business.model.vo.UserRole;
import roomescape.exception.business.InvalidCreateArgumentException;
import roomescape.exception.business.NotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    void 이메일로_사용자를_조회할_수_있다() {
        // given
        String email = "test@example.com";
        User userData = User.restore("user-id", "USER", "Test User", email, "password123");
        UserDto expectedUser = new UserDto(Id.create("user-id"), UserRole.USER, new UserName("Test User"), new Email(email));

        when(users.findByEmail(email)).thenReturn(Optional.of(userData));

        // when
        UserDto result = sut.getByEmail(email);

        // then
        assertThat(result).isEqualTo(expectedUser);
        verify(users).findByEmail(email);
    }

    @Test
    void 존재하지_않는_이메일로_사용자_조회_시_예외가_발생한다() {
        // given
        String email = "nonexistent@example.com";

        when(users.findByEmail(email)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> sut.getByEmail(email))
                .isInstanceOf(NotFoundException.class);

        verify(users).findByEmail(email);
    }

    @Test
    void 모든_사용자를_조회할_수_있다() {
        // given
        List<User> userData = Arrays.asList(
                User.restore("user-id-1", "USER", "User One", "user1@example.com", "password1"),
                User.restore("user-id-2", "USER", "User Two", "user2@example.com", "password2")
        );
        List<UserDto> expectedUsers = Arrays.asList(
                new UserDto(Id.create("user-id-1"), UserRole.USER, new UserName("User One"), new Email("user1@example.com")),
                new UserDto(Id.create("user-id-2"), UserRole.USER, new UserName("User Two"), new Email("user2@example.com"))
        );

        when(users.findAll()).thenReturn(userData);

        // when
        List<UserDto> result = sut.getAll();

        // then
        assertThat(result).isEqualTo(expectedUsers);
        verify(users).findAll();
    }
}
