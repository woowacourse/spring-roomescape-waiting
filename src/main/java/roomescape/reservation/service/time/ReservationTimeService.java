package roomescape.reservation.service.time;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DataExistException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepositoryInterface reservationTimeRepository;

    @Transactional
    public ReservationTime save(final LocalTime startAt) {
        validateExistReservationTime(startAt);

        final ReservationTime reservationTimeEntity = new ReservationTime(startAt);

        return reservationTimeRepository.save(reservationTimeEntity);
    }

    @Transactional
    public void deleteById(final Long id) {
        reservationTimeRepository.findById(id);

        reservationTimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    private void validateExistReservationTime(final LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new DataExistException("해당 예약 시간이 이미 존재합니다. startAt = " + startAt);
        }
    }
}
