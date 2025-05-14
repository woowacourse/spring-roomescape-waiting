package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findAll();

    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate fromDate,
                                                                LocalDate toDate);

    List<Reservation> findByDateBetween(LocalDate from, LocalDate to);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
