package roomescape.reservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.custom.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.MyReservationWithStatus;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.MemberReservationRepository;

import java.util.List;

@Service
public class WaitingReservationService {

    @Autowired
    private final CommonFindService commonFindService;
    private final MemberReservationRepository memberReservationRepository;

    public WaitingReservationService(CommonFindService commonFindService,
                                     MemberReservationRepository memberReservationRepository) {
        this.commonFindService = commonFindService;
        this.memberReservationRepository = memberReservationRepository;
    }

    public List<MyReservationResponse> handleWaitingOrder(List<MyReservationWithStatus> myReservationWithStatuses) {
        return myReservationWithStatuses
                .stream()
                .map(this::handler)
                .toList();
    }

    private MyReservationResponse handler(MyReservationWithStatus myReservationWithStatus) {
        if (myReservationWithStatus.status().isWaiting()) {
            int waitingCount = memberReservationRepository
                    .countWaitingMemberReservation(myReservationWithStatus.memberReservationId());
            return MyReservationResponse.from(myReservationWithStatus, waitingCount);
        }
        return MyReservationResponse.from(myReservationWithStatus);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByWaitingReservation() {
        List<MemberReservation> memberReservations = memberReservationRepository
                .findAllByStatus(ReservationStatus.WAITING);
        return memberReservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteMemberReservation(AuthInfo authInfo, Long memberReservationId) {
        MemberReservation memberReservation = commonFindService.getMemberReservation(memberReservationId);
        Member member = commonFindService.getMember(authInfo.getId());
        if (!member.isAdmin() && !memberReservation.isMember(member)) {
            throw new ForbiddenException("예약자가 아닙니다.");
        }
        memberReservationRepository.deleteById(memberReservationId);
        memberReservationRepository.findFirstByReservationOrderByCreatedTime(memberReservation.getReservation())
                        .ifPresent(MemberReservation::confirmReservation);
    }
}
