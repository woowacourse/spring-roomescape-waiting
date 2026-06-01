package roomescape.time.service;

import java.time.LocalTime;
import org.springframework.dao.DataIntegrityViolationException;
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
        ReservationTime reservationTime = ReservationTime.of(command.startAt());

        try {
            ReservationTime saved = reservationTimeRepository.save(reservationTime);
            return ReservationTimeResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(TimeErrorCode.DUPLICATE_TIME.getMessage());
        }
    }

    private void validateReservationTimeUniqueness(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new ConflictException(TimeErrorCode.DUPLICATE_TIME.getMessage());
        }
    }

    public ReservationTime getById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage()));
    }

    @Transactional
    public void deleteById(Long id) {
        ReservationTime deleteTarget = getById(id);

        try {
            reservationTimeRepository.delete(deleteTarget);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(TimeErrorCode.TIME_IN_USE.getMessage());
        }
    }
}
