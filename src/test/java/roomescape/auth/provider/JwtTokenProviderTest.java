package roomescape.auth.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import roomescape.auth.domain.Token;
import roomescape.auth.exception.TokenException;
import roomescape.config.TestConfig;
import roomescape.exception.model.RoomEscapeException;

@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Token을 정상적으로 만든다.")
    void getAccessToken() {
        Token token = jwtTokenProvider.getAccessToken(1);
        assertNotNull(token.getToken());
    }

    @Test
    @DisplayName("토큰이 null일 경우 예외를 던진다.")
    void shouldThrowException_WhenTokenIsNull() {
        Throwable nullToken = assertThrows(RoomEscapeException.class, () -> jwtTokenProvider.resolveToken(null));

        assertEquals(nullToken.getMessage(), TokenException.TOKEN_IS_EMPTY_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("토큰이 비어있을 경우 예외를 던진다.")
    void shouldThrowException_WhenTokenIsBlank() {
        Throwable emptyToken = assertThrows(RoomEscapeException.class, () -> jwtTokenProvider.resolveToken(" "));

        assertEquals(emptyToken.getMessage(), TokenException.TOKEN_IS_EMPTY_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("잘못된 형식의 토큰일 경우 예외를 던진다.")
    void shouldThrowException_WhenTokenIsInvalidForm() {
        Throwable illegalToken = assertThrows(RoomEscapeException.class,
                () -> jwtTokenProvider.resolveToken("eydjsndjnjsJ92nj.njnjdl"));

        assertEquals(illegalToken.getMessage(), TokenException.FAILED_PARSE_TOKEN_EXCEPTION.getMessage());
    }
}
