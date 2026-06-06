package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.exception.ReservationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.domain.ReservationStatus.*;
import static roomescape.reservation.exception.ReservationErrorInformation.*;

class ReservationTest {

    private final Long slotId = 1L;
    private final String name = "한다";

    private final Reservation reservation = Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now());

    @Test
    @DisplayName("아직 DB에 추가되지 않은 예약은 id가 없다.")
    void unpersist_reservation_null_id() {
        // given & when
        Reservation unpersisted = Reservation.reserve(name, slotId, LocalDateTime.now());

        // then
        assertThat(unpersisted.getId())
                .isNull();
    }

    @Test
    @DisplayName("예약자명이 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateName() {
        // given
        String nullName = null;
        String emptyName = "";

        // when & then
        assertThatThrownBy(() -> Reservation.reserve(nullName, slotId, LocalDateTime.now()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NAME_IS_NULL.getMessage());

        assertThatThrownBy(() -> Reservation.reserve(emptyName, slotId, LocalDateTime.now()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NAME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("예약 ID가 유효하지 않은 경우 load 시 예외가 발생한다.")
    void validateId() {
        // given
        Long nullId = null;

        // when & then
        assertThatThrownBy(() -> Reservation.load(nullId, name, slotId, RESERVED, LocalDateTime.now()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ID_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("본인의 예약을 취소하면 CANCELED 상태가 된다.")
    void cancel() {
        // given
        Reservation reserved = Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now());

        // when
        reserved.cancel(name);

        // then
        assertThat(reserved.getStatus()).isEqualTo(CANCELED);
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 취소하면 예외가 발생한다.")
    void cancel_not_owner() {
        // given
        String otherName = "다른사람";

        // when & then
        assertThatThrownBy(() -> reservation.cancel(otherName))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약을 취소하면 예외가 발생한다.")
    void cancel_already_canceled() {
        // given
        Reservation canceled = Reservation.load(1L, name, slotId, CANCELED, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> canceled.cancel(name))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

    @Test
    @DisplayName("예약 일정을 새 slotId로 변경할 수 있다.")
    void changeSchedule() {
        // given
        Reservation reserved = Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now());
        Long newSlotId = 99L;

        // when
        reserved.changeSchedule(name, newSlotId);

        // then
        assertThat(reserved.getSlotId())
                .isEqualTo(newSlotId);
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 변경하면 예외가 발생한다.")
    void changeSchedule_not_owner() {
        // given
        String otherName = "다른사람";

        // when & then
        assertThatThrownBy(() -> reservation.changeSchedule(otherName, 99L))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_already_canceled() {
        // given
        Reservation canceled = Reservation.load(1L, name, slotId, CANCELED, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> canceled.changeSchedule(name, 99L))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

    @Test
    @DisplayName("대기 예약은 일정 변경이 불가하다.")
    void changeSchedule_waiting() {
        // given
        Reservation waiting = Reservation.load(1L, name, slotId, WAITING, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> waiting.changeSchedule(name, 99L))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_WAITING.getMessage());
    }

    @Test
    @DisplayName("관리자는 본인 확인 없이 일정을 변경할 수 있다.")
    void changeScheduleByManager() {
        // given
        Reservation reserved = Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now());
        Long newSlotId = 99L;

        // when
        reserved.changeScheduleByManager(newSlotId);

        // then
        assertThat(reserved.getSlotId())
                .isEqualTo(newSlotId);
    }

    @Test
    @DisplayName("관리자가 이미 취소된 예약을 변경하면 예외가 발생한다.")
    void changeScheduleByManager_already_canceled() {
        // given
        Reservation canceled = Reservation.load(1L, name, slotId, CANCELED, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> canceled.changeScheduleByManager(99L))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

}
