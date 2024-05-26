package roomescape.auth.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.auth.dto.LoginRequest;
import roomescape.global.auth.jwt.dto.TokenDto;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;

import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@Import(AuthService.class)
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("존재하는 회원의 email, password로 로그인하면 memberId, accessToken을 Response 한다.")
    void loginSuccess() {
        // given
        String email = "test@test.com";
        String password = "test@test.com";
        memberRepository.save(new Member("이름", email, password, Role.MEMBER));

        // when
        TokenDto response = authService.login(new LoginRequest(email, password));

        // then
        assertAll(
                () -> Assertions.assertThat(response.accessToken()).isNotNull(),
                () -> Assertions.assertThat(response.refreshToken()).isNotNull()
        );
    }

    @Test
    @DisplayName("존재하지 않는 회원 email 또는 password로 로그인하면 예외를 발생한다.")
    void loginFailByNotExistMemberInfo() {
        // given
        String notExistEmail = "invalid@test.com";
        String notExistPassword = "invalid1234";

        // when & then
        Assertions.assertThatThrownBy(() -> authService.login(new LoginRequest(notExistEmail, notExistPassword)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 memberId로 로그인 여부를 체크하면 예외를 발생한다.")
    void checkLoginFailByNotExistMemberInfo() {
        // given
        Long notExistMemberId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() -> authService.checkLogin(notExistMemberId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("유효하지 않은 JWT로 토큰 갱신 요청을 보내면 예외를 발생한다.")
    void failRefreshTokenByInvalidToken() {
        // given
        String invalidAccessToken = "hihihihihihihihihihihihihihihihihihi";
        String invalidRefreshToken = "hihihihihihihihihihihihihihihihihihi";

        // when & then
        Assertions.assertThatThrownBy(() -> authService.reissueToken(invalidAccessToken, invalidRefreshToken))
                .isInstanceOf(UnauthorizedException.class);
    }
}
