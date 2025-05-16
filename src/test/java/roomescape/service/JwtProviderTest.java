package roomescape.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import roomescape.common.exception.UnauthorizedException;

@TestPropertySource(properties = {
        "security.jwt.token.secret-key=your-secret-key",
        "security.jwt.token.expire-length=3600000"
})
@SpringBootTest(classes = JwtProvider.class)
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Test
    @DisplayName("토큰을 정상적으로 생성한다")
    void test1() {
        // given
        String email = "example@gmail.com";

        // when
        String token = jwtProvider.createToken(email, new Date());

        // then
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("토큰에 담긴 정보가 정상적으로 파싱된다")
    void test2() {
        // given
        String email = "example@gmail.com";
        String token = jwtProvider.createToken(email, new Date());

        // when
        String payload = jwtProvider.getPayload(token);

        // then
        assertThat(payload).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰이 만료된 경우 예외를 던진다")
    void test3() {
        // given
        String email = "example@gmail.com";
        Date issuedAt = Date.from(LocalDate.of(2023, 12, 30)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        String token = jwtProvider.createToken(email, issuedAt);

        // when & then
        assertThatThrownBy(() -> jwtProvider.getPayload(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("토큰이 만료되었습니다.");
    }

    @Test
    @DisplayName("토큰이 유효하지 않은 상태인 경우 예외를 던진다")
    void test4() {
        // given
        String token = "invalidToken";

        // when & then
        assertThatThrownBy(() -> jwtProvider.getPayload(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("토큰이 null 인 경우 예외를 던진다")
    void test5() {
        // given
        String token = " ";

        // when & then
        assertThatThrownBy(() -> jwtProvider.getPayload(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("토큰이 비어 있습니다.");
    }
}
