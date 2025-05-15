package roomescape.reservationtime.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false)
    private LocalTime startAt;

    @Builder
    private ReservationTime(final Long id, @NonNull final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime from(final LocalTime startAt) {
        return builder()
                .startAt(startAt)
                .build();
    }
}
