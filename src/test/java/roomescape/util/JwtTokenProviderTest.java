package roomescape.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.exception.UnauthorizedException;

class JwtTokenProviderTest {

    private final String secretKey = "thisisasecretkeyfortestingpurposesonly12345678901234567890";
    private JwtTokenProvider jwtTokenProvider;
    private Member testMember;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "key", secretKey);
        jwtTokenProvider.init();

        testMember = new Member(1L, "Test User", "test@example.com", Role.USER, "password");
    }

    @Nested
    class CreateTokenTest {

        @Test
        @DisplayName("회원 정보로 토큰을 생성할 수 있다")
        void createToken() {
            // when
            String token = jwtTokenProvider.createToken(testMember);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            // 토큰 검증
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);

            assertThat(claims.getPayload().getSubject()).isEqualTo(testMember.getId().toString());
            assertThat(claims.getPayload().get("name", String.class)).isEqualTo(testMember.getName());
            assertThat(claims.getPayload().get("role", String.class)).isEqualTo(testMember.getRole().toString());
        }
    }

    @Nested
    class ExtractIdTest {

        @Test
        @DisplayName("토큰에서 회원 ID를 추출할 수 있다")
        void extractId() {
            // given
            String token = jwtTokenProvider.createToken(testMember);

            // when
            Long extractedId = jwtTokenProvider.extractId(token);

            // then
            assertThat(extractedId).isEqualTo(testMember.getId());
        }

        @Test
        @DisplayName("유효하지 않은 토큰에서 ID를 추출하려고 하면 예외가 발생한다")
        void extractIdFromInvalidToken() {
            // given
            String invalidToken = "invalid.token.string";

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.extractId(invalidToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("유효하지 않은 토큰");
        }
    }

    @Nested
    class ExtractRoleTest {

        @Test
        @DisplayName("토큰에서 회원 역할을 추출할 수 있다")
        void extractRole() {
            // given
            String token = jwtTokenProvider.createToken(testMember);

            // when
            Role extractedRole = jwtTokenProvider.extractRole(token);

            // then
            assertThat(extractedRole).isEqualTo(testMember.getRole());
        }

        @Test
        @DisplayName("유효하지 않은 토큰에서 역할을 추출하려고 하면 예외가 발생한다")
        void extractRoleFromInvalidToken() {
            // given
            String invalidToken = "invalid.token.string";

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.extractRole(invalidToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("유효하지 않은 토큰");
        }
    }

    @Nested
    class ExtractTokenFromCookieTest {

        @Test
        @DisplayName("쿠키에서 토큰을 추출할 수 있다")
        void extractTokenFromCookie() {
            // given
            String tokenValue = "sample-token-value";
            Cookie tokenCookie = new Cookie("token", tokenValue);
            Cookie[] cookies = new Cookie[]{tokenCookie};

            // when
            String extractedToken = jwtTokenProvider.extractTokenFromCookie(cookies);

            // then
            assertThat(extractedToken).isEqualTo(tokenValue);
        }

        @Test
        @DisplayName("쿠키가 null이면 예외가 발생한다")
        void extractTokenFromNullCookies() {
            // given
            Cookie[] cookies = null;

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.extractTokenFromCookie(cookies))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("쿠키가 존재하지 않습니다");
        }

        @Test
        @DisplayName("토큰 쿠키가 없으면 예외가 발생한다")
        void extractTokenFromCookiesWithoutTokenCookie() {
            // given
            Cookie otherCookie = new Cookie("other", "value");
            Cookie[] cookies = new Cookie[]{otherCookie};

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.extractTokenFromCookie(cookies))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("접근 권한이 없습니다");
        }
    }

    @Nested
    class InitTest {

        @Test
        @DisplayName("init 메서드가 secretKey를 초기화한다")
        void init() {
            // given
            JwtTokenProvider provider = new JwtTokenProvider();
            ReflectionTestUtils.setField(provider, "key", secretKey);

            // when & then
            assertDoesNotThrow(() -> provider.init());
        }
    }
}
