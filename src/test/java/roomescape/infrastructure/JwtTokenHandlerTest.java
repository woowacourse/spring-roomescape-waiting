package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.UserRole;

class JwtTokenHandlerTest {

    private final AuthenticationTokenHandler tokenProvider = new JwtTokenHandler();

    @Test
    @DisplayName("인증 정보로부터 토큰을 생성한다.")
    void createToken() {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);

        // when
        var token = tokenProvider.createToken(authenticationInfo);

        // then
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("토큰으로부터 식별자를 추출한다.")
    void extractId() {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);

        // when
        var id = tokenProvider.extractId(token);

        // then
        assertThat(id).isEqualTo(1L);
    }

    @Test
    @DisplayName("토큰으로부터 인증 정보를 추출한다.")
    void extractAuthenticationInfo() {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);

        // when
        var extractedAuthInfo = tokenProvider.extractAuthenticationInfo(token);

        // then
        assertAll(
                () -> assertThat(extractedAuthInfo.id()).isEqualTo(1L),
                () -> assertThat(extractedAuthInfo.role()).isEqualTo(UserRole.ADMIN)
        );
    }

    @Test
    @DisplayName("유효한 토큰에 대한 유효성 여부를 검사한다.")
    void isValidToken() {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);

        // when
        var isValidToken = tokenProvider.isValidToken(token);

        // then
        assertThat(isValidToken).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰에 대한 유효성 여부를 검사한다.")
    void isValidTokenWithInvalidToken() {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);
        var forgedToken = token.substring(0, token.length() - 1);

        // when
        var isValidToken = tokenProvider.isValidToken(forgedToken);

        // then
        assertThat(isValidToken).isFalse();
    }
}
