package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;

@Repository
public class JpaReservationTimeRepositoryAdapter implements ReservationTimeRepository {

    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    public JpaReservationTimeRepositoryAdapter(final JpaReservationTimeRepository jpaReservationTimeRepository) {
        this.jpaReservationTimeRepository = jpaReservationTimeRepository;
    }

    @Override
    public boolean existsByStartAt(final LocalTime time) {
        return jpaReservationTimeRepository.existsByStartAt(time);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }

    @Override
    public void deleteById(final Long id) {
        jpaReservationTimeRepository.deleteById(id);
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime);
    }

    @Override
    public Optional<ReservationTime> findById(final Long reservationTimeId) {
        return jpaReservationTimeRepository.findById(reservationTimeId);
    }
}
