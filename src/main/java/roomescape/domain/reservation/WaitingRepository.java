package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByThemeScheduleAndMemberId(@Param("themeSchedule") ThemeSchedule themeSchedule,
                                             @Param("memberId") Long memberId);
}
