package roomescape.reservation.infrastructure;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationTimeRepository;


@Repository
@RequiredArgsConstructor
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final JpaReservationTimeRepository jpaRepository;

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return jpaRepository.save(reservationTime);
    }

    @Override
    public void deleteById(final Long timeId) {
        jpaRepository.deleteById(timeId);
    }

    @Override
    public ReservationTime getById(final Long timeId) {
        return jpaRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다. id = " + timeId));
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
