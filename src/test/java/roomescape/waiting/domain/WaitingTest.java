package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class WaitingTest {
    private final Member member = new Member(1L, "커찬", "kuchn@abc.com");
    private final LocalDate date = LocalDate.of(2050, 10, 10);
    private final ReservationTime time = new ReservationTime(LocalTime.of(9, 0));
    private final Theme theme = new Theme(
            "오리와 호랑이",
            "오리들과 호랑이들 사이에서 살아남기",
            "https://image.jpg");

    @DisplayName("자신이 예약한 방탈출에 대해 에약 대기 시, 예외를 던진다.")
    @Test
    void validateWaitingTest_whenDuplicate() {
        assertThatThrownBy(() -> new Waiting(new Reservation(member, date, time, theme), member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자신이 예약한 방탈출에 대해 예약 대기를 할 수 없습니다.");
    }
}
