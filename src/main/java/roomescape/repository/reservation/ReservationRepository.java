package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.dto.search.SearchConditions;

public interface ReservationRepository {
    Reservation addReservation(Reservation reservation);

    int deleteReservationById(Long id);

    List<Reservation> findAllReservation();

    boolean existsByDateAndTime(LocalDate date, Long timeId);

    boolean existsByTimeId(Long id);

    List<Long> findTimeIdsByDateAndTheme(LocalDate date, Long themeId);

    List<Long> findTopThemesByReservationCountBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findReservationsByConditions(SearchConditions searchConditions);
}
