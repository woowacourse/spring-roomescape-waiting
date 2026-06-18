package roomescape.reservationtime.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.reservationtime.domain.ReservationTime;

@Entity
@Table(name = "reservation_time")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    private ReservationTimeEntity(final LocalTime startAt) {
        this.startAt = startAt;
    }

    public static ReservationTimeEntity from(final ReservationTime time) {
        return new ReservationTimeEntity(time.getStartAt());
    }

    public ReservationTime toDomain() {
        return ReservationTime.of(id, startAt);
    }
}
