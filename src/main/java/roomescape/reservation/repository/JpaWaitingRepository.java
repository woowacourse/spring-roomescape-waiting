package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long themeId,
                                                       Long memberId);

    List<Waiting> findByMemberId(Long memberId);
}
