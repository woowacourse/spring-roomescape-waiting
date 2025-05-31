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
public class ReservationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column
    private Long rank;

    @Builder
    private ReservationStatus(
            Long id,
            @NonNull Status status,
            Long rank
    ) {
        this.id = id;
        this.status = status;
        this.rank = rank;
    }

    public static ReservationStatus booked() {
        return builder()
                .status(Status.BOOKED)
                .rank(null)
                .build();
    }

    public static ReservationStatus waiting(Long rank) {
        return builder()
                .status(Status.WAITING)
                .rank(rank)
                .build();
    }

    public void reduceRank() {
        if (rank == null || rank == 0) {
            throw new IllegalStateException("더이상 대기 순번을 앞당길 수 없습니다. Waiting.id: " + id);
        }
        rank--;
        if (rank == 0) {
            status = Status.BOOKED;
        }
    }
}
