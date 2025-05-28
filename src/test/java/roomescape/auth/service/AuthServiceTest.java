package roomescape.auth.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.auth.fixture.AuthFixture;
import roomescape.global.auth.domain.dto.TokenResponseDto;
import roomescape.global.auth.exception.InvalidTokenException;
import roomescape.global.auth.service.AuthService;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.domain.dto.UserResponseDto;
import roomescape.user.fixture.UserFixture;
import roomescape.user.repository.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AuthServiceTest {

    private static final String NAME = "username";
    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "password";

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("토큰으로 유저 찾는 기능")
    class findMemberByToken {

        private User savedUser;
        private String accessToken;

        @BeforeEach
        void setUPLogin() {
            savedUser = userRepository.save(UserFixture.create(Role.ROLE_MEMBER, NAME, EMAIL, PASSWORD));
            TokenResponseDto tokenResponseDto = authService.login(AuthFixture.createTokenRequestDto(EMAIL, PASSWORD));
            accessToken = tokenResponseDto.accessToken();
        }

        @DisplayName("유효한 토큰으로 요청 시 유저를 찾을 수 있다")
        @Test
        void findMemberByToken_success_byValidToken() {
            // given
            // when
            UserResponseDto userResponseDto = authService.findMemberByToken(accessToken);

            // then
            Assertions.assertThat(userResponseDto.id()).isEqualTo(savedUser.getId());
            Assertions.assertThat(userResponseDto.roleName()).isEqualTo(savedUser.getRole().name());
            Assertions.assertThat(userResponseDto.name()).isEqualTo(savedUser.getName());
            Assertions.assertThat(userResponseDto.email()).isEqualTo(savedUser.getEmail());
            Assertions.assertThat(userResponseDto.password()).isEqualTo(savedUser.getPassword());
        }

        @DisplayName("유효하지 않은 토큰으로 요청 시 예외 발생 : 상태코드 401 반환")
        @Test
        void findMemberByToken_throwException_byInvalidToken() {
            // given
            String invalidToken = "invalidToken";

            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> authService.findMemberByToken(invalidToken)
            ).isInstanceOf(InvalidTokenException.class);
        }
    }
}
