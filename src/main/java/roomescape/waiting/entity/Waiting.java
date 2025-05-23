package roomescape.waiting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.ReservationSlot;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ReservationSlot reservationSlot;

    @ManyToOne
    private Member member;

    public Waiting(ReservationSlot reservationSlot, Member member) {
        this(null, reservationSlot, member);
    }
}
