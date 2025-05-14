package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.AuthenticationInfo;
import roomescape.domain.AuthenticationTokenHandler;
import roomescape.domain.UserRole;

class JwtTokenProviderTest {

    private final AuthenticationTokenHandler tokenProvider = new JwtTokenHandler();

    @Test
    @DisplayName("인증 정보로부터 토큰을 생성한다.")
    void createToken() {
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);
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
        AuthenticationInfo extracted = tokenProvider.extractAuthenticationInfo(token);

        // then
        assertAll(
            () -> assertThat(extracted.id()).isEqualTo(1L),
            () -> assertThat(extracted.role()).isEqualTo(UserRole.ADMIN)
        );
    }

    @Test
    @DisplayName("유효한 토큰에 대한 유효성 여부를 검사한다.")
    void isValidToken() {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);

        // when
        boolean isValidToken = tokenProvider.isValidToken(token);

        // then
        assertThat(isValidToken).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰에 대한 유효성 여부를 검사한다.")
    void isValidToken2() {
        // given
        var authenticationInfo = new AuthenticationInfo(1L, UserRole.ADMIN);
        var token = tokenProvider.createToken(authenticationInfo);
        var forgedToken = token.substring(0, token.length() - 1);

        // when
        boolean isValidToken = tokenProvider.isValidToken(forgedToken);

        // then
        assertThat(isValidToken).isFalse();
    }
}
