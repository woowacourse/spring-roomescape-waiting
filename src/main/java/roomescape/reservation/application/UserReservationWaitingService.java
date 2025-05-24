package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.request.CreateReservationWaitingServiceRequest;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.service.ReservationWaitingOperation;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationWaitingOperation reservationWaitingOperation;

    @Transactional
    public void create(CreateReservationWaitingServiceRequest request) {
        reservationWaitingOperation.waiting(request.toSchedule(), request.memberId());
    }

    @Transactional
    public void cancel(Long reservationWaitingId, Long memberId) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.getById(reservationWaitingId);
        reservationWaiting.checkOwner(memberId);
        reservationWaiting.changeToCancel();
    }
}
