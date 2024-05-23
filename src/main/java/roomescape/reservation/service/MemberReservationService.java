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
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.specification.MemberReservationSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MemberReservationService {

    private final CommonFindService commonFindService;
    private final MemberReservationRepository memberReservationRepository;

    public MemberReservationService(CommonFindService commonFindService, MemberReservationRepository memberReservationRepository) {
        this.commonFindService = commonFindService;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        Specification<MemberReservation> spec = Specification.where(MemberReservationSpecification.greaterThanOrEqualToStartDate(request.getStartDate()))
                .and(MemberReservationSpecification.lessThanOrEqualToEndDate(request.getEndDate()))
                .and(MemberReservationSpecification.equalMemberId(request.getMemberId()))
                .and(MemberReservationSpecification.equalThemeId(request.getThemeId()));
        return memberReservationRepository.findAll(spec)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse createMemberReservation(AuthInfo authInfo, ReservationRequest reservationRequest) {
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
        ReservationTime reservationTime = commonFindService.getReservationTime(timeId);
        Theme theme = commonFindService.getTheme(themeId);
        Member member = commonFindService.getMember(memberId);
        Reservation reservation = commonFindService.getReservation(date, reservationTime, theme);
        ReservationStatus reservationStatus = ReservationStatus.BOOKED;

        validateMemberReservation(reservation, member);

        if (memberReservationRepository.existsByReservation(reservation)) {
            reservationStatus = ReservationStatus.WAITING;
        }

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation, LocalDateTime.now(), reservationStatus));
        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    private void validateMemberReservation(Reservation reservation, Member member) {
        if (reservation.isPast()) {
            throw new BadRequestException("올바르지 않는 데이터 요청입니다.");
        }
        if (memberReservationRepository.existsByReservationAndMember(reservation, member)) {
            throw new ForbiddenException("중복된 예약입니다.");
        }
    }

    public void deleteMemberReservation(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = commonFindService.getMemberReservation(memberReservationId);
        Member member = commonFindService.getMember(authInfo.getId());
        if (!member.isAdmin() && !memberReservation.isMember(member)) {
            throw new ForbiddenException("예약자가 아닙니다.");
        }
        memberReservationRepository.deleteById(memberReservationId);
    }
}
