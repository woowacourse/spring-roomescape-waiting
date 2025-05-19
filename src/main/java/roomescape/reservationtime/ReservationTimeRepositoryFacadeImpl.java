package roomescape.reservationtime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class ReservationTimeRepositoryFacadeImpl implements ReservationTimeRepositoryFacade {

    private final ReservationTimeRepository reservationTimeRepository;

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return reservationTimeRepository.save(reservationTime);
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        return reservationTimeRepository.findById(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Override
    public void delete(final ReservationTime reservationTime) {
        reservationTimeRepository.delete(reservationTime);
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeRepository.existsByStartAt(startAt);
    }
}
