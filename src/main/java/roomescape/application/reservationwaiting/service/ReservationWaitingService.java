package roomescape.application.reservationwaiting.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.reservationwaiting.service.dto.ReservationInfoAndWaitingInfo;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.WaitingInfo;

@Service
public class ReservationWaitingService {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationWaitingService(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public ReservationInfoAndWaitingInfo findMyReservationAndWaiting(long memberId) {
        List<ReservationInfo> reservationInfos = reservationService.findAllByMemberId(memberId);
        List<WaitingInfo> waitingInfos = waitingService.findAllByMemberId(memberId);
        return new ReservationInfoAndWaitingInfo(reservationInfos, waitingInfos);
    }
}
