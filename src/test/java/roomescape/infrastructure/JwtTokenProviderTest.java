package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
