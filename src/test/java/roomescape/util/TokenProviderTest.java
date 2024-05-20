package roomescape.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.acceptance.fixture.MemberFixture;
import roomescape.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TokenProviderTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("토큰을 생성하고,추출한다")
    void some() {
        final var member = memberRepository.save(MemberFixture.getDomain());
        final var token = tokenProvider.generateToken(member);
        final var decodeInfo = tokenProvider.decodeToken(token);
        assertThat(decodeInfo.getId()).isEqualTo(member.getId());
    }
}
