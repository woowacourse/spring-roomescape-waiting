package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberName;
import roomescape.exception.member.NotFoundMemberException;
import roomescape.service.member.MemberService;
import roomescape.service.member.dto.MemberListResponse;

class MemberServiceTest extends ServiceTest {
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

    @Nested
    @DisplayName("id로 사용자 조회")
    class FindById {
        @Test
        void id로_사용자를_조회할_수_있다() {
            Member member = memberService.findById(1L);

            assertThat(member.getName())
                    .isEqualTo(new MemberName("사용자"));
        }

        @Test
        void 존재하지_않는_id로_사용자_조회_시_예외가_발생한다() {
            assertThatThrownBy(() -> memberService.findById(3L))
                    .isInstanceOf(NotFoundMemberException.class);
        }
    }
}
