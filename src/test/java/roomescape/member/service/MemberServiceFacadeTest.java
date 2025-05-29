package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.auth.dto.request.MemberCreationRequest;
import roomescape.auth.dto.response.MemberCreationUpResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.response.MemberNameResponse;
import roomescape.member.fixture.MemberFixture;

@SpringBootTest
class MemberServiceFacadeTest {

    @Autowired
    private MemberServiceFacade memberServiceFacade;

    @MockitoBean
    private MemberService memberService;

    @Test
    void 멤버를__추가할_수_있다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        MemberCreationRequest signUpRequest = new MemberCreationRequest(
            member.getName(),
            member.getEmail(),
            member.getPassword()
        );
        MemberCreationUpResponse expected = new MemberCreationUpResponse(member.getEmail(), true);
        when(memberService.create(signUpRequest))
            .thenReturn(expected);

        // when
        MemberCreationUpResponse actual = memberServiceFacade.create(signUpRequest);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 모든_멤버의_이름을_조회한다() {

        // given
        Member member1 = MemberFixture.createWithoutId(MemberRole.USER);
        Member member2 = MemberFixture.createWithoutId(MemberRole.USER);
        List<MemberNameResponse> expected = List.of(
            new MemberNameResponse(member1.getId(), member1.getName()),
            new MemberNameResponse(member2.getId(), member2.getName())
        );
        when(memberService.findNames())
            .thenReturn(expected);

        // when
        List<MemberNameResponse> actual = memberServiceFacade.findNames();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
