package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndThemeIdAndReservationTimeIdAndDate(Long memberId, Long themeId, Long reservationTimeId,
        LocalDate date);
}
