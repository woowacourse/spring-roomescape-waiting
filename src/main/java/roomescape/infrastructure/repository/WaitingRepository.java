package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.ReservationInfo;
import roomescape.domain.Waiting;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    long countByReservationInfo(ReservationInfo reservationInfo);

    List<Waiting> findAllByMember(Member member);

    boolean existsByReservationInfoAndMember(ReservationInfo reservationInfo, Member member);

    Optional<Waiting> findTop1ByReservationInfoOrderByRankAsc(ReservationInfo reservationInfo);

    List<Waiting> findAllByReservationInfo(ReservationInfo reservationInfo);

    boolean existsByReservationInfo(ReservationInfo reservationInfo);
}
