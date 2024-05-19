package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationDate;
import roomescape.domain.Waiting;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, ReservationDate date, Long timeId, Long themeId);
}
