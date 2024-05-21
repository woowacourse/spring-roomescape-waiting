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
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.service.dto.MemberReservationCreate;
import roomescape.reservation.service.dto.MyReservationInfo;
import roomescape.waiting.service.WaitingHistoryService;

@Service
@Transactional(readOnly = true)
public class MemberReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final ReservationCommonService reservationCommonService;
    private final WaitingHistoryService waitingHistoryService;

    public MemberReservationService(ReservationRepository reservationRepository,
                                    MemberReservationRepository memberReservationRepository,
                                    ReservationCommonService reservationCommonService,
                                    WaitingHistoryService waitingHistoryService) {
        this.reservationRepository = reservationRepository;
        this.memberReservationRepository = memberReservationRepository;
        this.reservationCommonService = reservationCommonService;
        this.waitingHistoryService = waitingHistoryService;
    }


    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        return memberReservationRepository.findBy(
                        request.getMemberId(),
                        request.getThemeId(),
                        ReservationStatus.APPROVED,
                        request.getStartDate(),
                        request.getEndDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationInfo> findMyReservations(AuthInfo authInfo) {
        Member member = reservationCommonService.getMember(authInfo.getId());
        return memberReservationRepository.findByMember(member.getId())
                .stream()
                .map(MyReservationInfo::of)
                .toList();
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

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation, ReservationStatus.APPROVED));
        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    @Transactional
    public void deleteMemberReservation(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = reservationCommonService.getMemberReservation(memberReservationId);
        Member member = reservationCommonService.getMember(authInfo.getId());
        reservationCommonService.delete(member, memberReservation);
        waitingHistoryService.createHistory(memberReservation.getReservation());
    }

    @Transactional
    public void delete(long reservationId) {
        memberReservationRepository.deleteByReservationId(reservationId);
        reservationRepository.deleteById(reservationId);
    }
}
