package roomescape.repository;

import java.util.List;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    List<ReservationWaiting> findAllByMemberId(long memberId);

    List<ReservationWaiting> findByReservation(Reservation reservation);

    boolean existsByReservationAndWaitingMember(Reservation reservation, Member waitingMember);
}
