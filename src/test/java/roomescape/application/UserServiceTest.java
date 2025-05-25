package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("새로운 사용자를 등록할 수 있다.")
    void saveUser() {
        // given
        var email = "new@email.com";
        var password = "password123";
        var name = "새사용자";

        // when
        User created = service.saveUser(email, password, name);

        // then
        var users = userRepository.findAll();
        assertThat(users).contains(created);
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 사용자 등록 시 예외가 발생한다.")
    void saveUser_WhenEmailAlreadyExists() {
        // given
        var existingEmail = "user1@email.com";
        var password = "password123";
        var name = "새사용자";

        // when & then
        assertThatThrownBy(() -> service.saveUser(existingEmail, password, name))
                .isInstanceOf(AlreadyExistedException.class)
                .hasMessage("이미 해당 이메일로 가입된 사용자가 있습니다.");
    }

    @Test
    @DisplayName("모든 사용자를 조회할 수 있다.")
    void findAllUsers() {
        // when
        var users = service.findAllUsers();

        // then
        assertThat(users).hasSize(3);
    }
}
