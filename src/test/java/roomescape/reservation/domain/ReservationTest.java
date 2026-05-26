package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;
import static roomescape.reservation.exception.ReservaitonErrorInformation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

class ReservationTest {

    private final String name = "한다";
    private final LocalDate date = LocalDate.now().plusMonths(1);
    private final ReservationDate reservationDate = ReservationDate.create(date);
    private final ReservationDate pastDate = ReservationDate.load(2L, LocalDate.now().minusDays(1),
        true);
    private final LocalTime startAt = LocalTime.of(15, 40);
    private final ReservationTime reservationTime = ReservationTime.create(startAt);
    private final Theme theme = Theme.load(1L, "테마", "설명", "썸네일", true);
    private final Reservation reservation = Reservation.load(1L, name, reservationDate,
        reservationTime, theme, RESERVED, LocalDateTime.now());

    @Test
    @DisplayName("예약 id를 가져온다.")
    void getId() {
        //given
        Long expected = 1L;

        //when
        Long actual = reservation.getId();

        //then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("예약자명을 가져온다.")
    void getName() {
        //given
        String expected = "한다";

        //when
        String actual = reservation.getName();

        //then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("예약날짜를 가져온다.")
    void getDate() {
        //given
        LocalDate expected = LocalDate.now().plusMonths(1);

        //when
        LocalDate actual = reservation.getDate().getDate();

        //then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("예약시간을 가져온다.")
    void getTime() {
        //given
        LocalTime expected = startAt;

        //when
        LocalTime actual = reservation.getTime().getStartAt();

        //then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("두 예약 객체의 동등성을 비교한다.")
    void equals() {
        //given
        Reservation otherReservation = Reservation.load(1L, name, reservationDate, reservationTime,
            theme, RESERVED, LocalDateTime.now());

        // when & then
        assertThat(reservation)
            .usingRecursiveComparison()
            .ignoringFields("reservedAt")
            .isEqualTo(otherReservation);
    }

    @Test
    @DisplayName("아직 DB에 추가되지 않은 예약은 id가 없다.")
    void unpersist_reservation_null_id() {
        // given & when
        Reservation unpersistReservation = Reservation.create("한다", reservationDate,
            reservationTime, theme, LocalDateTime.now());

        // then
        assertThat(unpersistReservation.getId())
            .isNull();
    }

    @Test
    @DisplayName("예약자명이 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateName() {
        // given
        String nullName = null;
        String emptyName = "";

        // when & then
        assertThatThrownBy(
            () -> Reservation.create(nullName, reservationDate, reservationTime, theme,
                LocalDateTime.now()))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_NAME_IS_NULL.getMessage());

        assertThatThrownBy(
            () -> Reservation.create(emptyName, reservationDate, reservationTime, theme,
                LocalDateTime.now()))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_NAME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("예약 시간이 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateTime() {
        // given
        ReservationTime nullTime = null;

        // when & then
        assertThatThrownBy(
            () -> Reservation.create(name, reservationDate, nullTime, theme, LocalDateTime.now()))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_TIME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("예약 날짜가 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateDate() {
        // given
        ReservationDate nullDate = null;

        // when & then
        assertThatThrownBy(
            () -> Reservation.create(name, nullDate, reservationTime, theme, LocalDateTime.now()))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_DATE_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("예약 ID가 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateId() {
        // given
        Long nullId = null;

        // when & then
        assertThatThrownBy(
            () -> Reservation.load(nullId, name, reservationDate, reservationTime, theme, RESERVED,
                LocalDateTime.now()))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_ID_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("아직 지나지 않은 본인의 예약은 취소할 수 있다.")
    void cancel() {
        // given
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);

        // when
        reserved.cancel(name);

        // then
        assertThat(reserved.getStatus())
            .isEqualTo(CANCELED);
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 취소를하면 예외가 발생한다.")
    void cancel_not_owner() {
        // given
        String notOwerName = "주주";
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);

        // when & then
        assertThatThrownBy(() -> reserved.cancel(notOwerName))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약을 취소하면 예외가 발생한다.")
    void cancel_already_canceled() {
        // given
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);
        reserved.updateStatus(CANCELED);

        // when & then
        assertThatThrownBy(() -> reserved.cancel(name))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

    @Test
    @DisplayName("이미 지난 예약을 취소하면 예외가 발생한다.")
    void cancel_past() {
        // given
        Reservation reserved = Reservation.load(2L, name, pastDate, reservationTime, theme,
            RESERVED, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> reserved.cancel(name))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("예약 가능한 날짜로 변경할 수 있다.")
    void changeSchedule() {
        // given
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);
        ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();
        ReservationTime changedTime = ReservationTimeFixture.activeTime15();

        // when
        reserved.changeSchedule(name, changedDate, changedTime);

        // then
        assertThat(reserved.getDate())
            .usingRecursiveComparison()
            .isEqualTo(changedDate);
        assertThat(reserved.getTime())
            .usingRecursiveComparison()
            .isEqualTo(changedTime);
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 변경을 시도하면 예외가 발생한다.")
    void changeSchedule_not_owner() {
        // given
        String notOwerName = "다른사람";
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);
        ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

        // when && then
        Assertions.assertThatThrownBy(
                () -> reserved.changeSchedule(notOwerName, changedDate, reservationTime))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_already_canceled() {
        // given
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);
        reserved.updateStatus(CANCELED);
        ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

        // when && then
        Assertions.assertThatThrownBy(
                () -> reserved.changeSchedule(name, changedDate, reservationTime))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

    @Test
    @DisplayName("이미 지난 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_past() {
        // given
        Reservation reserved = Reservation.load(2L, name, pastDate, reservationTime, theme,
            RESERVED, LocalDateTime.now());
        ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

        // when & then
        assertThatThrownBy(() -> reserved.changeSchedule(name, changedDate, reservationTime))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("지난 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_new_datetime_is_past() {
        // given
        Reservation reserved = Reservation.load(2L, name, reservationDate, reservationTime, theme,
            RESERVED, LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> reserved.changeSchedule(name, pastDate, reservationTime))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("관리자는 본인 확인없이, 예약 날짜/시간을 변경할 수 있다.")
    void changeScheduleByManager() {
        // given
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);
        ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();
        ReservationTime changedTime = ReservationTimeFixture.activeTime15();

        // when
        reserved.changeScheduleByManager(changedDate, changedTime);

        // then
        assertThat(reserved.getDate())
            .usingRecursiveComparison()
            .isEqualTo(changedDate);
        assertThat(reserved.getTime())
            .usingRecursiveComparison()
            .isEqualTo(changedTime);
    }

    @Test
    @DisplayName("관리자가 이미 지난 날짜/시간으로 변경할 시, 예외가 발생한다.")
    void changeScheduleByManager_pastDateTime() {
        // given
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);
        ReservationDate yesterday = ReservationDate.load(2L, LocalDate.now().minusDays(1), true);
        ReservationTime time = ReservationTimeFixture.activeTime15();

        // when & then
        assertThatThrownBy(() -> reserved.changeScheduleByManager(yesterday, time))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("관리자가 이미 취소된 예약을 변경하면 예외가 발생한다.")
    void changeScheduleByManager_already_canceled() {
        // given
        Reservation reserved = ReservationFixture.reservation(name, reservationDate,
            reservationTime, theme);
        reserved.updateStatus(CANCELED);
        ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

        // when && then
        Assertions.assertThatThrownBy(
                () -> reserved.changeScheduleByManager(changedDate, reservationTime))
            .isInstanceOf(ReservationException.class)
            .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

}
