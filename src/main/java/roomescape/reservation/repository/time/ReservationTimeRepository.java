package roomescape.reservation.repository.time;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DataNotFoundException;
import roomescape.reservation.domain.ReservationTime;

@RequiredArgsConstructor
@Repository
public class ReservationTimeRepository implements ReservationTimeRepositoryInterface {

    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime);
    }

    @Override
    public ReservationTime findById(Long id) {
        return jpaReservationTimeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간 데이터가 존재하지 않습니다. id = " + id));
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return jpaReservationTimeRepository.existsByStartAt(startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationTimeRepository.deleteById(id);
    }
}
