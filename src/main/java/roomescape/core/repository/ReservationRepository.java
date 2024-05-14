package roomescape.core.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.Theme;

public interface ReservationRepository {
    Long save(final Reservation reservation);

    List<Reservation> findAll();

    List<Theme> findPopularTheme(final LocalDate today, final LocalDate yesterday);

    List<Reservation> findAllByDateAndThemeId(final String date, long themeId);

    List<Reservation> findAllByMemberAndThemeAndPeriod(final Long memberId, final Long themeId, final String dateFrom,
                                                       final String dateTo);

    Integer countByTimeId(final long timeId);

    Integer countByThemeId(final long themeId);

    Integer countByDateAndTimeIdAndThemeId(final String date, final long timeId, final long themeId);

    void deleteById(final long id);
}
