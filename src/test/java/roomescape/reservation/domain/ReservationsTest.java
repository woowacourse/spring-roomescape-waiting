package roomescape.reservation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.slot.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static roomescape.reservation.domain.ReservationStatus.WAITING;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;

class ReservationsTest {

    private final String name = "송송";
    private final String anotherName = "다른사람";
    private ReservationDate date = ReservationDateFixture.activeOneWeekLater();
    private ReservationTime time = ReservationTimeFixture.activeTime15();
    private Theme theme = ThemeFixture.theme("테마1");
    private ReservationSlot slot = ReservationSlot.of(date, time, theme);

    @Test
    @DisplayName("슬롯 예약 목록에 요청자의 예약이 있으면 예외가 발생한다.")
    void validateNotAlreadyBookedBy_fail_whenRequesterAlreadyBooked() { // 특정 슬롯의 예약+대기목록
        // given
        Reservations reservations = new Reservations(List.of(ReservationFixture.reservation(name, slot)));

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
                List.of(ReservationFixture.reservation(anotherName, slot))
        );

        // when & then
        Assertions.assertThatCode(() -> reservations.validateNotAlreadyBookedBy(name))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("슬롯 예약 목록에 다른 사람의 확정 예약이 있으면 true를 반환한다.")
    void hasReservedByOthers_returnTrue_whenAnotherUserReserved() {
        // given
        Reservations reservations = new Reservations(List.of(ReservationFixture.reservation(anotherName, slot)));

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
        Reservation newReservation = ReservationFixture.reservation(name, slot);

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
                List.of(ReservationFixture.reservation(name, slot))
        );
        Reservation waitReservation = ReservationFixture.waitReservation(anotherName, slot);

        // when
        reservations.reserve(anotherName, slot, LocalDateTime.now());

        // then
        Assertions.assertThat(reservations.values())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("reservedAt")
                .contains(waitReservation);
    }

    @Test
    @DisplayName("예약+대기 목록에서 승격자는 가장 빨리 예약요청을 보낸 자이다.")
    void findPromoteWaiting() {
        // given
        Reservation firstWait = Reservation.wait("test1", slot, LocalDateTime.now());
        Reservation secondWait = Reservation.wait("test1", slot, LocalDateTime.now().plusDays(1));

        Reservations reservations = new Reservations(List.of(firstWait, secondWait));

        // when
        Reservation actual = reservations.promoteWaiting().get();

        // then
        Assertions.assertThat(actual)
                .isEqualTo(firstWait);
    }

    @Test
    @DisplayName("예약+대기 목록에서 예약요청이 같을 경우, 승격자는 ID가 낮은(빠른) 사람이다.")
    void findPromoteWaiting_same_reservedAt() {
        LocalDateTime sameReservedAt = LocalDateTime.now();
        Reservation laterCreatedWaiting = Reservation.load(2L, "test1", slot, WAITING, sameReservedAt);
        Reservation earlierCreatedWaiting = Reservation.load(1L, "test1", slot, WAITING, sameReservedAt);

        Reservations reservations = new Reservations(List.of(laterCreatedWaiting, earlierCreatedWaiting));

        // when
        Reservation actual = reservations.promoteWaiting().get();

        // then
        Assertions.assertThat(actual)
                .isEqualTo(earlierCreatedWaiting);
    }

}
