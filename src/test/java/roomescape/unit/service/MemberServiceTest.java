package roomescape.unit.service;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.dto.response.MemberResponse;
import roomescape.infrastructure.MemberRepository;
import roomescape.service.MemberService;
import roomescape.unit.fake.FakeMemberRepository;

class MemberServiceTest {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public MemberServiceTest() {
        this.memberRepository = new FakeMemberRepository();
        this.memberService = new MemberService(memberRepository);
    }

    @Test
    void 모든_회원을_조회한다() {
        // given
        Member member1 = new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER);
        Member member2 = new Member(null, "name2", "email2@domain.com", "password2", Role.MEMBER);
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<MemberResponse> allMembers = memberService.findAllMembers();
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(allMembers).hasSize(2);
        soft.assertThat(allMembers.getFirst().email()).isEqualTo("email1@domain.com");
        soft.assertThat(allMembers.get(1).email()).isEqualTo("email2@domain.com");
        soft.assertAll();
    }
}