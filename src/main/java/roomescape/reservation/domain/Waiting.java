package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column
    private Long rank;

    @Builder
    private Waiting(
            Long id,
            @NonNull ReservationStatus status,
            Long rank
    ) {
        this.id = id;
        this.status = status;
        this.rank = rank;
    }

    public static Waiting booked() {
        return builder()
                .status(ReservationStatus.BOOKED)
                .rank(null)
                .build();
    }

    public static Waiting waiting(Long rank) {
        return builder()
                .status(ReservationStatus.WAITING)
                .rank(rank)
                .build();
    }
}
