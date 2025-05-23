package roomescape.repository.jpa;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Waiting;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId,
        Long memberId);
}
