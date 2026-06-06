package roomescape.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.presentation.reservation.request.TimeCreateRequest;
import roomescape.presentation.reservation.response.ReservationTimesResponse;
import roomescape.presentation.reservation.response.TimeCreateResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationSlotRepository slotRepository;

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
        if (slotRepository.existsByTimeId(timeId)) {
            throw new BusinessException(ErrorCode.RESERVATION_TIME_IN_USE);
        }

        int deletedRow = timeRepository.deleteById(timeId);
        if (deletedRow == 0) {
            throw new BusinessException(ErrorCode.RESERVATION_TIME_NOT_FOUND);
        }
    }
}
