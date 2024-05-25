package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.getMemberClover;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.util.ServiceTest;

@DisplayName("사용자 로직 테스트")
class MemberServiceTest extends ServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @DisplayName("식별자로 사용자 조회에 성공한다.")
    @Test
    void findById() {
        //given
        Member memberClover = getMemberClover();
        Member member = memberRepository.save(memberClover);

        //when
        Member foundMember = memberService.findById(member.getId());

        //then
        assertThat(member).isEqualTo(foundMember);
    }
}
