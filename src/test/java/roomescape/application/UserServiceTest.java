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
import roomescape.exception.NotFoundException;

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

    @Test
    @DisplayName("사용자의 전체 기록을 조회할 수 있다.")
    void findTotalRecordByUserId() {
        // given
        var userId = 1L;

        // when
        var records = service.findTotalRecordByUserId(userId);

        // then
        assertThat(records).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 전체 기록 조회 시 예외가 발생한다.")
    void findTotalRecordByUserId_WhenUserNotFound() {
        // given
        var invalidUserId = 1000000L;

        // when & then
        assertThatThrownBy(() -> service.findTotalRecordByUserId(invalidUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }
}
