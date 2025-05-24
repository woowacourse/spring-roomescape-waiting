package roomescape.reservation.domain.waiting;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.reservation.Reservation;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class ReservationWaiting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public ReservationWaiting(final Reservation reservation, final Member member) {
        this(null, reservation, member);
    }
}
