package roomescape.time.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        ReservationTime reservationTime = new ReservationTime(command.startAt());
        ReservationTime saved = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResult.from(saved);
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
