package roomescape.domain;

import java.time.LocalDate;
import java.util.List;

public interface ReservationCustomRepository {
    List<Reservation> searchAll(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    List<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    List<Theme> findThemeWithMostPopularReservation(String startDate, String endDate);
}
