package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

class WaitingTest {

    private LocalDate date = LocalDate.of(2025, 5, 1);
    private ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
    private Theme theme = new Theme(null, "공포", "진짜무서운거임", "이거 하면 오줌 지림");
    private Member member = new Member(null, "레오", "이메일", "비번", MemberRole.USER);

    @DisplayName("주어진 순번보다 이후 순번인지 여부를 반환할 수 있다")
    @Test
    void hasAfterPriority() {
        // given
        Waiting waiting = new Waiting(null, date, reservationTime, theme, member, 3);

        // when
        boolean result = waiting.hasAfterPriority(2);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("주어진 크기만큼 순번을 앞당길 수 있다")
    @Test
    void pullPriority() {
        // given
        Waiting waiting = new Waiting(null, date, reservationTime, theme, member, 3);

        // when
        waiting.pullPriority(1);

        // then
        assertThat(waiting.getPriority()).isEqualTo(2);
    }

}
