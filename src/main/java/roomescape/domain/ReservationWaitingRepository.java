package roomescape.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    Optional<ReservationWaiting> findByMemberAndReservation(Member member, Reservation reservation);

    List<ReservationWaiting> findAllByMember(Member member);

    List<ReservationWaiting> findAllByReservation(Reservation reservation);

    boolean existsByMemberAndReservation(Member member, Reservation reservation);
}
