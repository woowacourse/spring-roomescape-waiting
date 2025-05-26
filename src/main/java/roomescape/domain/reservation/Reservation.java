package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.member.Member;
import roomescape.domain.reservationitem.ReservationItem;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Reservation {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "reservation_item_id")
    private ReservationItem reservationItem;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @Builder
    public Reservation(Member member, ReservationItem reservationItem, ReservationStatus reservationStatus) {
        this.member = member;
        this.reservationItem = reservationItem;
        this.reservationStatus = reservationStatus;
    }

    public void changeStatusToAccepted() {
        this.reservationStatus = ReservationStatus.ACCEPTED;
    }

    public void changeStatusToDenied() {
        this.reservationStatus = ReservationStatus.DENIED;
    }
}
