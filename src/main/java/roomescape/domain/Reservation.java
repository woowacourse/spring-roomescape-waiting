package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.PAST_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.exception.RoomEscapeException;

@Entity
@Table(
        name = "reservation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_slot",
                        columnNames = {"date", "time_id", "theme_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public Reservation(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(null, name, date, time, theme);
    }

    public void verifyReservable(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점에 예약할 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public void verifyCancelableBy(String name, LocalDateTime now) {
        verifyReservedBy(name, "본인의 예약만 취소할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    public void changeDateAndTime(String name, LocalDateTime now, LocalDate newDate, ReservationTime newTime) {
        verifyReservedBy(name, "본인의 예약만 변경할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 변경할 수 없습니다.");
        }
        if (LocalDateTime.of(newDate, newTime.getStartAt()).isBefore(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점으로 변경할 수 없습니다.");
        }
        this.date = newDate;
        this.time = newTime;
    }

    private void verifyReservedBy(String other, String message) {
        if (!this.name.equals(other)) {
            throw new RoomEscapeException(UNAUTHORIZED_RESERVATION, message);
        }
    }

}
