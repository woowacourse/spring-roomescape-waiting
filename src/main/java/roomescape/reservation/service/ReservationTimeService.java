package roomescape.reservation.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public Long save(final LocalTime startAt) {
        final List<ReservationTime> foundReservationTimes = reservationTimeRepository.findAllByStartAt(startAt);
        if (!foundReservationTimes.isEmpty()) {
            throw new DataExistException("해당 예약 시간이 이미 존재합니다. startAt = " + startAt);
        }

        final ReservationTime reservationTimeEntity = new ReservationTime(startAt);

        return reservationTimeRepository.save(reservationTimeEntity);
    }

    public void deleteById(final Long id) {
        final Optional<ReservationTime> found = reservationTimeRepository.findById(id);

        if (found.isEmpty()) {
            throw new DataNotFoundException("해당 예약 시간 데이터가 존재하지 않습니다. id = " + id);
        }

        reservationTimeRepository.deleteById(id);
    }

    public ReservationTime getById(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 시간 데이터가 존재하지 않습니다. id = " + id));
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }
}
