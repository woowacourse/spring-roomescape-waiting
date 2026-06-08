package roomescape.time.service;

import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional
    public ReservationTimeResult save(ReservationTimeCommand command) {
        validateReservationTimeUniqueness(command.startAt());
        ReservationTime reservationTime = new ReservationTime(command.startAt());
        ReservationTime saved = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResult.from(saved);
    }

    private void validateReservationTimeUniqueness(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(new ReservationTime(startAt))) {
            throw new ConflictException(TimeErrorCode.DUPLICATE_TIME);
        }
    }

    public ReservationTime getById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TimeErrorCode.TIME_NOT_FOUND));
    }

    @Transactional
    public void deleteById(long id) {
        ReservationTime deleteTarget = getById(id);
        reservationTimeRepository.delete(deleteTarget);
    }
}
