package roomescape.business.model.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.vo.Id;

public interface ReservationTimeRepository {

    void save(ReservationTime time);

    List<ReservationTime> findAll();

    List<ReservationTime> findAvailableByDateAndThemeId(LocalDate date, Id themeId);

    List<ReservationTime> findNotAvailableByDateAndThemeId(LocalDate date, Id themeId);

    Optional<ReservationTime> findById(Id timeId);

    boolean existBetween(LocalTime startInclusive, LocalTime endExclusive);

    boolean existById(Id timeId);

    boolean existByTime(LocalTime createTime);

    void deleteById(Id timeId);
}
