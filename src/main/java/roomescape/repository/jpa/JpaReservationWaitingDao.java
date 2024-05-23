package roomescape.repository.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public interface JpaReservationWaitingDao extends JpaRepository<ReservationWaiting, Long> {
    Optional<ReservationWaiting> findTopByReservationOrderByCreateAt(Reservation reservation);

    @EntityGraph(attributePaths = {"reservation", "waitingMember"})
    List<ReservationWaiting> findAllByWaitingMember_Id(long waitingMemberId);

    @EntityGraph(attributePaths = {"reservation", "waitingMember"})
    List<ReservationWaiting> findAllByReservation(Reservation reservation);

    boolean existsByReservationAndWaitingMember(Reservation reservation, Member waitingMember);
}
