package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.Reservation;

public interface JpaReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate from, LocalDate to);

    List<Reservation> findByThemeIdAndDate(Long themeId, LocalDate date);

    List<Reservation> findAll();

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
