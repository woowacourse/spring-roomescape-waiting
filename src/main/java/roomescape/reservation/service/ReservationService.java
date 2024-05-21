package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
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

    private MemberReservation findMemberReservationById(Long id) {
        return memberReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberReservationResponse createReservation(MemberReservationCreateRequest request, LoginMember loginMember) {
        ReservationCreateRequest createRequest = ReservationCreateRequest.of(request, loginMember);
        return createReservation(createRequest);
    }

    @Transactional(rollbackFor = Exception.class)
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

        return memberReservationRepository.findByMemberIdAndReservations(memberId, reservations).stream()
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

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> readWaitingReservations() {
        return memberReservationRepository.findByStatus(ReservationStatus.WAITING).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmWaitingReservation(Long id) {
        MemberReservation memberReservation = findMemberReservationById(id);
        memberReservation.validateWaitingReservation();

        Reservation reservation = memberReservation.getReservation();
        validateConfirmReservationExists(reservation);
        validateRankCanConfirm(reservation, memberReservation);

        memberReservation.setStatus(ReservationStatus.CONFIRMATION);
    }

    private void validateConfirmReservationExists(Reservation reservation) {
        memberReservationRepository.findByReservationAndStatus(reservation, ReservationStatus.CONFIRMATION)
                .ifPresent((confirmReservation) -> {
                    throw new BadRequestException("이미 예약이 존재해 대기를 승인할 수 없습니다.");
                });
    }

    private void validateRankCanConfirm(Reservation reservation, MemberReservation memberReservation) {
        Long waitingRank = memberReservationRepository.countByReservationAndCreatedAtBefore(
                reservation, memberReservation.getCreatedAt()
        );
        memberReservation.validateRankCanConfirm(waitingRank);
    }
}
