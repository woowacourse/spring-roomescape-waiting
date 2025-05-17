package roomescape.time.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "start_at")
    private LocalTime startAt;

    private ReservationTime(Long id, LocalTime time) {
        this.id = id;
        this.startAt = time;
    }

    public static ReservationTime open(LocalTime time) {
        return new ReservationTime(null, time);
    }
}
