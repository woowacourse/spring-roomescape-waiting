package roomescape.auth.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.member.model.MemberRole;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @DisplayName("사용자 정보를 입력하면 인증 토큰을 생성한다.")
    @Test
    void createTokenTest() {
        // Given
        final Long memberId = 3L;
        final MemberRole role = MemberRole.USER;

        // When
        final String token = tokenProvider.createToken(memberId, role);

        // Then
        final long expirationDay = tokenProvider.getTokenClaims(token).getExpiration().getTime();
        final long today = new Date().getTime();
        final int expirationPeriod = (int) (expirationDay - today) / (24 * 60 * 60 * 1000) + 1;

        assertAll(
                () -> assertThat(Long.parseLong(tokenProvider.getTokenClaims(token).getSubject())).isEqualTo(memberId),
                () -> assertThat(tokenProvider.getTokenClaims(token).get("role")).isEqualTo(role.name()),
                () -> assertThat(expirationPeriod).isEqualTo(7)
        );
    }
}
