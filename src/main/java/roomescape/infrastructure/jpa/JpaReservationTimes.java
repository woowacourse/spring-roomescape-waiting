package roomescape.infrastructure.jpa;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;
import roomescape.infrastructure.jpa.dao.JpaReservationTimeDao;

import java.time.LocalTime;
import java.util.Optional;

@Primary
@Repository
public class JpaReservationTimes implements ReservationTimes {

    private final JpaReservationTimeDao dao;

    public JpaReservationTimes(JpaReservationTimeDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(ReservationTime time) {
        dao.save(time);
    }

    @Override
    public Optional<ReservationTime> findById(Id timeId) {
        return dao.findById(timeId);
    }

    @Override
    public boolean existBetween(LocalTime startInclusive, LocalTime endExclusive) {
        return dao.existsByStartTimeBetween(new StartTime(startInclusive), new StartTime(endExclusive));
    }

    @Override
    public boolean existById(Id timeId) {
        return dao.existsById(timeId);
    }

    @Override
    public boolean existByTime(LocalTime createTime) {
        return dao.existsByStartTime(new StartTime(createTime));
    }

    @Override
    public void deleteById(Id timeId) {
        dao.deleteById(timeId);
    }
}
