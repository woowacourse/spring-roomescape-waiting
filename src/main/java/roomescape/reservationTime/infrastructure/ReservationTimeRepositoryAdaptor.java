package roomescape.reservationTime.infrastructure;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;

@Repository
public class ReservationTimeRepositoryAdaptor implements ReservationTimeRepository {
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final ReservationTimeJdbcDao reservationTimeJdbcDao;

    public ReservationTimeRepositoryAdaptor(ReservationTimeJpaRepository reservationTimeJpaRepository,
                                            ReservationTimeJdbcDao reservationTimeJdbcDao) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
        this.reservationTimeJdbcDao = reservationTimeJdbcDao;
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
    }

    @Override
    public Collection<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll();
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeJpaRepository.save(reservationTime);
    }

    @Override
    public void deleteById(Long id) {
        reservationTimeJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimeJdbcDao.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return reservationTimeJpaRepository.existsById(id);
    }
}
