package roomescape.repository.reservationtime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import roomescape.domain.reservationtime.ReservationTime;

@Component
public class JpaReservationTimeRepositoryAdapter implements ReservationTimeRepository {

    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    public JpaReservationTimeRepositoryAdapter(JpaReservationTimeRepository jpaReservationTimeRepository) {
        this.jpaReservationTimeRepository = jpaReservationTimeRepository;
    }

    @Override
    public long save(ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime).getId();
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationTimeRepository.deleteById(id);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jpaReservationTimeRepository.findById(id);
    }

    @Override
    public boolean existsByTime(LocalTime time) {
        return jpaReservationTimeRepository.existsByTime(time);
    }
}
