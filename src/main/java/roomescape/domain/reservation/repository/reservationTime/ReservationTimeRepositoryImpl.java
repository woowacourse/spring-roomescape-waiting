package roomescape.domain.reservation.repository.reservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    public ReservationTimeRepositoryImpl(JpaReservationTimeRepository jpaReservationTimeRepository) {
        this.jpaReservationTimeRepository = jpaReservationTimeRepository;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jpaReservationTimeRepository.findById(id);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return jpaReservationTimeRepository.existsByStartAt(startAt);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationTimeRepository.deleteById(id);
    }
}
