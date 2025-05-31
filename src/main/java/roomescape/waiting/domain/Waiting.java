package roomescape.waiting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
@Table(name = "RESERVATION_WAITING")
public class Waiting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Waiting(final Reservation reservation, Member member) {
        this(null, reservation, member);
    }
}
