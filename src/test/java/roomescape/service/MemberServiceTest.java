package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.MEMBER2;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;
import roomescape.service.dto.response.MemberResponse;

class MemberServiceTest extends DBTest {

    @DisplayName("모든 멤버를 조회한다.")
    @Test
    void findAll() {
        // given
        memberRepository.save(MEMBER1);
        memberRepository.save(MEMBER2);

        // when
        List<MemberResponse> members = memberService.findAll().responses();

        // then
        assertThat(members).hasSize(2);
        assertThat(members).extracting("name").containsExactly(MEMBER1.getName(), MEMBER2.getName());
    }
}
