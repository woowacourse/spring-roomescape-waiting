package roomescape.domain.reservationwaiting;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    boolean existsByReservationAndMember(Reservation reservation, Member member);
}
