package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Member;
import roomescape.service.dto.response.MemberResponse;

public class MemberServiceTest extends ServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    void 모든_멤버를_조회한다() {
        List<MemberResponse> members = memberService.findAllMember();

        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    void 아이디로_멤버를_조회한다() {
        Member member = memberService.findById(1L);

        assertThat(member.getName()).isEqualTo("어드민");
    }
}
