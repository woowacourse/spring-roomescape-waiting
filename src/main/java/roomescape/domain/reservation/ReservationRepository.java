package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.exception.UnauthorizedException;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByScheduleDateAndScheduleTimeIdAndThemeId(ReservationDate scheduleDate, long scheduleTimeId,
                                                            long themeId);

    boolean existsByScheduleTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    default Reservation getById(long id) {
        return findById(id).orElseThrow(() -> new UnauthorizedException("더이상 존재하지 않는 예약입니다."));
    }

    List<Reservation> findBy(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByScheduleDateAndThemeId(ReservationDate date, long themeId);
}
