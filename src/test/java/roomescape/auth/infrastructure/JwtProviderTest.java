package roomescape.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.config.JwtProperties;
import roomescape.auth.dto.MemberInfo;
import roomescape.auth.exception.UnAuthorizedException;
import roomescape.member.domain.MemberRole;

class JwtProviderTest {

    private static final String SECRET_KEY = "test-secret-key";
    private static final long VALIDITY_IN_MILLISECONDS = 900000L;
    private static final Long MEMBER_ID = 1L;
    private static final MemberRole MEMBER_ROLE = MemberRole.REGULAR;

    private JwtProvider jwtProvider;
    private MemberInfo memberInfo;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(SECRET_KEY);
        jwtProperties.setExpireLength(VALIDITY_IN_MILLISECONDS);

        jwtProvider = new JwtProvider(jwtProperties);
        memberInfo = new MemberInfo(MEMBER_ID, MEMBER_ROLE);
    }

    @Test
    @DisplayName("토큰을 생성하고 검증할 수 있다.")
    void createAndVerifyToken() {
        // when
        String token = jwtProvider.createToken(memberInfo);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(jwtProvider.isInvalidToken(token)).isFalse();
            softAssertions.assertThat(jwtProvider.getMemberId(token)).isEqualTo(MEMBER_ID);
//            softAssertions.assertThat(jwtProvider.getRole(token)).isEqualTo(MEMBER_ROLE);
        });
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다.")
    void verifyExpiredToken() {
        // given
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecretKey(SECRET_KEY);
        expiredProperties.setExpireLength(0L);
        JwtProvider expiredJwtProvider = new JwtProvider(expiredProperties);

        String token = expiredJwtProvider.createToken(memberInfo);

        // when & then
        assertThatThrownBy(() -> expiredJwtProvider.isInvalidToken(token))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("만료된 토큰입니다.");
    }

    @Test
    @DisplayName("잘못된 시크릿 키로 생성된 토큰은 검증에 실패한다.")
    void verifyTokenWithWrongSecretKey() {
        // given
        String token = jwtProvider.createToken(memberInfo);

        JwtProperties wrongProperties = new JwtProperties();
        wrongProperties.setSecretKey("wrong-secret-key");
        wrongProperties.setExpireLength(VALIDITY_IN_MILLISECONDS);
        JwtProvider wrongJwtProvider = new JwtProvider(wrongProperties);

        // when & then
        assertThatThrownBy(() -> wrongJwtProvider.isInvalidToken(token))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("잘못된 형식의 토큰은 검증에 실패한다.")
    void verifyInvalidToken() {
        // when & then
        assertThatThrownBy(() -> jwtProvider.isInvalidToken("invalid.token"))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("유효하지 않은 토큰입니다.");
    }
}
