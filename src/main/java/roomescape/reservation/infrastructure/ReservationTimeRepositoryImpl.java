package roomescape.reservation.infrastructure;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;


@Repository
@RequiredArgsConstructor
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final JpaReservationTimeRepository jpaRepository;

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return jpaRepository.save(reservationTime);
    }

    @Override
    public void deleteById(final Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ReservationTime> findAllByStartAt(final LocalTime startAt) {
        return jpaRepository.findAllByStartAt(startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaRepository.findAll();
    }
}
