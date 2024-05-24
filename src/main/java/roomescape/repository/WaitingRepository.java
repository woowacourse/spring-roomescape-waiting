package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long memberId);
}
