package roomescape.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import jdk.jfr.Timestamp;

@Entity
public class DeletedReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    DeletedReservationStatus status;
    @Timestamp
    private LocalDateTime createdAt;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private Member member;

    protected DeletedReservation() {
    }

    public DeletedReservation(Long id,
                              LocalDate date,
                              LocalDateTime createdAt,
                              ReservationTime time,
                              Theme theme,
                              Member member) {
        this.id = id;
        this.date = date;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public DeletedReservation(Reservation reservation) {
        this.date = reservation.getDate();
        this.createdAt = reservation.getCreatedAt();
        this.time = reservation.getTime();
        this.theme = reservation.getTheme();
        this.member = reservation.getMember();
        this.status = mapToStatus(reservation.getStatus());
    }

    private DeletedReservationStatus mapToStatus(ReservationStatus status) {
        switch (status) {
            case ACCEPT -> {
                return DeletedReservationStatus.ACCEPT_DELETED;
            }
            case WAITING -> {
                return DeletedReservationStatus.WAIT_DELETED;
            }
            default -> throw new IllegalStateException("Unexpected value: " + status);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        DeletedReservation that = (DeletedReservation) object;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDate(), that.getDate())
                && Objects.equals(getCreatedAt(), that.getCreatedAt()) && Objects.equals(getTime(),
                that.getTime()) && Objects.equals(getTheme(), that.getTheme()) && Objects.equals(
                getMember(), that.getMember());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDate(), getCreatedAt(), getTime(), getTheme(), getMember());
    }

    @Override
    public String toString() {
        return "DeletedReservation{" +
                "id=" + id +
                ", date=" + date +
                ", createdAt=" + createdAt +
                ", time=" + time +
                ", theme=" + theme +
                ", member=" + member +
                '}';
    }
}
