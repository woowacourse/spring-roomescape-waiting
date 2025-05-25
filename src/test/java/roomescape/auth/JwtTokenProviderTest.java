package roomescape.auth;

import io.jsonwebtoken.Claims;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.domain.dto.TokenInfoDto;
import roomescape.user.domain.Role;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtTokenProvider.class})
@TestPropertySource(properties = {
        "security.jwt.token.secret-key=testsecretkeytestsecretkeytestsecretkey",
        "security.jwt.token.expire-length=3600000"
})
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("토큰 생성 기능")
    class createToken {

        private static final long USER_ID_FIELD = 1L;
        private static final Role ROLE_FIELD = Role.ROLE_MEMBER;

        private TokenInfoDto tokenInfoDto;

        @BeforeEach
        public void beforeEach() {
            tokenInfoDto = new TokenInfoDto(USER_ID_FIELD, ROLE_FIELD);
        }

        @DisplayName("토큰이 잘 생성되는 지 확인")
        @Test
        void createToken_andGetClaims() {
            // given

            // when
            String token = jwtTokenProvider.createToken(tokenInfoDto);
            Claims claims = jwtTokenProvider.getClaims(token);

            // then
            long actualId = Long.parseLong(claims.getSubject());
            Role actualRole = Role.findByName((String) claims.get("role"));

            SoftAssertions.assertSoftly(
                    s -> {
                        s.assertThat(actualId).isEqualTo(USER_ID_FIELD);
                        s.assertThat(actualRole).isEqualTo(ROLE_FIELD);
                    }
            );
        }
    }
}
