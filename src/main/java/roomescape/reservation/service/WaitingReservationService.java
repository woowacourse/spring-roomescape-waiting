package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.AuthorizationException;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.*;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.service.dto.WaitingCreate;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingReservationService {

    private final MemberReservationRepository memberReservationRepository;
    private final ReservationCommonService reservationCommonService;

    public WaitingReservationService(MemberReservationRepository memberReservationRepository,
                                     ReservationCommonService reservationCommonService) {
        this.memberReservationRepository = memberReservationRepository;
        this.reservationCommonService = reservationCommonService;
    }

    public List<ReservationResponse> getWaiting() {
        return memberReservationRepository.findAllByReservationStatus(ReservationStatus.PENDING)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse addWaiting(WaitingCreate waitingCreate) {
        ReservationTime reservationTime = reservationCommonService.getReservationTime(waitingCreate.timeId());
        Theme theme = reservationCommonService.getTheme(waitingCreate.themeId());
        Member member = reservationCommonService.getMember(waitingCreate.memberId());
        Reservation reservation = reservationCommonService.getReservation(waitingCreate.date(), reservationTime, theme);

        reservationCommonService.validatePastReservation(reservation);
        reservationCommonService.validateDuplicatedReservation(reservation, member);

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation, ReservationStatus.PENDING));

        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    @Transactional
    public void deleteWaiting(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);
        Member member = reservationCommonService.getMember(authInfo.getId());
        validateWaitingReservation(memberReservation);
        reservationCommonService.delete(member, memberReservation);
    }

    @Transactional
    public void approveWaiting(AuthInfo authInfo, long memberReservationId) {
        Member member = reservationCommonService.getMember(authInfo.getId());
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);

        validateAdminPermission(member);
        validateWaitingReservation(memberReservation);
        memberReservation.approve();
    }

    @Transactional
    public void denyWaiting(AuthInfo authInfo, long memberReservationId) {
        Member member = reservationCommonService.getMember(authInfo.getId());
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);

        validateAdminPermission(member);
        validateWaitingReservation(memberReservation);
        memberReservation.deny();
    }

    private void validateWaitingReservation(MemberReservation memberReservation) {
        if (!memberReservation.isPending()) {
            throw new BadRequestException(ErrorType.NOT_A_WAITING_RESERVATION);
        }
    }

    private static void validateAdminPermission(Member member) {
        if (!member.isAdmin()) {
            throw new AuthorizationException(ErrorType.NOT_ALLOWED_PERMISSION_ERROR);
        }
    }
}
