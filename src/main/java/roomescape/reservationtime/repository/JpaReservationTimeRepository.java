package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;

@Repository
public class JpaReservationTimeRepository implements ReservationTimeRepository {

    private final ReservationTimeListCrudRepository reservationTimeListCrudRepository;

    public JpaReservationTimeRepository(ReservationTimeListCrudRepository reservationTimeListCrudRepository) {
        this.reservationTimeListCrudRepository = reservationTimeListCrudRepository;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeListCrudRepository.save(reservationTime);
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeListCrudRepository.findAll();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimeListCrudRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        reservationTimeListCrudRepository.deleteById(id);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimeListCrudRepository.existsByStartAt(startAt);
    }
}
