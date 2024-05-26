package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @EntityGraph(attributePaths = {"member", "reservation.time", "reservation.theme"})
    List<Waiting> findAll();

    @EntityGraph(attributePaths = {"reservation.time", "reservation.theme"})
    List<Waiting> findAllByMember(Member member);

    Long countAllByReservationAndIdLessThanEqual(Reservation reservation, final Long id);

    boolean existsByReservation(Reservation reservation);

    boolean existsByReservationAndMember(Reservation reservation, Member member);

    Optional<Waiting> findFirstByReservation(Reservation reservation);
}
