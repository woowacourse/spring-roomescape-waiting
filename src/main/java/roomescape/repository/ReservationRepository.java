package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    default Reservation fetchById(long id) {
        return findById(id).orElseThrow();
    }

    boolean existsByTimeId(long timeId);

    boolean existsByThemeIdAndTimeIdAndDate(long themeId, long timeId, LocalDate date);

    boolean existsByThemeId(long themeId);

    default List<Theme> findPopularThemes(LocalDate from, LocalDate until, int limit){
        return null;
    }

    default List<Reservation> searchReservations(ReservationSearchCondition condition) {
        return null;
    }
}
