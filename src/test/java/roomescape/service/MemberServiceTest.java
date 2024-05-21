package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.service.member.MemberService;
import roomescape.service.member.dto.MemberListResponse;

public class MemberServiceTest extends ServiceTest {
    @Autowired
    private MemberService memberService;

    @Nested
    @DisplayName("사용자 목록 조회")
    class FindAllMember {
        @Test
        void 사용자_목록을_조회할_수_있다() {
            MemberListResponse response = memberService.findAllMember();
            assertThat(response.getMembers().size())
                    .isEqualTo(2);
        }
    }
}
