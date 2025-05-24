package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;

@Repository
@AllArgsConstructor
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return reservationTimeJpaRepository.save(reservationTime);
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
    public void delete(final ReservationTime reservationTime) {
        reservationTimeJpaRepository.delete(reservationTime);
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
    }
}
