package roomescape.unit.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.repository.MemberRepository;
import roomescape.service.MemberService;
import roomescape.service.response.MemberResponse;
import roomescape.unit.fake.FakeMemberRepository;

class MemberServiceTest {

    private final MemberRepository memberRepository = new FakeMemberRepository();
    private final MemberService memberService = new MemberService(memberRepository);

    @Test
    void 모든_멤버를_조회한다() {
        // given
        Member member1 = memberRepository.save(
                new MemberEmail("leehyeonsu4848@gmail.com"),
                new MemberName("짭한스1"),
                new MemberEncodedPassword("gdgd"),
                MemberRole.MEMBER
        );

        Member member2 = memberRepository.save(
                new MemberEmail("leehyeonsu488@gmail.com"),
                new MemberName("짭한스2"),
                new MemberEncodedPassword("gdgdsad"),
                MemberRole.MEMBER
        );

        // when
        List<MemberResponse> allMembers = memberService.findAllMembers();

        // then
        assertThat(allMembers).containsExactly(
                new MemberResponse(member1.getId(), member1.getName().name()),
                new MemberResponse(member2.getId(), member2.getName().name())
        );
    }
}
