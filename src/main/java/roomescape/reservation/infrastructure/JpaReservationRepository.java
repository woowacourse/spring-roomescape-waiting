package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface JpaReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate from, LocalDate to);

    List<Reservation> findByThemeIdAndTimeIdAndDate(Long themeId, Long timeId, LocalDate date);

    List<Reservation> findAll();

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByThemeIdAndTimeIdAndDateAndReservationStatus(Long themeId, Long timeId, LocalDate date, ReservationStatus status);
}
