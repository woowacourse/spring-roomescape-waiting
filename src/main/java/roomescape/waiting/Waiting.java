package roomescape.waiting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.reservation.Reservation;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Long id;

    @OneToOne
    private Reservation reservation;

    private Long rank;

    public Waiting(final Reservation reservation, final Long rank) {
        this.reservation = reservation;
        this.rank = rank;
    }
}
