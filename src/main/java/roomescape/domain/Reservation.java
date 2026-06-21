package roomescape.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_slot_id")
    private ReservationSlot reservationSlot;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    protected Reservation(){}

    public Reservation(ReservationSlot reservationSlot, String name, Status status, LocalDateTime updateAt) {
        validateName(name);
        this.reservationSlot = reservationSlot;
        this.name = name;
        this.status = status;
        this.updateAt = updateAt;
    }

    public boolean isReserved() {
        return status == Status.RESERVED;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isUpdatedAtBefore(Reservation other) {
        return updateAt.isBefore(other.updateAt);
    }

    public void promote() {
        if (!isWaiting()) {
            throw new CustomException(ErrorCode.RESERVATION_STATUS_UNAVAILABLE);
        }
        this.status = Status.RESERVED;
    }

    public void update(LocalDateTime now, Status status) {
        validateUpdateAt(now);
        validateNotCanceledStatus();
        this.status = status;
        this.updateAt = now;
    }

    public void cancel(LocalDateTime now) {
        validateUpdateAt(now);
        validateNotCanceledStatus();
        this.status = Status.CANCELED;
        this.updateAt = now;
    }

    private void validateName(String name) {
        if (name.length() > 255) {
            throw new CustomException(ErrorCode.RESERVATION_NAME_TOO_LONG);
        }
    }

    private void validateUpdateAt(LocalDateTime now) {
        if (now.isBefore(this.updateAt)) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_UNAVAILABLE);
        }
    }

    private void validateNotCanceledStatus() {
        if (this.status == Status.CANCELED) {
            throw new CustomException(ErrorCode.RESERVATION_STATUS_UNAVAILABLE);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public ReservationSlot getReservationSlot() {
        return reservationSlot;
    }
}
