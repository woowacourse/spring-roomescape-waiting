package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberRole;
import roomescape.exception.login.ExpiredTokenException;
import roomescape.exception.login.InvalidTokenException;
import roomescape.service.login.JwtTokenProvider;

class JwtTokenProviderTest extends ServiceTest {
    private static final String ROLE_CLAIM_NAME = "role";

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    @Nested
    @DisplayName("토큰에서 사용자 정보 추출")
    class GetMemberByToken {
        private MemberEmail email;
        private MemberRole role;
        private String token;
        private Date now;
        private Date expiredAt;

        @BeforeEach
        void setUp() {
            email = new MemberEmail("user@gmail.com");
            role = MemberRole.USER;
            token = jwtTokenProvider.createToken(email, role);
            now = Date.from(clock.instant());
            expiredAt = new Date(now.getTime() + validityInMilliseconds);
        }

        @Test
        void 토큰으로_사용자_이메일을_가져올_수_있다() {
            assertThat(jwtTokenProvider.getMemberEmail(token)).isEqualTo(email);
        }

        @Test
        void 토큰으로_사용자_역할을_가져올_수_있다() {
            assertThat(jwtTokenProvider.getMemberRole(token)).isEqualTo(role);
        }

        @Test
        void 토큰에_존재하지_않는_역할값이_들어있으면_예외가_발생한다() {
            String wrongRole = "WRONG_USER";

            String invalidToken = Jwts.builder()
                    .setSubject(email.getAddress())
                    .claim(ROLE_CLAIM_NAME, wrongRole)
                    .setExpiration(expiredAt)
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();

            assertThatThrownBy(() -> jwtTokenProvider.getMemberRole(invalidToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        void 만료된_토큰으로_사용자_정보를_가져올_시_예외가_발생한다() {
            Date alreadyExpiredAt = new Date(now.getTime() - validityInMilliseconds);

            String expiredToken = Jwts.builder()
                    .setSubject(email.getAddress())
                    .claim(ROLE_CLAIM_NAME, role.name())
                    .setExpiration(alreadyExpiredAt)
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();

            assertThatThrownBy(() -> jwtTokenProvider.getMemberRole(expiredToken))
                    .isInstanceOf(ExpiredTokenException.class);
        }

        @Test
        void 잘못된_시크릿키로_만들어진_토큰으로_사용자_정보를_가져올_시_예외가_발생한다() {
            String invalidSecretKey = "wrongSecretKey";

            String invalidToken = Jwts.builder()
                    .setSubject(email.getAddress())
                    .claim(ROLE_CLAIM_NAME, role.name())
                    .setExpiration(expiredAt)
                    .signWith(SignatureAlgorithm.HS256, invalidSecretKey)
                    .compact();

            assertThatThrownBy(() -> jwtTokenProvider.getMemberRole(invalidToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void 빈_토큰으로_사용자_정보를_가져올_시_예외가_발생한다(String emptyToken) {
            assertThatThrownBy(() -> jwtTokenProvider.getMemberRole(emptyToken))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }
}
