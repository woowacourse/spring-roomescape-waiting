package roomescape.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.request.TimeCreateRequest;
import roomescape.application.reservation.response.ReservationTimesResponse;
import roomescape.application.reservation.response.TimeCreateResponse;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;

    public ReservationTimesResponse getAllReservationTime() {
        return ReservationTimesResponse.from(timeRepository.findAll());
    }

    @Transactional
    public TimeCreateResponse createReservationTime(TimeCreateRequest request) {
        ReservationTime reservationTime = ReservationTime.create(request.startAt());
        ReservationTime savedTime = timeRepository.save(reservationTime);
        return TimeCreateResponse.from(savedTime);
    }

    @Transactional
    public void deleteReservationTime(Long timeId) {
        try {
            int deletedRow = timeRepository.deleteById(timeId);
            if (deletedRow == 0) {
                throw new BusinessException(ErrorCode.RESERVATION_TIME_NOT_FOUND);
            }
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.RESERVATION_TIME_IN_USE);
        }
    }
}
