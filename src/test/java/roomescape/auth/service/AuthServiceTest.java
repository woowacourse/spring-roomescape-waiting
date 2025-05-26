package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void 로그인을_할_수_있다() {

        // given
        Member member = MemberFixture.create(MemberRole.USER);
        LoginRequest request = new LoginRequest(member.getEmail(), member.getPassword());

        // when
        AuthorizationPrincipal authorizationPrincipal = authService.createMemberPrincipal(member, request);

        // then
        assertThat(authorizationPrincipal).isNotNull();
    }
}
