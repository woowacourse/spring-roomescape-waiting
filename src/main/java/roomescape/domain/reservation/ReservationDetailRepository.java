package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
    Optional<ReservationDetail> findByScheduleAndTheme(Schedule schedule, Theme theme);
    boolean existsByScheduleDateAndScheduleTimeIdAndThemeId(ReservationDate scheduleDate, long scheduleTimeId, long themeId);
    boolean existsByScheduleTimeId(long timeId);
    List<ReservationDetail> findByScheduleDateAndThemeId(ReservationDate date, long themeId);
    boolean existsByThemeId(long themeId);
}
