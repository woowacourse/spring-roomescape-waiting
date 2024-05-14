package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.dto.AvailableTimeDto;

public interface ReservationQueryRepository {

    Optional<Reservation> findById(long id);

    List<Reservation> findAll();

    long findReservationCountByTimeId(long timeId);

    boolean existBy(LocalDate date, long timeId, long themeId);

    List<AvailableTimeDto> findAvailableReservationTimes(LocalDate date, long themeId);

    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, int limit);

    List<Reservation> findByCriteria(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);
}
