package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationWait;

public interface ReservationWaitRepository extends JpaRepository<ReservationWait, Long> {
    List<ReservationWait> findByMemberId(long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);
}
