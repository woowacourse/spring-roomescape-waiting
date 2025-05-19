package roomescape.application.member.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.member.query.dto.MemberResult;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;

class MemberQueryServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    private MemberQueryService memberQueryService;

    @BeforeEach
    void setUp() {
        memberQueryService = new MemberQueryService(memberRepository);
    }

    @Test
    void 모든_회원_조회가_가능하다() {
        // given
        memberRepository.save(new Member("벨로", new Email("test1@email.com"), "1234", MemberRole.NORMAL));
        memberRepository.save(new Member("서프", new Email("test2@email.com"), "1234", MemberRole.NORMAL));

        // when
        List<MemberResult> results = memberQueryService.findAll();

        // then
        assertThat(results)
                .isEqualTo(List.of(
                        new MemberResult(1L, "벨로"),
                        new MemberResult(2L, "서프")
                ));
    }
}
