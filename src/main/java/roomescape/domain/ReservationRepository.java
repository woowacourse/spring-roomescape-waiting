package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    Optional<Reservation> findById(long id);

    List<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    List<Theme> findThemeWithMostPopularReservation(String startDate, String endDate);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    Reservation save(Reservation reservation);

    void delete(Reservation reservation);
}
