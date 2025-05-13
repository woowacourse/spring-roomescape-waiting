package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @DisplayName("두 예약이 서로 중복된 예약인지 판단한다.")
    @Test
    void isDuplicateReservation() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = ReservationTime.parse("08:30");
        ReservationTheme theme = new ReservationTheme(1L, "제목", "설명", "썸네일");
        Reservation reservation = new Reservation("제프리", date, time, theme);
        Reservation duplicated = new Reservation("플린트", date, time, theme);

        // when
        boolean isDuplicated = reservation.isDuplicateReservation(duplicated);

        // then
        assertThat(isDuplicated).isTrue();
    }

    @DisplayName("날짜가 다르면 중복이 아니다.")
    @Test
    void isNotDuplicate_whenDateIsDifferent() {
        //given
        LocalDate date1 = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);
        ReservationTime time = ReservationTime.parse("08:30");
        ReservationTheme theme = new ReservationTheme(1L, "제목", "설명", "썸네일");
        Reservation reservation1 = new Reservation("제프리", date1, time, theme);
        Reservation reservation2 = new Reservation("플린트", date2, time, theme);

        //when
        boolean isDuplicated = reservation1.isDuplicateReservation(reservation2);

        //then
        assertThat(isDuplicated).isFalse();
    }

    @DisplayName("시간이 다르면 중복이 아니다.")
    @Test
    void isNotDuplicate_whenTimeIsDifferent() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTheme theme = new ReservationTheme(1L, "제목", "설명", "썸네일");
        ReservationTime time1 = ReservationTime.parse("08:30");
        ReservationTime time2 = ReservationTime.parse("09:00");
        Reservation reservation1 = new Reservation("제프리", date, time1, theme);
        Reservation reservation2 = new Reservation("플린트", date, time2, theme);

        //when
        boolean isDuplicated = reservation1.isDuplicateReservation(reservation2);

        //then
        assertThat(isDuplicated).isFalse();
    }

}
