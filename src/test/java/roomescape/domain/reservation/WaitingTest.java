package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.infrastructure.error.exception.WaitingException;

class WaitingTest {

    private final Member member = new Member(1L, "벨로", new Email("test@email.com"), "password", MemberRole.NORMAL);
    private final Member other = new Member(2L, "벨로아님", new Email("other@email.com"), "password", MemberRole.NORMAL);
    private final Member admin = new Member(3L, "관리자", new Email("admin@email.com"), "password", MemberRole.ADMIN);
    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(13, 0));
    private final Theme theme = new Theme(1L, "테마", "설명", "이미지");

    @Test
    void 관리자인_경우_대기가_승인될_수_있다() {
        // given
        Waiting waiting = new Waiting(member, LocalDate.now().plusDays(1), time, theme);

        // when
        boolean canBeApproved = waiting.canBeApprovedBy(admin);

        // then
        assertThat(canBeApproved)
                .isTrue();
    }

    @Test
    void 관리자가_아니라면_대기가_승인될_수_없다() {
        // given
        Waiting waiting = new Waiting(member, LocalDate.now().plusDays(1), time, theme);

        // when
        boolean canBeApproved = waiting.canBeApprovedBy(member);

        // then
        assertThat(canBeApproved)
                .isTrue();
    }

    @Test
    void 본인이_요청한_대기인_경우_취소될_수_있다() {
        // given
        Waiting waiting = new Waiting(member, LocalDate.now().plusDays(1), time, theme);

        // when
        boolean canBeCanceled = waiting.canBeCanceledBy(member);

        // then
        assertThat(canBeCanceled)
                .isTrue();
    }

    @Test
    void 본인이_요청한_대기가_아닌_경우_취소될_수_없다() {
        // given
        Waiting waiting = new Waiting(member, LocalDate.now().plusDays(1), time, theme);

        // when
        boolean canBeCanceled = waiting.canBeCanceledBy(other);

        // then
        assertThat(canBeCanceled)
                .isFalse();
    }

    @Test
    void 대기_검증_시간이_이미_지났다면_예외가_발생한다() {
        // given
        Waiting waiting = new Waiting(member, LocalDate.now().minusDays(1), time, theme);

        // when & then
        assertThatCode(() -> waiting.validateWaitable(LocalDateTime.now()))
                .isInstanceOf(WaitingException.class)
                .hasMessage("예약일이 지나 대기 신청을 할 수 없습니다.");
    }

    @Test
    void 대기_검증_시간이_미래라면_예외가_발생하지_않는다() {
        // given
        Waiting waiting = new Waiting(member, LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatCode(() -> waiting.validateWaitable(LocalDateTime.of(2025, 5, 21, 13, 0)))
                .doesNotThrowAnyException();
    }
}
