package roomescape.service.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.service.member.dto.MemberResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/truncate.sql")
class MemberServiceTest {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @DisplayName("존재하는 모든 사용자를 조회한다.")
    @Test
    void findAll() {
        // given
        memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.MEMBER));
        memberRepository.save(new Member("lini2", "lini2@email.com", "lini123", Role.MEMBER));
        memberRepository.save(new Member("lini3", "lini3@email.com", "lini123", Role.MEMBER));

        // when
        List<MemberResponse> memberResponses = memberService.findAll();

        // then
        assertThat(memberResponses).hasSize(3);
    }

    @DisplayName("id로 사용자를 조회한다.")
    @Test
    void findById() {
        // given
        Member member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.MEMBER));

        // when
        Member result = memberService.findById(member.getId());

        // then
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
    }
}
