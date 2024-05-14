package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.exception.ExceptionType.EMPTY_DATE;
import static roomescape.exception.ExceptionType.EMPTY_MEMBER;
import static roomescape.exception.ExceptionType.EMPTY_THEME;
import static roomescape.exception.ExceptionType.EMPTY_TIME;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.Fixture;
import roomescape.exception.RoomescapeException;

class ReservationTest {

    private static final LocalDate DEFAULT_DATE = LocalDate.now();
    private static final ReservationTime DEFAULT_TIME = new ReservationTime(1L, LocalTime.now());
    private static final Theme DEFAULT_THEME = new Theme(1L, "이름", "설명", "썸네일");

    @DisplayName("생성 테스트")
    @Test
    void constructTest() {
        assertAll(
                () -> assertThatThrownBy(() -> new Reservation(DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, null))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_MEMBER.getMessage()),

                () -> assertThatThrownBy(
                        () -> new Reservation(null, DEFAULT_TIME, DEFAULT_THEME, Fixture.defaultLoginuser))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_DATE.getMessage()),

                () -> assertThatThrownBy(
                        () -> new Reservation(DEFAULT_DATE, null, DEFAULT_THEME, Fixture.defaultLoginuser))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_TIME.getMessage()),

                () -> assertThatThrownBy(
                        () -> new Reservation(DEFAULT_DATE, DEFAULT_TIME, null, Fixture.defaultLoginuser))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_THEME.getMessage())
        );

    }

    @Test
    @DisplayName("날짜를 기준으로 비교를 잘 하는지 확인.")
    void compareTo() {
        Reservation first = new Reservation(1L, LocalDate.of(1999, 12, 1), new ReservationTime(
                LocalTime.of(16, 30)), DEFAULT_THEME, Fixture.defaultLoginuser);
        Reservation second = new Reservation(2L, LocalDate.of(1998, 1, 8), new ReservationTime(
                LocalTime.of(16, 30)), DEFAULT_THEME, Fixture.defaultLoginuser);
        int compareTo = first.compareTo(second);
        Assertions.assertThat(compareTo)
                .isGreaterThan(0);
    }

    @DisplayName("동일한 테마와 시간을 가지는 예약은 같은 예약으로 판단한다.")
    @Test
    void isSameReservationTest() {
        LocalDate date = LocalDate.now();
        Theme theme = new Theme(1L, "name", "description", "thumbnail");
        ReservationTime time = new ReservationTime(1L, LocalTime.now());

        Reservation savedReservation = new Reservation(1L, date, time, theme, Fixture.defaultLoginuser);
        Reservation nonSavedReservation = new Reservation(date, time, theme, Fixture.defaultLoginuser);

        assertThat(savedReservation.isSameReservation(nonSavedReservation))
                .isTrue();
    }

    @DisplayName("기간에 포함되는 예약인지 확인할 수 있다.")
    @Test
    void isBetweenDurationTest() {
        Duration beforeDuration = new Duration(
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 4, 20)
        );
        Duration containsDuration = new Duration(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 30)
        );
        Duration afterDuration = new Duration(
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 20)
        );
        Reservation reservation = new Reservation(
                LocalDate.of(2024, 5, 5),
                new ReservationTime(LocalTime.of(16, 30)),
                new Theme("name", "description", "thumbnail"),
                Fixture.defaultLoginuser
        );

        assertAll(
                () -> assertThat(reservation.isBetween(beforeDuration))
                        .isFalse(),
                () -> assertThat(reservation.isBetween(containsDuration))
                        .isTrue(),
                () -> assertThat(reservation.isBetween(afterDuration))
                        .isFalse()
        );
    }
}
