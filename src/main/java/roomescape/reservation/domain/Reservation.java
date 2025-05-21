package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.reservation.exception.InvalidReservationException;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationInfo info;


    protected Reservation() {
    }

    public Reservation(final ReservationInfo info) {
        this.info = info;
    }

    public static Reservation createUpcomingReservationWithUnassignedId(final ReservationInfo reservationInfo,
                                                                        LocalDateTime now) {
        validateDateTime(reservationInfo, now);
        return new Reservation(reservationInfo);
    }

    private static void validateDateTime(ReservationInfo reservationInfo, LocalDateTime now) {
        if (LocalDateTime.of(reservationInfo.getDate(), reservationInfo.getTime().getStartAt()).isBefore(now)) {
            throw new InvalidReservationException("예약 시간이 현재 시간보다 이전일 수 없습니다.");
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final Reservation that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId()) && Objects.equals(getInfo(), that.getInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getInfo());
    }

    public Long getId() {
        return id;
    }

    public ReservationInfo getInfo() {
        return info;
    }
}
