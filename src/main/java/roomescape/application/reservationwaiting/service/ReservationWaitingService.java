package roomescape.application.reservationwaiting.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservationwaiting.service.dto.ReservationInfoAndWaitingInfo;
import roomescape.common.exception.NoPermissionException;
import roomescape.member.service.dto.LoginMemberInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCreateFromWaitingCommand;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.WaitingInfo;

@Transactional
@Service
public class ReservationWaitingService {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationWaitingService(final ReservationService reservationService, final WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public ReservationInfoAndWaitingInfo findMyReservationAndWaiting(long memberId) {
        List<ReservationInfo> reservationInfos = reservationService.findAllByMemberId(memberId);
        List<WaitingInfo> waitingInfos = waitingService.findAllByMemberId(memberId);
        return new ReservationInfoAndWaitingInfo(reservationInfos, waitingInfos);
    }

    public void cancelReservation(long reservationId, LoginMemberInfo loginMemberInfo) {
        Reservation reservation = reservationService.getReservation(reservationId);
        boolean isSameMember = reservation.isMemberHasSameId(loginMemberInfo.id());
        if (!isSameMember && !loginMemberInfo.isAdmin()) {
            throw new NoPermissionException("해당 예약에 대한 취소 권한이 없습니다.");
        }
        reservationService.cancel(reservation);
        waitingService.pullPriority(reservation.getTheme(), reservation.getDate(), reservation.getTime(), 1, 1);
        makeReservationWithFirstWaiting(reservation);
    }

    private void makeReservationWithFirstWaiting(final Reservation reservation) {
        Optional<Waiting> waitingOptional = waitingService.popFirstWaiting(reservation.getTheme(),
                reservation.getDate(), reservation.getTime());
        if (waitingOptional.isEmpty()) {
            return;
        }
        Waiting waiting = waitingOptional.get();
        ReservationCreateFromWaitingCommand command = new ReservationCreateFromWaitingCommand(waiting);
        reservationService.createReservationFromWaiting(command);
        waitingService.cancel(waiting);
    }
}
