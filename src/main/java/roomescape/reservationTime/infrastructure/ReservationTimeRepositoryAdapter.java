package roomescape.reservationTime.infrastructure;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;

@Repository
@AllArgsConstructor
public class ReservationTimeRepositoryAdapter implements ReservationTimeRepository {
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
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
        return reservationTimeJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return reservationTimeJpaRepository.existsById(id);
    }
}
