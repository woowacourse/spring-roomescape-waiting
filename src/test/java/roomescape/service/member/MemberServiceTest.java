package roomescape.service.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.Fixture;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.service.ServiceTestBase;
import roomescape.service.member.dto.MemberResponse;

class MemberServiceTest extends ServiceTestBase {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @DisplayName("존재하는 모든 사용자를 조회한다.")
    @Test
    void findAll() {
        // given
        memberRepository.save(Fixture.member);
        memberRepository.save(Fixture.member2);
        memberRepository.save(Fixture.member3);

        // when
        List<MemberResponse> memberResponses = memberService.findAll();

        // then
        assertThat(memberResponses).hasSize(3);
    }

    @DisplayName("id로 사용자를 조회한다.")
    @Test
    void findById() {
        // given
        Member member = memberRepository.save(Fixture.member);

        // when
        Member result = memberService.findById(member.getId());

        // then
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
    }
}
