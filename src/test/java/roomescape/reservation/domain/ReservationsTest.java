package roomescape.reservation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;

class ReservationsTest {

    private final String name = "송송";
    private final String anotherName = "다른사람";
    private ReservationDate date = ReservationDateFixture.activeOneWeekLater();
    private ReservationTime time = ReservationTimeFixture.activeTime15();
    private Theme theme = ThemeFixture.theme("테마1");

    @Test
    @DisplayName("슬롯 예약 목록에 요청자의 예약이 있으면 예외가 발생한다.")
    void validateNotAlreadyBookedBy_fail_whenRequesterAlreadyBooked() { // 특정 슬롯의 예약+대기목록
        // given
        Reservations reservations = new Reservations(List.of(ReservationFixture.reservation(name, date, time, theme)));

        // when & then
        Assertions.assertThatThrownBy(() -> reservations.validateNotAlreadyBookedBy(name))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_BOOKED.getMessage());
    }

    @Test
    @DisplayName("슬롯 예약 목록에 요청자의 예약이 없으면 예외가 발생하지 않는다.")
    void validateNotAlreadyBookedBy_success_whenRequesterHasNoReservation() {
        // given
        Reservations reservations = new Reservations(
                List.of(ReservationFixture.reservation(anotherName, date, time, theme))
        );

        // when & then
        Assertions.assertThatCode(() -> reservations.validateNotAlreadyBookedBy(name))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("슬롯 예약 목록에 다른 사람의 확정 예약이 있으면 true를 반환한다.")
    void hasReservedByOthers_returnTrue_whenAnotherUserReserved() {
        // given
        Reservations reservations = new Reservations(List.of(ReservationFixture.reservation(anotherName, date, time, theme)));

        // when
        boolean result = reservations.hasReservedByOthers(name);

        // then
        Assertions.assertThat(result)
                .isTrue();
    }

    @Test
    @DisplayName("슬롯 예약 목록에 다른 사람의 확정 예약이 없으면 false를 반환한다.")
    void hasReservedByOthers_returnFalse_whenNoOtherUserReserved() {
        // given
        Reservations reservations = new Reservations(List.of());

        // when
        boolean result = reservations.hasReservedByOthers(name);

        // then
        Assertions.assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("빈 슬롯에 예약하면, 예약 상태로 목록에 추가된다.")
    void reserve_adds_new_reservation() {
        // given
        Reservations reservations = new Reservations(new ArrayList<>());
        Reservation newReservation = ReservationFixture.reservation(name, date, time, theme);
        ReservationSlot slot = ReservationSlot.of(date, time, theme);

        // when
        reservations.reserve(name, slot, LocalDateTime.now());

        // then
        Assertions.assertThat(reservations.values())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("reservedAt")
                .contains(newReservation);
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 다른 사람이 예약하면, 대기 상태로 목록에 추가된다.")
    void reserve_adds_waiting_reservation() {
        // given
        Reservations reservations = new Reservations(
                List.of(ReservationFixture.reservation(name, date, time, theme))
        );
        ReservationSlot slot = ReservationSlot.of(date, time, theme);
        Reservation waitReservation = ReservationFixture.waitReservation(anotherName, date, time, theme);

        // when
        reservations.reserve(anotherName, slot, LocalDateTime.now());

        // then
        Assertions.assertThat(reservations.values())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("reservedAt")
                .contains(waitReservation);
    }

}
