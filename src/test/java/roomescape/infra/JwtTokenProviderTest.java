package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.TokenException;

class JwtTokenProviderTest {

    private final String testKey = "woowacourse-6th-abcdefg-123456-abcdefg-123456";
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(testKey, 3600000);

    @Test
    @DisplayName("토큰을 생성한다.")
    void createToken() {
        String token = jwtTokenProvider.createToken("1");

        assertThatCode(() -> jwtTokenProvider.getMemberId(token))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("토큰에서 멤버 아이디를 가져온다.")
    void getMemberId() {
        String token = jwtTokenProvider.createToken("1");

        Long memberId = jwtTokenProvider.getMemberId(token);

        assertThat(memberId).isEqualTo(1L);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("토큰에서 멤버 아이디를 가져올 때, 토큰이 비어있으면 예외를 발생시킨다.")
    void getMemberIdWhenTokenIsEmpty(String token) {
        assertThatThrownBy(() -> jwtTokenProvider.getMemberId(token))
                .isInstanceOf(TokenException.class)
                .hasMessage("토큰이 비어있습니다.");
    }


    @Test
    @DisplayName("토큰에서 멤버 아이디를 가져올 때, 토큰이 만료되었으면 예외를 발생시킨다.")
    void getMemberIdWhenTokenIsExpired() {
        JwtTokenProvider expiredJwtTokenProvider = new JwtTokenProvider(testKey, 0);
        String token = expiredJwtTokenProvider.createToken("1");

        assertThatThrownBy(() -> expiredJwtTokenProvider.getMemberId(token))
                .isInstanceOf(TokenException.class)
                .hasMessage("만료된 토큰입니다.");
    }

    @Test
    @DisplayName("토큰에서 멤버 아이디를 가져올 때, 유효하지 않은 토큰이면 예외를 발생시킨다.")
    void getMemberIdWhenTokenIsInvalid() {
        String invalidToken = "a";

        assertThatThrownBy(() -> jwtTokenProvider.getMemberId(invalidToken))
                .isInstanceOf(TokenException.class)
                .hasMessage("유효하지 않은 토큰입니다.");
    }
}
