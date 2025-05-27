package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.service.MemberService;

@SpringBootTest
class AuthServiceFacadeTest {

    @Autowired
    private AuthServiceFacade authServiceFacade;

    @MockitoBean
    private MemberService memberService;

    @Test
    void 로그인을_할_수_있다() {

        // given
        Member member = MemberFixture.create(MemberRole.USER);
        LoginRequest loginRequest = new LoginRequest(member.getEmail(), member.getPassword());

        when(memberService.findByEmailAndPassword(member.getEmail(), member.getPassword()))
            .thenReturn(Optional.of(member));

        // when
        AuthorizationPrincipal principal = authServiceFacade.login(loginRequest);

        // then
        assertThat(principal).isNotNull();
    }

    @Test
    void 멤버가_존재하는지_확인할_수_있다() {

        // given
        Member member = MemberFixture.create(MemberRole.USER);
        when(memberService.existsByName(member.getName())).thenReturn(true);

        // when & then
        assertThatCode(
            () -> authServiceFacade.validateMemberExistence(new MemberPrincipal(member.getName()))
        ).doesNotThrowAnyException();
    }
}
