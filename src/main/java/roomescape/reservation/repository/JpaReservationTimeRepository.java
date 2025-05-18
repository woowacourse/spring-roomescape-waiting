package roomescape.reservation.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;

@RequiredArgsConstructor
@Repository
public class JpaReservationTimeRepository implements ReservationTimeRepositoryInterface {

    private final ReservationTimeRepository reservationTimeRepository;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeRepository.save(reservationTime);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimeRepository.findById(id);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimeRepository.existsByStartAt(startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
