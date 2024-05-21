package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.ReservationDetail;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.reservation.NotFoundReservationException;

public interface ReservationDetailRepository extends Repository<ReservationDetail, Long> {
    ReservationDetail save(ReservationDetail reservationDetail);

    default ReservationDetail getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    default ReservationDetail getByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        return findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> save(new ReservationDetail(date, time, theme)));
    }

    default List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId) {
        return findByDateAndThemeId(date, themeId).stream()
                .map(TimeIdProjection::getTimeId)
                .toList();
    }

    List<ReservationDetail> findAll();

    Optional<ReservationDetail> findById(Long id);

    List<TimeIdProjection> findByDateAndThemeId(LocalDate date, Long themeId);

    Optional<ReservationDetail> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void delete(ReservationDetail reservationDetail);

    void deleteAll();
}
