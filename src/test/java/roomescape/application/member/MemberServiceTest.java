package roomescape.application.member;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;

class MemberServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    EntityManager entityManager;
    @Autowired
    private MemberRepository memberRepository;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void 모든_회원_조회() {
        // given
        memberRepository.save(new Member("벨로", new Email("test1@email.com"), "1234", Role.NORMAL));
        memberRepository.save(new Member("서프", new Email("test2@email.com"), "1234", Role.NORMAL));

        // when
        List<MemberResult> results = memberService.findAll();

        // then
        assertThat(results)
                .isEqualTo(List.of(
                        new MemberResult(1L, "벨로"),
                        new MemberResult(2L, "서프")
                ));
    }
}
