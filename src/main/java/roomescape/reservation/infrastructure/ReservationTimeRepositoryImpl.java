package roomescape.reservation.infrastructure;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.domain.ReservationTimeQueryRepository;


@Repository
@RequiredArgsConstructor
public class ReservationTimeRepositoryImpl implements ReservationTimeCommandRepository, ReservationTimeQueryRepository {

    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime);
    }

    @Override
    public void deleteById(final Long id) {
        jpaReservationTimeRepository.deleteById(id);
    }

    @Override
    public ReservationTime getByIdOrThrow(final Long id) {
        return jpaReservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다."));
    }

    @Override
    public List<ReservationTime> findAllByStartAt(final LocalTime startAt) {
        return jpaReservationTimeRepository.findAllByStartAt(startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }
}
