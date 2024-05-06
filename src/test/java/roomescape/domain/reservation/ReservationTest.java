package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;

class ReservationTest {

    @Test
    @DisplayName("예약 시간이 예약 생성 시간보다 이전이면 예외가 발생한다.")
    void invalidReserveTimeTest() {
        LocalDateTime reservationTime = LocalDateTime.of(2024, 1, 1, 12, 0, 59);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 1, 0);
        LocalDate date = reservationTime.toLocalDate();
        ReservationTime time = new ReservationTime(reservationTime.toLocalTime());
        Theme theme = new Theme("테마명", "설명", "url");
        Member member = MemberFixture.createMember("아루");

        assertThatCode(() -> new Reservation(member, date, time, theme, createdAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 시간보다 과거로 예약할 수 없습니다.");
    }
}
