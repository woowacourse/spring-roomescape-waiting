package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    List<Long> findTimeIdsByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    int deleteById(long id);

    List<Reservation> findByTimeId(long timeId);

    List<Reservation> findByThemeId(long themeId);

    List<Reservation> findByMemberId(long memberId);
}
