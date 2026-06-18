package roomescape.domain;

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
        name = "waitlist",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"name", "date", "time_id", "theme_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate date;
    private LocalDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public Waitlist(
            Long id,
            String name,
            LocalDate date,
            LocalDateTime createdAt,
            ReservationTime time,
            Theme theme
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
    }

    public Waitlist(
            String name,
            LocalDate date,
            LocalDateTime createdAt,
            ReservationTime time,
            Theme theme
    ) {
        this(null, name, date, createdAt, time, theme);
    }

    public void verifyCancelableBy(String name) {
        verifyReservedBy(name, "본인의 대기 예약만 취소할 수 있습니다.");
    }

    private void verifyReservedBy(String other, String message) {
        if (!this.name.equals(other)) {
            throw new RoomEscapeException(UNAUTHORIZED_RESERVATION, message);
        }
    }

    public Reservation toReservation() {
        return new Reservation(name, date, time, theme);
    }

}
