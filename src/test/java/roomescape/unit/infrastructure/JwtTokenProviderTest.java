package roomescape.unit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.Role;
import roomescape.exception.ExpiredTokenException;
import roomescape.exception.InvalidTokenException;
import roomescape.infrastructure.JwtTokenProvider;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider = new JwtTokenProvider(
            "testtesttesttesttestkeykeykeykeykeykeykeykeykey",
            10000000000L
    );
    ;

    @Test
    void 원하는_페이로드로_토큰_생성() {
        // when
        String token = tokenProvider.createToken("payload", Role.MEMBER);
        // then
        String subject = tokenProvider.extractSubject(token);
        assertThat(subject).isEqualTo("payload");
    }

    @Test
    void 토큰_만료됐을_경우_예외가_발생한다() {
        // given
        tokenProvider = new JwtTokenProvider(
                "testtesttesttesttestkeykeykeykeykeykeykeykeykey",
                0L
        );
        String token = tokenProvider.createToken("payload", Role.MEMBER);
        // when & then
        assertThatThrownBy(() -> tokenProvider.extractSubject(token))
                .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    void 잘못된_토큰일_경우_예외가_발생한다() {
        // given
        String token = tokenProvider.createToken("payload", Role.MEMBER);
        // when & then
        assertThatThrownBy(() -> tokenProvider.extractSubject(token + "thisIsFakeTokenHahaha"))
                .isInstanceOf(InvalidTokenException.class);
    }
}