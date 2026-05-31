package roomescape.persistence.jdbc;

import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.persistence.jdbc.dao.ReservationTimeDao;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final ReservationTimeDao reservationTimeDao;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeDao.save(reservationTime);
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimeDao.findById(id);
    }

    @Override
    public boolean existsByStartAt(LocalTime time) {
        return reservationTimeDao.existsByStartAt(time);
    }

    @Override
    public void update(ReservationTime time) {
        reservationTimeDao.update(time);
    }
}
