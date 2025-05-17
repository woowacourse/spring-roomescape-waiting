package roomescape.reservation.infrastructure;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.domain.ReservationTimeQueryRepository;


@Repository
@RequiredArgsConstructor
public class ReservationTimeRepositoryImpl implements ReservationTimeCommandRepository, ReservationTimeQueryRepository {

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
    @Transactional(readOnly = true)
    public Optional<ReservationTime> findById(final Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationTime> findAllByStartAt(final LocalTime startAt) {
        return jpaRepository.findAllByStartAt(startAt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationTime> findAll() {
        return jpaRepository.findAll();
    }
}
