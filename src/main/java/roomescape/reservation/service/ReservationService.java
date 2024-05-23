package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.custom.BadRequestException;
import roomescape.exception.custom.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.domain.*;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service // TODO 분리 필요
public class ReservationService {

    private final CommonFindService commonFindService;
    private final ReservationRepository reservationRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(CommonFindService commonFindService, ReservationRepository reservationRepository, MemberReservationRepository memberReservationRepository) {
        this.commonFindService = commonFindService;
        this.reservationRepository = reservationRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        return memberReservationRepository.findBy(request.getMemberId(), request.getThemeId(), request.getStartDate(),
                        request.getEndDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public List<MyReservationWithStatus> findMyReservations(AuthInfo authInfo) {
        Member member = commonFindService.getMember(authInfo.getId());
        return memberReservationRepository.findAllByMember(member)
                .stream()
                .map(MyReservationWithStatus::from)
                .toList();
    }

    @Transactional
    public ReservationResponse createMemberReservation(AuthInfo authInfo, ReservationRequest reservationRequest) {
        LocalDate date = LocalDate.parse(reservationRequest.date());
        return createMemberReservation(
                authInfo.getId(),
                reservationRequest.timeId(),
                reservationRequest.themeId(),
                date
        );
    }

    @Transactional
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

        validateReservation(reservation, member);

        if (memberReservationRepository.existsByReservation(reservation)) {
            reservationStatus = ReservationStatus.WAITING;
        }

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation, LocalDateTime.now(), reservationStatus));
        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    private void validateReservation(Reservation reservation, Member member) {
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

    @Transactional
    public void delete(long reservationId) {
        memberReservationRepository.deleteByReservation_Id(reservationId);
        reservationRepository.deleteById(reservationId);
    }
}
