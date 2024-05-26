package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    List<ReservationWaiting> findAll();

    List<ReservationWaiting> findAllByMemberId(long memberId);

    List<ReservationWaiting> findByReservation(Reservation reservation);

    Optional<ReservationWaiting> findTopWaitingByReservation(Reservation reservation);

    boolean existsByReservationAndWaitingMember(Reservation reservation, Member waitingMember);

    void delete(long id);
}
