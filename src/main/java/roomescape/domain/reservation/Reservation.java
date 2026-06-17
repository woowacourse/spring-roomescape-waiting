package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.user.User;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationSlotErrors;

@Table(name = "reservation")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_slot_id", nullable = false)
    private ReservationSlot reservationSlot;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Reservation(
        Long id,
        ReservationSlot reservationSlot,
        User user,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        validate(reservationSlot, user, status, createdAt, updatedAt);
        this.id = id;
        this.reservationSlot = reservationSlot;
        this.user = user;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Reservation createWithoutId(
        ReservationSlot reservation,
        User user,
        ReservationStatus status,
        Clock clock
    ) {
        return new Reservation(
            null,
            reservation,
            user,
            status,
            LocalDateTime.now(clock),
            LocalDateTime.now(clock)
        );
    }

    public static Reservation createWithId(long id, Reservation userReservation) {
        return of(
            id,
            userReservation.getReservationSlot(),
            userReservation.getUser(),
            userReservation.getStatus(),
            userReservation.getCreatedAt(),
            userReservation.getUpdatedAt()
        );
    }

    public Reservation update(
        ReservationSlot updatedReservationSlot,
        ReservationStatus reservationStatus,
        Clock clock
    ) {
        return new Reservation(
            id,
            updatedReservationSlot,
            user,
            reservationStatus,
            createdAt,
            LocalDateTime.now(clock)
        );
    }

    public Reservation update(
        ReservationStatus reservationStatus,
        Clock clock
    ) {
        return new Reservation(
            id,
            reservationSlot,
            user,
            reservationStatus,
            createdAt,
            LocalDateTime.now(clock)
        );
    }

    public Reservation update(Clock clock) {
        return new Reservation(
            id,
            reservationSlot,
            user,
            status,
            createdAt,
            LocalDateTime.now(clock)
        );
    }

    public static Reservation of(
        long id,
        ReservationSlot reservation,
        User user,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Reservation(id, reservation, user, status, createdAt, updatedAt);
    }

    private static void validate(
        ReservationSlot reservation,
        User user,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (reservation == null || user == null || status == null || createdAt == null || updatedAt == null) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_USER_RESERVATION);
        }
    }
}
