package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<ReservationTime> findTimeByDateAndThemeId(LocalDate date, Long aLong);

    boolean existByTimeId(Long timeId);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existByThemeId(Long themeId);

    void deleteById(Long id);

    List<Theme> findTopThemesDurationOrderByCount(LocalDate startDate, LocalDate endDate, Integer limit);

    List<Reservation> findByDurationAndThemeIdAndMemberId(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);
}
