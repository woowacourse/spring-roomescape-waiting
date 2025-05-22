package roomescape.waiting;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservation.waiting.Waiting;
import roomescape.reservationtime.ReservationTime;
import roomescape.schedule.Schedule;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

public class WaitingTest {

    @Test
    void 순서를_1_감소시킬_수_있다() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
        Theme theme = new Theme("데블스플랜", "과연 우승자는", "abc");
        Member member = new Member("may@gmail.com", "1234", "메이", MemberRole.MEMBER);
        Schedule schedule = new Schedule(LocalDate.now(), reservationTime, theme);

        // when
        Long rank = 3L;
        Waiting waiting = new Waiting(schedule, member, rank);
        waiting.decrementRank();

        // then
        Assertions.assertThat(waiting.getRank()).isEqualTo(rank - 1);
    }
}
