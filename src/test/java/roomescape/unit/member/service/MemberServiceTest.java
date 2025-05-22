package roomescape.unit.member.service;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.member.service.MemberService;
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
        Member member1 = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        Member member2 = Member.builder()
                .name("name2")
                .email("email2@domain.com")
                .password("password2")
                .role(Role.MEMBER).build();
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