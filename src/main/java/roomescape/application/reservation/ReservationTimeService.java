package roomescape.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.presentation.reservation.request.TimeCreateRequest;
import roomescape.presentation.reservation.response.TimeCreateResponse;
import roomescape.presentation.reservation.response.ReservationTimesResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public ReservationTimesResponse getAllReservationTime() {
        return ReservationTimesResponse.from(reservationTimeRepository.findAll());
    }

    @Transactional
    public TimeCreateResponse createReservationTime(TimeCreateRequest request) {
        ReservationTime reservationTime = ReservationTime.create(request.startAt());
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);
        return TimeCreateResponse.from(savedTime);
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        if (reservationSlotRepository.existsByTimeId(id)) {
            throw new BusinessException();
        }
        reservationTimeRepository.deleteById(id);
    }
}
