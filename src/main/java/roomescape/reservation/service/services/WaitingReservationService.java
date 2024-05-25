package roomescape.reservation.service.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.AuthorizationException;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.MemberReservationRepository;

@Service
@Transactional(readOnly = true)
public class WaitingReservationService {

    private final MemberReservationRepository memberReservationRepository;

    public WaitingReservationService(MemberReservationRepository memberReservationRepository) {
        this.memberReservationRepository = memberReservationRepository;
    }

    private static void validateAdminPermission(Member member) {
        if (!member.isAdmin()) {
            throw new AuthorizationException(ErrorType.NOT_ALLOWED_PERMISSION_ERROR);
        }
    }

    public List<ReservationResponse> getWaiting() {
        return memberReservationRepository.findAllByReservationStatus(ReservationStatus.PENDING)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public MemberReservation addWaiting(Member member, Reservation reservation) {
        return memberReservationRepository.save(
                new MemberReservation(member, reservation, ReservationStatus.PENDING));
    }

    @Transactional
    public void approveWaiting(Member member, MemberReservation memberReservation) {
        validateAdminPermission(member);
        validateWaitingReservation(memberReservation);
        memberReservation.approve();
    }

    @Transactional
    public void denyWaiting(Member member, MemberReservation memberReservation) {
        validateAdminPermission(member);
        validateWaitingReservation(memberReservation);
        memberReservation.deny();
    }

    public void validateWaitingReservation(MemberReservation memberReservation) {
        if (!memberReservation.isPending()) {
            throw new BadRequestException(ErrorType.NOT_A_WAITING_RESERVATION);
        }
    }
}
