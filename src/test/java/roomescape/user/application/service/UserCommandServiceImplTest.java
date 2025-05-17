package roomescape.user.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.application.usecase.CreateUserRequest;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class UserCommandServiceImplTest {

    @Autowired
    private UserCommandServiceImpl userCommandService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("올바르게 유저를 저장할 수 있다.")
    void create() {
        //given
        UserName userName = UserName.from("강산");
        Email email = Email.from("email@email.com");
        Password password = Password.fromEncoded("1234");
        CreateUserRequest request = new CreateUserRequest(userName, email, password);
        final User user = userCommandService.create(request);

        UserId userId = user.getId();

        //when
        User actual = userRepository.findById(userId)
                .orElseThrow();

        assertThat(actual.getName()).isEqualTo(userName);
        assertThat(actual.getEmail()).isEqualTo(email);
        assertThat(actual.getPassword()).isEqualTo(password);
        assertThat(actual.getRole()).isEqualTo(UserRole.NORMAL);
    }
}
