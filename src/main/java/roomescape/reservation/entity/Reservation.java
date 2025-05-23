package roomescape.reservation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.entity.Member;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private ReservationSlot reservationSlot;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    public Reservation(ReservationSlot reservationSlot, Member member) {
        this(null, reservationSlot, member);
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(reservationSlot.getDate(), reservationSlot.getTime().getStartAt());
    }
}
