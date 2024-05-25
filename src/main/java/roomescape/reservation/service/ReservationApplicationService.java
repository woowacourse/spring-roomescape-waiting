package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.service.dto.MemberReservationCreate;
import roomescape.reservation.service.dto.MyReservationInfo;
import roomescape.reservation.service.dto.WaitingCreate;
import roomescape.reservation.service.services.MemberReservationService;
import roomescape.reservation.service.services.ReservationCommonService;
import roomescape.reservation.service.services.WaitingReservationService;

@Service
@Transactional(readOnly = true)
public class ReservationApplicationService {

    private final ReservationCommonService reservationCommonService;

    private final MemberReservationService memberReservationService;

    private final WaitingReservationService waitingReservationService;

    public ReservationApplicationService(ReservationCommonService reservationCommonService,
                                         MemberReservationService memberReservationService,
                                         WaitingReservationService waitingReservationService) {
        this.reservationCommonService = reservationCommonService;
        this.memberReservationService = memberReservationService;
        this.waitingReservationService = waitingReservationService;
    }

    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        return memberReservationService.findMemberReservations(request);
    }

    public List<MyReservationInfo> findMyReservations(AuthInfo authInfo) {
        Member member = reservationCommonService.getMember(authInfo.getId());
        return memberReservationService.findMyReservations(member);
    }

    @Transactional
    public ReservationResponse createMemberReservation(MemberReservationCreate memberReservationCreate) {
        ReservationTime reservationTime = reservationCommonService.getReservationTime(memberReservationCreate.timeId());
        Theme theme = reservationCommonService.getTheme(memberReservationCreate.themeId());
        Member member = reservationCommonService.getMember(memberReservationCreate.memberId());
        Reservation reservation = reservationCommonService.getReservation(memberReservationCreate.date(),
                reservationTime, theme);

        reservationCommonService.validatePastReservation(reservation);
        reservationCommonService.validateDuplicatedReservation(reservation, member);

        return createReservation(reservation, member);
    }

    private ReservationResponse createReservation(Reservation reservation, Member member) {
        if (reservationCommonService.isReservationConfirmed(reservation)) {
            MemberReservation waiting = waitingReservationService.addWaiting(member, reservation);
            return ReservationResponse.from(waiting.getId(), reservation, member);
        }
        MemberReservation memberReservation = memberReservationService.createMemberReservation(member, reservation);
        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    @Transactional
    public void deleteMemberReservation(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);
        Member member = reservationCommonService.getMember(authInfo.getId());
        reservationCommonService.delete(member, memberReservation);
        memberReservationService.updateStatus(memberReservation, ReservationStatus.PENDING, ReservationStatus.APPROVED);
    }

    @Transactional
    public void delete(long reservationId) {
        memberReservationService.delete(reservationId);
    }

    public List<ReservationResponse> getWaiting() {
        return waitingReservationService.getWaiting();
    }

    @Transactional
    public ReservationResponse addWaiting(WaitingCreate waitingCreate) {
        ReservationTime reservationTime = reservationCommonService.getReservationTime(waitingCreate.timeId());
        Theme theme = reservationCommonService.getTheme(waitingCreate.themeId());
        Member member = reservationCommonService.getMember(waitingCreate.memberId());
        Reservation reservation = reservationCommonService.getReservation(waitingCreate.date(), reservationTime, theme);

        reservationCommonService.validatePastReservation(reservation);
        reservationCommonService.validateDuplicatedReservation(reservation, member);

        MemberReservation memberReservation = waitingReservationService.addWaiting(member, reservation);
        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    @Transactional
    public void deleteWaiting(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);
        Member member = reservationCommonService.getMember(authInfo.getId());
        waitingReservationService.validateWaitingReservation(memberReservation);
        reservationCommonService.delete(member, memberReservation);
    }

    @Transactional
    public void approveWaiting(AuthInfo authInfo, long memberReservationId) {
        Member member = reservationCommonService.getMember(authInfo.getId());
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);
        waitingReservationService.approveWaiting(member, memberReservation);
    }

    @Transactional
    public void denyWaiting(AuthInfo authInfo, long memberReservationId) {
        Member member = reservationCommonService.getMember(authInfo.getId());
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);
        waitingReservationService.denyWaiting(member, memberReservation);
    }
}

