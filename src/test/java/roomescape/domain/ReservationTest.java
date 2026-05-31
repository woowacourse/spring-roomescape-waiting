package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

class ReservationTest {

    private final Reserver reserver = new Reserver("러로");

    @DisplayName("예약은 예약자, 스케줄, 상태, 기준 일시를 저장한다.")
    @Test
    void create() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);

        Reservation reservation = Reservation.createBy(reserver, schedule, ReservationStatus.RESERVED, now);

        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getReserver()).isEqualTo(reserver);
        assertThat(reservation.getSchedule()).isEqualTo(schedule);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getUpdateAt()).isEqualTo(now);
    }

    @DisplayName("예약 생성 시 예약자, 스케줄, 상태, 기준 일시는 null일 수 없다.")
    @Test
    void createRequiredFields() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);

        assertInvalidInput(() -> Reservation.createBy(null, schedule, ReservationStatus.RESERVED, now));
        assertInvalidInput(() -> Reservation.createBy(reserver, null, ReservationStatus.RESERVED, now));
        assertInvalidInput(() -> Reservation.createBy(reserver, schedule, null, now));
        assertInvalidInput(() -> Reservation.createBy(reserver, schedule, ReservationStatus.RESERVED, null));
    }

    @DisplayName("예약 시각이 현재보다 미래이면 예약할 수 있다.")
    @Test
    void createFutureReservation() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 9, 59);
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));

        Reservation reservation = Reservation.createBy(reserver, schedule, ReservationStatus.RESERVED, now);

        assertThat(reservation.getSchedule()).isEqualTo(schedule);
    }

    @DisplayName("예약 시각이 현재와 같거나 과거이면 예약할 수 없다.")
    @Test
    void createPastOrEqualReservation() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));

        assertRoomescapeException(
                () -> Reservation.createBy(reserver, schedule, ReservationStatus.RESERVED,
                        LocalDateTime.of(2026, 7, 1, 10, 0)),
                DomainErrorCode.PAST_RESERVATION
        );
        assertRoomescapeException(
                () -> Reservation.createBy(reserver, schedule, ReservationStatus.RESERVED,
                        LocalDateTime.of(2026, 7, 1, 10, 1)),
                DomainErrorCode.PAST_RESERVATION
        );
    }

    @DisplayName("예약 수정은 같은 예약자만 가능하며 ID와 예약자는 유지하고 스케줄과 상태를 바꾼다.")
    @Test
    void updateBy() {
        Schedule origin = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Schedule target = scheduleAt(LocalDate.of(2026, 7, 2), LocalTime.of(11, 0));
        Reservation reservation = new Reservation(
                1L,
                reserver,
                origin,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 11, 0);

        Reservation updated = reservation.updateBy(reserver, target, ReservationStatus.WAITING, now);

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getReserver()).isEqualTo(reserver);
        assertThat(updated.getSchedule()).isEqualTo(target);
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(updated.getUpdateAt()).isEqualTo(now);
    }

    @DisplayName("다른 예약자는 예약을 수정할 수 없다.")
    @Test
    void updateByOtherReserver() {
        Schedule origin = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Schedule target = scheduleAt(LocalDate.of(2026, 7, 2), LocalTime.of(11, 0));
        Reservation reservation = new Reservation(
                1L,
                reserver,
                origin,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );

        assertRoomescapeException(
                () -> reservation.updateBy(
                        new Reserver("다른사람"),
                        target,
                        ReservationStatus.WAITING,
                        LocalDateTime.of(2026, 6, 1, 11, 0)
                ),
                DomainErrorCode.UNAUTHORIZED_RESERVATION
        );
    }

    @DisplayName("수정 대상 스케줄이 현재와 같거나 과거이면 수정할 수 없다.")
    @Test
    void updateByPastSchedule() {
        Reservation reservation = new Reservation(
                1L,
                reserver,
                scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0)),
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );
        Schedule target = scheduleAt(LocalDate.of(2026, 7, 2), LocalTime.of(11, 0));

        assertRoomescapeException(
                () -> reservation.updateBy(
                        reserver,
                        target,
                        ReservationStatus.RESERVED,
                        LocalDateTime.of(2026, 7, 2, 11, 0)
                ),
                DomainErrorCode.PAST_RESERVATION
        );
    }

    @DisplayName("미래 예약은 본인이 취소 상태로 변경할 수 있다.")
    @Test
    void changeBy() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Reservation reservation = new Reservation(
                1L,
                reserver,
                schedule,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 9, 59);

        Reservation changed = reservation.changeBy(reserver, schedule, now);

        assertThat(changed.getId()).isEqualTo(1L);
        assertThat(changed.getReserver()).isEqualTo(reserver);
        assertThat(changed.getSchedule()).isEqualTo(schedule);
        assertThat(changed.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(changed.getUpdateAt()).isEqualTo(now);
    }

    @DisplayName("현재와 같거나 과거인 예약은 취소 상태로 변경할 수 없다.")
    @Test
    void changeByPastOrEqualReservation() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Reservation reservation = new Reservation(
                1L,
                reserver,
                schedule,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );

        assertRoomescapeException(
                () -> reservation.changeBy(reserver, schedule, LocalDateTime.of(2026, 7, 1, 10, 0)),
                DomainErrorCode.PAST_RESERVATION
        );
        assertRoomescapeException(
                () -> reservation.changeBy(reserver, schedule, LocalDateTime.of(2026, 7, 1, 10, 1)),
                DomainErrorCode.PAST_RESERVATION
        );
    }

    @DisplayName("본인 예약이 아니면 취소할 수 없다.")
    @Test
    void changeByOtherReserver() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Reservation reservation = new Reservation(
                1L,
                reserver,
                schedule,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );

        assertRoomescapeException(
                () -> reservation.changeBy(
                        new Reserver("다른사람"),
                        schedule,
                        LocalDateTime.of(2026, 7, 1, 9, 59)
                ),
                DomainErrorCode.UNAUTHORIZED_RESERVATION
        );
    }

    @DisplayName("예약 상태 helper는 RESERVED와 CANCELED를 구분한다.")
    @Test
    void statusHelpers() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));

        Reservation reserved = new Reservation(1L, reserver, schedule, ReservationStatus.RESERVED, LocalDateTime.now());
        Reservation waiting = new Reservation(2L, reserver, schedule, ReservationStatus.WAITING, LocalDateTime.now());
        Reservation canceled = new Reservation(3L, reserver, schedule, ReservationStatus.CANCELED, LocalDateTime.now());

        assertThat(reserved.isReserved()).isTrue();
        assertThat(waiting.isReserved()).isFalse();
        assertThat(canceled.isAlreadyCanceled()).isTrue();
    }

    private Schedule scheduleAt(LocalDate date, LocalTime time) {
        return new Schedule(
                1L,
                new Theme(1L, "잠긴 방", "설명", "https://example.com/theme.jpg"),
                date,
                new ReservationTime(1L, time)
        );
    }

    private void assertInvalidInput(Runnable runnable) {
        assertRoomescapeException(runnable, DomainErrorCode.INVALID_INPUT);
    }

    private void assertRoomescapeException(Runnable runnable, DomainErrorCode code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(code);
    }
}
