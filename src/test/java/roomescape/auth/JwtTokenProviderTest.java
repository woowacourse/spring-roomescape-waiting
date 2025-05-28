package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Duration;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.domain.MemberRole;
import roomescape.exception.UnauthorizedException;
import roomescape.service.result.MemberResult;

class JwtTokenProviderTest {

    private static final String secretKey = "wpvmflgltmxkaoxmdhflajdrnahfn20250514";
    private final int validityInMilliseconds = (int) Duration.ofHours(1).toMillis();
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", validityInMilliseconds);
    }

    @Test
    void 로그인_결과를_통해_토큰을_생성할_수_있다() {
        //given
        MemberResult memberResult = new MemberResult(1L, "Eve", MemberRole.USER, "eve@example.com");

        //when
        String token = jwtTokenProvider.createToken(memberResult);

        //then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void 토큰으로부터_id를_추출할_수_있다() {
        //given
        Long memberId = 1L;
        String token = generateValidToken(memberId);

        //when
        Long id = jwtTokenProvider.extractIdFromToken(token);

        //then
        assertThat(id).isEqualTo(memberId);
    }

    @Test
    void 토큰이_만료됐을_경우_예외를_던진다() {
        //given
        Long memberId = 1L;
        String token = generateExpiredToken(memberId);

        //when & then
        assertThatThrownBy(() -> jwtTokenProvider.extractIdFromToken(token))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 토큰의_클레임에서_아이디를_추출할_수_있다() {
        //given
        Long memberId = 1L;
        String token = generateValidToken(memberId);

        //when
        Long id = jwtTokenProvider.extractIdFromToken(token);

        //then
        assertThat(id).isEqualTo(memberId);
    }

    private String generateValidToken(Long id) {
        Date expirationDate = new Date(System.currentTimeMillis() + validityInMilliseconds);

        return Jwts.builder()
                .subject(id.toString())
                .expiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    private String generateExpiredToken(Long id) {
        Date expirationDate = new Date(System.currentTimeMillis() - 1000);

        return Jwts.builder()
                .subject(id.toString())
                .expiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }
}
