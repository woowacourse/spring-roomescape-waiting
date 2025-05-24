package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.business.domain.Member;
import roomescape.persistence.repository.MemberRepository;
import roomescape.presentation.dto.MemberResponse;

@DataJpaTest
class MemberServiceTest {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceTest(final MemberRepository memberRepository) {
        this.memberService = new MemberService(memberRepository);
        this.memberRepository = memberRepository;
    }

    @Test
    @DisplayName("모든 사용자를 조회한다")
    void findAll() {
        // given
        final Member member1 = new Member("후유", "ADMIN", "fuyu@test.com", "1234");
        memberRepository.save(member1);
        final Member member2 = new Member("브라운", "USER", "braun@test.com", "1234");
        memberRepository.save(member2);

        // when
        final List<MemberResponse> memberResponses = memberService.findAll();

        // then
        assertThat(memberResponses).hasSize(2);
    }
}
