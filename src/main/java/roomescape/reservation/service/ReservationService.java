package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.WaitingReservationRanking;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.repository.MemberReservationRepository;
import roomescape.reservation.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final ReservationCreateService reservationCreateService;

    public ReservationService(
            ReservationRepository reservationRepository,
            MemberReservationRepository memberReservationRepository,
            ReservationCreateService reservationCreateService) {
        this.reservationRepository = reservationRepository;
        this.memberReservationRepository = memberReservationRepository;
        this.reservationCreateService = reservationCreateService;
    }

    private MemberReservation findMemberReservationById(Long id) {
        return memberReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberReservationResponse createReservation(MemberReservationCreateRequest request, LoginMember loginMember) {
        ReservationCreateRequest createRequest = ReservationCreateRequest.of(request, loginMember);
        return reservationCreateService.createReservation(createRequest);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberReservationResponse createReservation(ReservationCreateRequest request) {
        return reservationCreateService.createReservation(request);
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
    public List<MemberReservationResponse> searchReservations(LocalDate start, LocalDate end, Long memberId, Long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateBetweenAndThemeId(start, end, themeId);

        return memberReservationRepository.findByMemberIdAndReservationIn(memberId, reservations).stream()
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
        memberReservationRepository.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReservation(Long id, LoginMember loginMember) {
        findMemberReservationById(id).validateIsOwner(loginMember);
        memberReservationRepository.deleteById(id);
    }
}
