package roomescape.application.reservationwaiting.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservationwaiting.service.dto.ReservationInfoAndWaitingInfo;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    public ReservationWaitingService(
            final ReservationService reservationService,
            final WaitingService waitingService,
            final MemberRepository memberRepository
    ) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
        this.memberRepository = memberRepository;
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
            throw new IllegalArgumentException("해당 예약에 대한 취소 권한이 없습니다.");
        }
        reservationService.cancel(reservation);
        waitingService.pullPriority(reservation.getTheme(), reservation.getDate(), reservation.getTime(), 1, 1);
        makeReservationWithFirstWaiting(reservation);
    }

    private void makeReservationWithFirstWaiting(final Reservation reservation) {
        Optional<Waiting> waitingOptional = waitingService.popFirstWaiting(reservation.getTheme(), reservation.getDate(),
                reservation.getTime());
        if (waitingOptional.isEmpty()) {
            return;
        }
        Waiting waiting = waitingOptional.get();
        Member member = getMember(waiting.getMember().getId());
        ReservationCreateFromWaitingCommand command = new ReservationCreateFromWaitingCommand(waiting, member);
        reservationService.createReservationFromWaiting(command);
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));
    }
}
