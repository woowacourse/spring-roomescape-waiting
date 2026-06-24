package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Entity
public class ReservationTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    protected ReservationTime() {
    }

    public ReservationTime(Long id, LocalTime startAt) {
        Objects.requireNonNull(startAt, "예약 시간은 필수값 입니다.");
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime createWithoutId(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isNotReserved(List<Reservation> reservations) {
        return reservations.stream()
                .noneMatch(reservation -> reservation.getTime().equals(this));
    }

    public boolean isPast(LocalDate date, LocalDateTime now) {
        return LocalDateTime.of(date, startAt).isBefore(now);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ReservationTime reservationTime = (ReservationTime) object;
        if (id != null && reservationTime.id != null) {
            return Objects.equals(id, reservationTime.id);
        }
        return Objects.equals(startAt, reservationTime.startAt);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(startAt);
    }
}
