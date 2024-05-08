package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import static roomescape.model.Role.MEMBER;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.jsonwebtoken.Claims;
import roomescape.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("주어진 사용자로 토큰을 생성한다.")
    @Test
    void should_create_token_when_given_user() {
        User user = new User(1L, "썬", MEMBER, "sun@email.com", "1234");

        String token = jwtTokenProvider.createToken(user);

        assertThat(token).isNotBlank();
    }

    @DisplayName("주어진 토큰으로 payload를 반환한다.")
    @Test
    void should_get_payload_when_given_token() {
        User user = new User(1L, "썬", MEMBER, "sun@email.com", "1234");
        String token = jwtTokenProvider.createToken(user);

        Claims claims = jwtTokenProvider.getPayload(token);

        String role = claims.get("role", String.class);
        String userId = claims.getSubject();
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(userId).isEqualTo("1");
            softAssertions.assertThat(role).isEqualTo("MEMBER");
        });
    }
}
