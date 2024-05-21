package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.WaitingReservationRanking;
import roomescape.reservation.dto.*;
import roomescape.reservation.repository.MemberReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository, MemberReservationRepository memberReservationRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));
    }

    private ReservationTime findReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 테마입니다."));
    }

    public MemberReservationResponse createReservation(MemberReservationCreateRequest request, LoginMember loginMember) {
        ReservationCreateRequest createRequest = ReservationCreateRequest.of(request, loginMember);
        return createReservation(createRequest);
    }

    public MemberReservationResponse createReservation(ReservationCreateRequest request) {
        Member member = findMemberById(request.memberId());
        Reservation reservation = findReservationOrSave(request);

        MemberReservation memberReservation = request.toMemberReservation(member, reservation);
        reservation.validateIsBeforeNow();
        validateDuplicated(memberReservation);

        MemberReservation savedMemberReservation = memberReservationRepository.save(memberReservation);
        return MemberReservationResponse.from(savedMemberReservation);
    }

    private Reservation findReservationOrSave(ReservationCreateRequest request) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId())
                .orElseGet(() -> {
                    ReservationTime time = findReservationTimeById(request.timeId());
                    Theme theme = findThemeById(request.themeId());
                    return reservationRepository.save(new Reservation(request.date(), time, theme));
                });
    }

    private void validateDuplicated(MemberReservation memberReservation) {
        if (memberReservation.getStatus().isNotWaiting()) {
            memberReservationRepository.findByReservationAndStatus(
                            memberReservation.getReservation(),
                            ReservationStatus.CONFIRMATION)
                    .ifPresent(memberReservation::validateDuplicated);
            return;
        }

        memberReservationRepository.findByReservationAndMember(
                        memberReservation.getReservation(),
                        memberReservation.getMember())
                .ifPresent(memberReservation::validateDuplicated);
    }

    public List<MemberReservationResponse> readReservations() {
        return memberReservationRepository.findAll().stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> readMemberReservations(LoginMember loginMember) {
        List<MemberReservation> confirmationReservation = memberReservationRepository
                .findByMemberIdAndStatus(loginMember.id(), ReservationStatus.CONFIRMATION);
        List<WaitingReservationRanking> waitingReservation = memberReservationRepository.
                findWaitingReservationRankingByMemberId(loginMember.id());

        return Stream.concat(
                        confirmationReservation.stream().map(MyReservationResponse::from),
                        waitingReservation.stream().map(MyReservationResponse::from)
                ).sorted(Comparator.comparing(MyReservationResponse::date)
                        .thenComparing(MyReservationResponse::time))
                .toList();
    }

    public List<MemberReservationResponse> searchReservations(LocalDate start, LocalDate end, Long memberId, Long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateBetweenAndThemeId(start, end, themeId);

        return memberReservationRepository.findByMemberIdAndReservations(memberId, reservations).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    public MemberReservationResponse readReservation(Long id) {
        MemberReservation memberReservation = memberReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
        return MemberReservationResponse.from(memberReservation);
    }

    public void deleteReservation(Long id) {
        memberReservationRepository.deleteById(id);
    }

    public List<MemberReservationResponse> readWaitingReservations() {
        return memberReservationRepository.findByStatus(ReservationStatus.WAITING).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
