package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.service.dto.response.MemberResponse;

class MemberServiceTest extends DBTest {

    @DisplayName("모든 멤버를 조회한다.")
    @Test
    void findAll() {
        // given
        Member savedMember1 = memberRepository.save(TestFixture.getMember1());
        Member savedMember2 = memberRepository.save(TestFixture.getMember2());

        // when
        List<MemberResponse> members = memberService.findAll().responses();

        // then
        assertThat(members).hasSize(2);
        assertThat(members).extracting("name").contains(savedMember1.getName(), savedMember2.getName());
    }
}
