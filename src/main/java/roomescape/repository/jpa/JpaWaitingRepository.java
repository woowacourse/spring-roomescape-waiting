package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Waiting;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMemberId(Long memberId);

    long countByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId,
        Long memberId);
}
