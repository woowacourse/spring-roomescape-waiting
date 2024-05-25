package roomescape.reservation.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.custom.BadRequestException;
import roomescape.exception.custom.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.MemberReservationRequest;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.*;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.specification.MemberReservationSpecification;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MemberReservationService {

    private final CommonFindService commonFindService;
    private final ReservationRepository reservationRepository;

    public MemberReservationService(CommonFindService commonFindService,
                                    ReservationRepository reservationRepository) {
        this.commonFindService = commonFindService;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        Specification<Reservation> spec = Specification
                .where(MemberReservationSpecification.greaterThanOrEqualToStartDate(request.getStartDate()))
                .and(MemberReservationSpecification.lessThanOrEqualToEndDate(request.getEndDate()))
                .and(MemberReservationSpecification.equalMemberId(request.getMemberId()))
                .and(MemberReservationSpecification.equalThemeId(request.getThemeId()));
        return reservationRepository.findAll(spec)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse createReservation(AuthInfo authInfo, ReservationRequest reservationRequest) {
        LocalDate date = LocalDate.parse(reservationRequest.date());
        return createMemberReservation(
                authInfo.getId(),
                reservationRequest.timeId(),
                reservationRequest.themeId(),
                date
        );
    }

    public ReservationResponse createMemberReservation(MemberReservationRequest memberReservationRequest) {
        LocalDate date = LocalDate.parse(memberReservationRequest.date());
        return createMemberReservation(
                memberReservationRequest.memberId(),
                memberReservationRequest.timeId(),
                memberReservationRequest.themeId(),
                date
        );
    }

    private ReservationResponse createMemberReservation(long memberId, long timeId, long themeId, LocalDate date) {
        ReservationTime reservationTime = commonFindService.getReservationSlotTime(timeId);
        Theme theme = commonFindService.getTheme(themeId);
        Member member = commonFindService.getMember(memberId);
        ReservationSlot reservationSlot = commonFindService.getReservationSlot(date, reservationTime, theme);
        ReservationStatus reservationStatus = ReservationStatus.BOOKED;

        validateMemberReservation(reservationSlot, member);

        if (reservationRepository.existsByReservationSlot(reservationSlot)) {
            reservationStatus = ReservationStatus.WAITING;
        }

        Reservation memberReservation = reservationRepository.save(
                new Reservation(member, reservationSlot, reservationStatus));
        return ReservationResponse.from(memberReservation.getId(), reservationSlot, member);
    }

    private void validateMemberReservation(ReservationSlot reservationSlot, Member member) {
        if (reservationSlot.isPast()) {
            throw new BadRequestException("올바르지 않는 데이터 요청입니다.");
        }
        if (reservationRepository.existsByReservationSlotAndMember(reservationSlot, member)) {
            throw new ForbiddenException("중복된 예약입니다.");
        }
    }

    public void deleteMemberReservation(AuthInfo authInfo, long memberReservationId) {
        Reservation memberReservation = commonFindService.getMemberReservation(memberReservationId);
        Member member = commonFindService.getMember(authInfo.getId());
        if (!member.isAdmin() && !memberReservation.isMember(member)) {
            throw new ForbiddenException("예약자가 아닙니다.");
        }
        reservationRepository.deleteById(memberReservationId);
    }
}
