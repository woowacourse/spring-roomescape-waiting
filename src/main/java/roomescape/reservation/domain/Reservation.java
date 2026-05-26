package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.reservation.domain.exception.IllegalReservationDateTimeException;
import roomescape.reservation.domain.exception.IllegalStateReservationException;
import roomescape.reservation.domain.exception.UnauthorizedReservationChangeException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class Reservation {

    private Long id;
    private String name;
    private LocalDate date;
    private ReservationTime time;
    private Theme theme;
    private Status status;
    private Long is_deleted;
    private LocalDateTime createdAt;

    public Reservation withId(final Long id) {
        return Reservation.builder()
                .id(id)
                .name(this.name)
                .date(this.date)
                .time(this.time)
                .theme(this.theme)
                .status(this.status)
                .is_deleted(0L)
                .createdAt(this.createdAt)
                .build();
    }

    private void checkChangeable(final String username, final Clock clock) {
        if (!this.name.equals(username)) {
            throw new UnauthorizedReservationChangeException("예약 변경 권한이 없습니다.");
        }
        if (status == Status.CANCELED) {
            throw new IllegalStateReservationException("이미 취소된 예약은 변경할 수 없습니다.");
        }
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now(clock))) {
            throw new IllegalReservationDateTimeException("이미 지난 예약은 변경할 수 없습니다.");
        }
    }

    public Reservation changeTime(final String username, final LocalDate date, final ReservationTime time, final Theme theme, final Clock clock) {
        checkChangeable(username, clock);
        time.checkValidDateTime(date, clock);
        return Reservation.builder()
                .id(id)
                .name(username)
                .date(date)
                .time(time)
                .theme(theme)
                .status(Status.ACTIVE)
                .is_deleted(0L)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }

    public Reservation pending(final String username, final LocalDate date, final ReservationTime time, final Theme theme, final Clock clock) {
        checkChangeable(username, clock);
        time.checkValidDateTime(date, clock);
        return Reservation.builder()
                .id(id)
                .name(username)
                .date(date)
                .time(time)
                .theme(theme)
                .status(Status.PENDING)
                .is_deleted(0L)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }

    public Reservation cancel() {
        return Reservation.builder()
                .id(id)
                .name(name)
                .date(date)
                .time(time)
                .theme(theme)
                .status(Status.CANCELED)
                .is_deleted(id)
                .createdAt(createdAt)
                .build();
    }
}
