package roomescape.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.user.Member;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TokenProviderTest {
    @Autowired
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("토큰을 생성하고,추출한다")
    void some() {
        final var token = tokenProvider.generateToken(
                Member.fromMember(3L, "조이썬", "joyson5582@gmail.com", "password1234"));
        final var decodeInfo = tokenProvider.decodeToken(token);
        assertThat(decodeInfo.getId()).isEqualTo(3);
    }
}
