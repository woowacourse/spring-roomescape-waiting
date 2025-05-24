package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTestBase;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.service.MemberService;
import roomescape.service.response.MemberResponse;

class MemberServiceTest extends ServiceTestBase {

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private MemberService memberService;

    @Test
    void 모든_멤버를_조회한다() {
        // given
        var member1 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var member2 = memberDbFixture.leehyeonsu4888_지메일_gustn111느낌표두개();

        // when
        var allMembers = memberService.findAllMembers();

        // then
        assertThat(allMembers).containsExactly(
                new MemberResponse(member1.getId(), member1.getName().name()),
                new MemberResponse(member2.getId(), member2.getName().name())
        );
    }
}
