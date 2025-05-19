package roomescape.time.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.domain.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    @Override
    public boolean existsById(final ReservationTimeId id) {
        return jpaReservationTimeRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return jpaReservationTimeRepository.existsByStartAt(startAt);
    }

    @Override
    public Optional<ReservationTime> findById(final ReservationTimeId id) {
        return jpaReservationTimeRepository.findById(id.getValue());
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime);
    }

    @Override
    public void deleteById(final ReservationTimeId id) {
        jpaReservationTimeRepository.deleteById(id.getValue());
    }
}
