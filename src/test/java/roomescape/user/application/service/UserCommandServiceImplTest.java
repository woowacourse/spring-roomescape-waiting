package roomescape.user.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.application.dto.CreateUserRequest;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class UserCommandServiceImplTest {

    @Autowired
    private UserCommandService userCommandService;

    @Test
    @DisplayName("사용자를 생성한다")
    void createUser() {
        // given
        final UserName userName = UserName.from("사용자 이름");
        final Email email = Email.from("test@example.com");
        final Password password = Password.fromEncoded("encodedPassword");
        final CreateUserRequest request = new CreateUserRequest(userName, email, password);

        // when
        final User result = userCommandService.create(request);

        // then
        assertAll(() -> {
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(userName);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getId()).isNotNull();
        });
    }
} 