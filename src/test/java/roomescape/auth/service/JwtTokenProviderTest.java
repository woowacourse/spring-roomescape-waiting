package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.core.token.JwtTokenProvider;
import roomescape.auth.core.token.TokenProperties;
import roomescape.auth.core.token.TokenProvider;
import roomescape.auth.domain.AuthInfo;
import roomescape.common.exception.UnAuthorizationException;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;

class JwtTokenProviderTest {
    private static final String SECRET_KEY = "secret";

    @Test
    @DisplayName("인증 객체 추출 성공")
    void extractAuthInfo() {
        TokenProvider tokenProvider = new JwtTokenProvider(new TokenProperties(SECRET_KEY, 1000));
        Member member = MemberFixture.getOneWithId(1L);
        String token = tokenProvider.createToken(member);

        assertThat(tokenProvider.extractAuthInfo(token))
                .isEqualTo(new AuthInfo(member.getId(), member.getName(), member.getRole()));
    }

    @Test
    @DisplayName("인증 객체 추출 실패: 토큰의 형식이 다름")
    void extractAuthInfo_WhenTokenIsMalformed() {
        TokenProvider tokenProvider = new JwtTokenProvider(new TokenProperties(SECRET_KEY, 100));

        assertThatThrownBy(() -> tokenProvider.extractAuthInfo("ㅁㄴㅇㄹㅇㄹ"))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessage("토큰의 형식이 유효하지 않습니다. 다시 로그인해주세요.");
    }

    @Test
    @DisplayName("인증 객체 추출 실패: 토큰 만료")
    void extractAuthInfo_WhenTokenIsExpired() {
        TokenProvider tokenProvider = new JwtTokenProvider(new TokenProperties(SECRET_KEY, 1));
        String token = tokenProvider.createToken(MemberFixture.getOneWithId(1L));

        assertThatThrownBy(() -> tokenProvider.extractAuthInfo(token))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessage("토큰이 만료되었습니다. 다시 로그인해주세요.");
    }

    @Test
    @DisplayName("인증 객체 추출 실패: 토큰의 비밀 키가 다름")
    void extractAuthInfo_WhenTokenSignatureIsInvalid() {
        TokenProvider tokenProvider = new JwtTokenProvider(new TokenProperties(SECRET_KEY, 100));
        TokenProvider tokenProviderWithOtherSecretKey = new JwtTokenProvider(new TokenProperties("asdf", 100));
        String token = tokenProvider.createToken(MemberFixture.getOneWithId(1L));

        assertThatThrownBy(() -> tokenProviderWithOtherSecretKey.extractAuthInfo(token))
                .isInstanceOf(UnAuthorizationException.class)
                .hasMessage("토큰의 값을 인증할 수 없습니다. 다시 로그인해주세요.");
    }
}
