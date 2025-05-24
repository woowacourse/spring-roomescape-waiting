package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.AuthorizationException;
import roomescape.reservation.application.dto.request.CreateReservationWaitingServiceRequest;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.service.ReservationWaitingOperation;

@Service
@RequiredArgsConstructor
public class UserReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationWaitingOperation reservationWaitingOperation;

    public void create(CreateReservationWaitingServiceRequest request) {
        reservationWaitingOperation.waiting(request.date(), request.themeId(), request.themeId(), request.memberId());
    }

    public void cancel(Long reservationWaitingId, Long memberId) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.getById(reservationWaitingId);
        if (reservationWaiting.hasNotEqualsMemberId(memberId)) {
            throw new AuthorizationException("해당 웨이팅을 취소할 권한이 없습니다.");
        }
        reservationWaiting.changeToCancel();
    }
}
