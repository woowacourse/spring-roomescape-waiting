package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.repository.jpa.ReservationTimeJpaRepository;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    public ReservationTimeRepositoryImpl(final ReservationTimeJpaRepository reservationTimeJpaRepository) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        return reservationTimeJpaRepository.findById(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll();
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return reservationTimeJpaRepository.save(reservationTime);
    }

    @Override
    public void deleteById(final long id) {
        reservationTimeJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
    }
}
