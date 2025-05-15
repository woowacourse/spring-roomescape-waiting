package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @DisplayName("Date 이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationDateTest() {
        // given
        Member member = new Member(1L, "가이온", "user@gmail.com", "wooteco7", Role.USER);
        ReservationTime time = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "우테코", "방탈출", "https://");

        // when & then
        assertThatThrownBy(() -> new Reservation(1L, member, null, time, theme, ReservationStatus.RESERVED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("ReservationTime이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationTimeTest() {
        // given
        Member member = new Member(1L, "가이온", "user@gmail.com", "wooteco7", Role.USER);
        LocalDate date = LocalDate.now();
        Theme theme = new Theme(1L, "우테코", "방탈출", "https://");

        // when & then
        assertThatThrownBy(() -> new Reservation(1L, member, date, null, theme, ReservationStatus.RESERVED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("멤버가 존재하지 않는 경우 생성할 수 없다.")
    @Test
    void invalidReservationNameTest() {
        // given
        LocalDate date = LocalDate.now();
        ReservationTime time = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "우테코", "방탈출", "https://");

        // when & then
        assertThatThrownBy(() -> new Reservation(1L, null, date, time, theme, ReservationStatus.RESERVED))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
