package roomescape.reservation.infrastructure.jpa;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.time.ReservationTimeRepository;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    public ReservationTimeRepositoryImpl(final ReservationTimeJpaRepository reservationTimeJpaRepository) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
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
    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll();
    }

    @Override
    public Optional<ReservationTime> findById(final long id) {
        return reservationTimeJpaRepository.findById(id);
    }
}
