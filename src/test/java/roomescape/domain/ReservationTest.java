package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.MEMBER2_USER;
import static roomescape.testFixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReservationTest {

    @DisplayName("일시가 같으면 중복된 예약이다")
    @Test
    void isDuplicated() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 1);
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.of(10, 0));
        Reservation reservation1 = Reservation.of(
                1L,
                MEMBER1_ADMIN,
                THEME_1,
                date,
                reservationTime,
                new Waiting(ReservationStatus.RESERVED)
        );

        // when
        Reservation reservation2 = Reservation.of(
                2L,
                MEMBER2_USER,
                THEME_1,
                date,
                reservationTime,
                new Waiting(ReservationStatus.RESERVED)
        );
        boolean duplicated = reservation2.isDuplicated(reservation1);

        // then
        assertThat(duplicated).isTrue();
    }

    @DisplayName("일시가 같지 않으면 중복된 예약이 아니다")
    @ParameterizedTest
    @CsvSource({
            "2025-02-01,10:00,2",
            "2025-01-01,11:00,3",
    })
    void isNotduplicated(String date, String time, Long timeId) {
        // given
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        ReservationTime time1 = ReservationTime.of(1L, LocalTime.of(10, 0));
        Reservation reservation1 = Reservation.of(
                1L,
                MEMBER1_ADMIN,
                THEME_1,
                date1,
                time1,
                new Waiting(ReservationStatus.RESERVED)
        );

        // when
        LocalDate date2 = LocalDate.parse(date);
        ReservationTime time2 = ReservationTime.of(timeId, LocalTime.parse(time));
        Reservation reservation2 = Reservation.of(
                2L,
                MEMBER2_USER,
                THEME_1,
                date2,
                time2,
                new Waiting(ReservationStatus.RESERVED)
        );
        boolean duplicated = reservation2.isDuplicated(reservation1);

        // then
        assertThat(duplicated).isFalse();
    }
}
