package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.ReservationDetail;

public interface ReservationDetailRepository extends Repository<ReservationDetail, Long> {
    List<ReservationDetail> findAll();

    Optional<ReservationDetail> findById(Long id);

    default List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId) {
        return findByDateAndThemeId(date, themeId).stream()
                .map(TimeIdProjection::getTimeId)
                .toList();
    }

    List<TimeIdProjection> findByDateAndThemeId(LocalDate date, Long themeId);

    Optional<ReservationDetail> findByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    ReservationDetail save(ReservationDetail reservationDetail);

    void delete(ReservationDetail reservationDetail);

    void deleteAll();
}
