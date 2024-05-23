package roomescape.domain.reservationdetail;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;

import java.util.List;
import java.util.Optional;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
    Optional<ReservationDetail> findByScheduleAndTheme(Schedule schedule, Theme theme);

    boolean existsByScheduleTimeId(long timeId);

    List<ReservationDetail> findByScheduleDateAndThemeId(ReservationDate date, long themeId);
}
