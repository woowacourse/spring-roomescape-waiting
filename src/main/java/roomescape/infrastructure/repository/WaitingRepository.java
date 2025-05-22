package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    long countByReservation(Reservation reservation);

    List<Waiting> findAllByMember(Member member);

    boolean existsByReservationAndMember(Reservation reservation, Member member);
}
