package roomescape.reservation.repository.jpa;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    public ReservationTimeRepositoryImpl(final ReservationTimeJpaRepository reservationTimeJpaRepository) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return reservationTimeJpaRepository.save(reservationTime);
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll();
    }

    @Override
    public Optional<ReservationTime> findById(final long id) {
        return reservationTimeJpaRepository.findById(id);
    }

    @Override
    public boolean existsByTime(final LocalTime reservationTime) {
        return reservationTimeJpaRepository.existsByStartAt(reservationTime);
    }

    @Override
    public void deleteById(final long id) {
        reservationTimeJpaRepository.deleteById(id);
    }
}
