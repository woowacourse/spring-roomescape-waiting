package roomescape.auth.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.fixture.MemberDbFixture;
import roomescape.member.domain.Member;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private MemberDbFixture memberDbFixture;

    @Test
    void 토큰을_생성하고_memberId를_파싱한다() {
        Member member = memberDbFixture.유저1_생성();

        String token = jwtProvider.issue(member);

        Long memberId = jwtProvider.getMemberId(token);
        assertThat(memberId).isEqualTo(member.getId());
    }

    @Test
    void 토큰을_생성하고_role을_파싱한다() {
        Member member = memberDbFixture.유저1_생성();

        String token = jwtProvider.issue(member);

        String role = jwtProvider.getRole(token).name();
        assertThat(role).isEqualTo(member.getRole().name());
    }


}
