package roomescape.time.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.global.error.ErrorCode;
import roomescape.reservation.exception.PastReservationException;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReservationTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime startAt;

    @Column
    private LocalDateTime endAt;

    public ReservationTime(LocalDateTime startAt, LocalDateTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void validateExpired(LocalDateTime dateTime) {
        if (startAt.isBefore(dateTime)) {
            throw new PastReservationException(ErrorCode.RESERVATION_EXPIRED);
        }
    }

    public void updateStart(LocalDateTime startAt) {
        this.startAt = startAt;
    }
}
