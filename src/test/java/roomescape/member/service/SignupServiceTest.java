package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.fixture.FakeMemberRepositoryFixture;
import roomescape.jwt.TokenProvider;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.RegistrationRequest;
import roomescape.member.repository.MemberRepository;
import roomescape.repository.FakeTokenProvider;

@DisplayName("사용자 생성")
class SignupServiceTest {

    private final MemberRepository memberRepository = FakeMemberRepositoryFixture.create();
    private final TokenProvider fakeTokenProvider = new FakeTokenProvider();
    private final AuthService authService = new AuthService(memberRepository, fakeTokenProvider);

    @DisplayName("사용자를 생성할 수 있다")
    @Test
    void signupTest() {
        // given
        RegistrationRequest request = new RegistrationRequest("브라운", "brown@gmail.com", "wooteco7");

        // when & then
        assertThatNoException().isThrownBy(() -> authService.signup(request));
    }

    @DisplayName("동일한 이메일의 사용자를 중복 생성할 수 없다")
    @Test
    void signupDuplicateTest() {
        // given
        RegistrationRequest request = new RegistrationRequest("브라운", "brown@gmail.com", "wooteco7");

        // when
        authService.signup(request);

        // then
        assertThatThrownBy(() -> authService.signup(request)).isInstanceOf(ConflictException.class);
    }

    @DisplayName("올바른 사용자 정보를 전달하면 로그인 토큰을 생성한다")
    @Test
    void createTokenTest() {
        // given
        LoginRequest request = new LoginRequest("wooteco7", "admin@gmail.com");

        // when
        String token = authService.createToken(request);
        String expected = "admin@gmail.com";

        // then
        assertThat(token).isEqualTo(expected);
    }

    @DisplayName("토큰 생성 시 사용자 정보가 잘못되면 예외가 발생한다")
    @Test
    void createTokenExceptionTest() {
        // given
        LoginRequest request = new LoginRequest("", "admin@gmail.com");

        // when & then
        assertThatThrownBy(() -> authService.createToken(request)).isInstanceOf(NotFoundException.class);
    }
}
