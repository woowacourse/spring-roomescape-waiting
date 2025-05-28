package roomescape.business.model.repository;

import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.vo.Id;

import java.time.LocalTime;
import java.util.Optional;

public interface ReservationTimes {

    void save(ReservationTime time);

    Optional<ReservationTime> findById(Id timeId);

    boolean existBetween(LocalTime startInclusive, LocalTime endExclusive);

    boolean existById(Id timeId);

    boolean existByTime(LocalTime createTime);

    void deleteById(Id timeId);
}
