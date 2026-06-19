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

    private final Member member = new Member(1L, "roro", "러로", "password", Role.USER);

    @DisplayName("예약은 회원, 스케줄, 상태, 기준 일시를 저장한다.")
    @Test
    void create() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);

        Reservation reservation = Reservation.createBy(member, schedule, ReservationStatus.RESERVED, now);

        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getMember()).isEqualTo(member);
        assertThat(reservation.getSchedule()).isEqualTo(schedule);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getUpdateAt()).isEqualTo(now);
    }

    @DisplayName("예약 생성 시 회원, 스케줄, 상태, 기준 일시는 null일 수 없다.")
    @Test
    void createRequiredFields() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);

        assertInvalidInput(() -> Reservation.createBy(null, schedule, ReservationStatus.RESERVED, now));
        assertInvalidInput(() -> Reservation.createBy(member, null, ReservationStatus.RESERVED, now));
        assertInvalidInput(() -> Reservation.createBy(member, schedule, null, now));
        assertInvalidInput(() -> Reservation.createBy(member, schedule, ReservationStatus.RESERVED, null));
    }

    @DisplayName("예약 시각이 현재보다 미래이면 예약할 수 있다.")
    @Test
    void createFutureReservation() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 9, 59);
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));

        Reservation reservation = Reservation.createBy(member, schedule, ReservationStatus.RESERVED, now);

        assertThat(reservation.getSchedule()).isEqualTo(schedule);
    }

    @DisplayName("예약 시각이 현재와 같거나 과거이면 예약할 수 없다.")
    @Test
    void createPastOrEqualReservation() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));

        assertRoomescapeException(
                () -> Reservation.createBy(member, schedule, ReservationStatus.RESERVED,
                        LocalDateTime.of(2026, 7, 1, 10, 0)),
                DomainErrorCode.PAST_RESERVATION
        );
        assertRoomescapeException(
                () -> Reservation.createBy(member, schedule, ReservationStatus.RESERVED,
                        LocalDateTime.of(2026, 7, 1, 10, 1)),
                DomainErrorCode.PAST_RESERVATION
        );
    }

    @DisplayName("미래 예약은 본인이 취소 상태로 변경할 수 있다.")
    @Test
    void cancelBy() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Reservation reservation = new Reservation(
                1L,
                member,
                schedule,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 9, 59);

        Reservation changed = reservation.cancelBy(member, now);

        assertThat(changed.getId()).isEqualTo(1L);
        assertThat(changed.getMember()).isEqualTo(member);
        assertThat(changed.getSchedule()).isEqualTo(schedule);
        assertThat(changed.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(changed.getUpdateAt()).isEqualTo(now);
    }

    @DisplayName("현재와 같거나 과거인 예약은 취소 상태로 변경할 수 없다.")
    @Test
    void cancelByPastOrEqualReservation() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Reservation reservation = new Reservation(
                1L,
                member,
                schedule,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );

        assertRoomescapeException(
                () -> reservation.cancelBy(member, LocalDateTime.of(2026, 7, 1, 10, 0)),
                DomainErrorCode.PAST_RESERVATION
        );
        assertRoomescapeException(
                () -> reservation.cancelBy(member, LocalDateTime.of(2026, 7, 1, 10, 1)),
                DomainErrorCode.PAST_RESERVATION
        );
    }

    @DisplayName("본인 예약이 아니면 취소할 수 없다.")
    @Test
    void cancelByOtherMember() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        Reservation reservation = new Reservation(
                1L,
                member,
                schedule,
                ReservationStatus.RESERVED,
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );

        assertRoomescapeException(
                () -> reservation.cancelBy(
                        new Member(2L, "other", "다른사람", "password", Role.USER),
                        LocalDateTime.of(2026, 7, 1, 9, 59)
                ),
                DomainErrorCode.UNAUTHORIZED_RESERVATION
        );
    }

    @DisplayName("예약 상태 helper는 RESERVED와 CANCELED를 구분한다.")
    @Test
    void statusHelpers() {
        Schedule schedule = scheduleAt(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));

        Reservation reserved = new Reservation(1L, member, schedule, ReservationStatus.RESERVED, LocalDateTime.now());
        Reservation waiting = new Reservation(2L, member, schedule, ReservationStatus.WAITING, LocalDateTime.now());
        Reservation canceled = new Reservation(3L, member, schedule, ReservationStatus.CANCELED, LocalDateTime.now());

        assertThat(reserved.isReserved()).isTrue();
        assertThat(waiting.isReserved()).isFalse();
        assertThat(canceled.isAlreadyCanceled()).isTrue();
    }

    private Schedule scheduleAt(LocalDate date, LocalTime time) {
        return new Schedule(
                1L,
                new Theme(1L, "잠긴 방", "설명", "https://example.com/theme.jpg", 20000),
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
