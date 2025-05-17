package roomescape.infrastructure;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationTimeRepository;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.StartTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JpaReservationTimeRepository implements ReservationTimeRepository {

    private final JpaReservationTimeDao dao;

    public JpaReservationTimeRepository(JpaReservationTimeDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(ReservationTime time) {
        dao.save(time);
    }

    @Override
    public List<ReservationTime> findAll() {
        return dao.findAll();
    }

    @Override
    public List<ReservationTime> findAvailableByDateAndThemeId(LocalDate date, Id themeId) {
        return dao.findAvailableByDateAndThemeId(new ReservationDate(date), themeId);
    }

    @Override
    public List<ReservationTime> findNotAvailableByDateAndThemeId(LocalDate date, Id themeId) {
        return dao.findNotAvailableByDateAndThemeId(new ReservationDate(date), themeId);
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
