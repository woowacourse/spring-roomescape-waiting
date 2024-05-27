package roomescape.reservation.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.domain.entity.MemberReservation;
import roomescape.reservation.domain.entity.Reservation;
import roomescape.reservation.domain.entity.ReservationStatus;
import roomescape.reservation.domain.dto.WaitingReservationRanking;
import roomescape.reservation.dto.*;
import roomescape.reservation.repository.MemberReservationRepository;
import roomescape.reservation.repository.ReservationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            MemberReservationRepository memberReservationRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    private MemberReservation findMemberReservationById(Long id) {
        return memberReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> readReservations() {
        return memberReservationRepository.findByStatus(ReservationStatus.CONFIRMATION).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> readMemberReservations(LoginMember loginMember) {
        List<MemberReservation> confirmationReservation = memberReservationRepository
                .findByMemberIdAndStatus(loginMember.id(), ReservationStatus.CONFIRMATION);
        List<WaitingReservationRanking> waitingReservation = memberReservationRepository.
                findWaitingReservationRankingByMemberId(loginMember.id());

        return Stream.concat(
                        confirmationReservation.stream().map(MyReservationResponse::from),
                        waitingReservation.stream().map(MyReservationResponse::from))
                .sorted(Comparator.comparing(MyReservationResponse::date)
                        .thenComparing(MyReservationResponse::time))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> searchReservations(ReservationSearchRequestParameter searchCondition) {

        List<Reservation> reservations = reservationRepository.findByDateBetweenAndThemeId(
                searchCondition.dateFrom(),
                searchCondition.dateTo(),
                searchCondition.themeId()
        );

        return memberReservationRepository.findByMemberIdAndReservationIn(searchCondition.memberId(), reservations).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberReservationResponse readReservation(Long id) {
        MemberReservation memberReservation = findMemberReservationById(id);
        return MemberReservationResponse.from(memberReservation);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReservation(Long id) {
        MemberReservation memberReservation = findMemberReservationById(id);
        memberReservationRepository.deleteById(id);

        confirmFirstWaitingReservation(memberReservation.getReservation());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReservation(Long id, LoginMember loginMember) {
        MemberReservation memberReservation = findMemberReservationById(id);
        memberReservation.validateIsOwner(loginMember);
        memberReservationRepository.deleteById(id);

        confirmFirstWaitingReservation(memberReservation.getReservation());
    }

    private void confirmFirstWaitingReservation(Reservation reservation) {
        memberReservationRepository.findFirstByReservationAndStatus(reservation, ReservationStatus.WAITING)
                .ifPresent((memberReservation) -> memberReservation.setStatus(ReservationStatus.CONFIRMATION));
    }
}
