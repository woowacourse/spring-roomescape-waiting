package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
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

    @Transactional
    public ReservationResponse createReservation(MemberReservationCreateRequest request, LoginMember loginMember) {
        ReservationCreateRequest createRequest = ReservationCreateRequest.of(request, loginMember);
        return createReservation(createRequest);
    }

    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Member member = findMemberById(request.memberId());
        ReservationTime time = findReservationTimeById(request.timeId());
        Theme theme = findThemeById(request.themeId());

        Reservation reservation = request.toReservation(member, time, theme);
        validateDuplicatedReservation(reservation);
        validateRequestedTime(reservation, time);

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    @Transactional
    public WaitingResponse createWaitingReservation(MemberReservationCreateRequest request, LoginMember loginMember) {
        ReservationCreateRequest createRequest = ReservationCreateRequest.of(request, loginMember);
        return createWaitingReservation(createRequest);
    }

    @Transactional
    public WaitingResponse createWaitingReservation(ReservationCreateRequest request) {
        Member member = findMemberById(request.memberId());
        ReservationTime time = findReservationTimeById(request.timeId());
        Theme theme = findThemeById(request.themeId());

        Reservation reservation = request.toWaitingReservation(member, time, theme);

        validateRequestedTime(reservation, time);
        validateDuplicatedWaiting(reservation);
        validateNoReservation(reservation);

        Reservation savedWaitingReservation = reservationRepository.save(reservation);
        Long sequence = findSequence(savedWaitingReservation);

        return WaitingResponse.of(savedWaitingReservation, sequence);
    }

    @Transactional
    public ReservationResponse approveWaiting(Long id) {
        Reservation waitingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
        if (waitingReservation.getStatus() != ReservationStatus.WAITING) {
            throw new UnauthorizedException("예약 대기만 승인할 수 있습니다");
        }

        validateDuplicatedReservation(waitingReservation);
        waitingReservation.setStatus(ReservationStatus.CONFIRMATION);
        Reservation reservation = reservationRepository.save(waitingReservation);

        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> readReservations() {
        return reservationRepository.findAllFetchJoin().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> readMemberReservations(LoginMember loginMember) {
        return reservationRepository.findByMemberId(loginMember.id()).stream()
                .map(reservation -> MemberReservationResponse.of(reservation, findSequence(reservation)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> searchReservations(LocalDate start, LocalDate end, Long memberId, Long themeId) {
        return reservationRepository.findByDateBetweenAndMemberIdAndThemeId(start, end, memberId, themeId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse readReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
        return ReservationResponse.from(reservation);
    }

    public List<WaitingResponse> readWaitings() {
        return reservationRepository.findAllByStatusFetchJoin(ReservationStatus.WAITING).stream()
                .map(waiting -> WaitingResponse.of(waiting, findSequence(waiting)))
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteWaitingReservation(Long id, LoginMember loginMember) {
        Reservation waitingReservation = reservationRepository.findByIdAndStatus(id, ReservationStatus.WAITING)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
        Member member = memberRepository.findByEmail(loginMember.email())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 멤버입니다."));

        if (!waitingReservation.isOwnedBy(member)) {
            throw new UnauthorizedException("삭제할 수 없는 예약입니다");
        }

        deleteReservation(id);
    }

    @Transactional
    public void deleteWaiting(Long id) {
        Reservation waitingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
        if (waitingReservation.getStatus() != ReservationStatus.WAITING) {
            throw new UnauthorizedException("예약 대기만 삭제할 수 있습니다");
        }

        deleteReservation(id);
    }

    private void validateDuplicatedReservation(Reservation reservation) {
        Optional<Reservation> existsReservation = reservationRepository.findByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                ReservationStatus.CONFIRMATION
        );

        existsReservation.ifPresent((duplicated) -> {
            if (duplicated.isSameMember(reservation)) {
                throw new BadRequestException("중복된 예약입니다.");
            }
            throw new BadRequestException("이미 예약된 테마입니다.");
        });
    }

    private void validateNoReservation(Reservation waitingReservation) {
        Optional<Reservation> existsReservation = reservationRepository.findByDateAndTimeIdAndThemeIdAndStatus(
                waitingReservation.getDate(),
                waitingReservation.getTime().getId(),
                waitingReservation.getTheme().getId(),
                ReservationStatus.CONFIRMATION
        );
        if (existsReservation.isEmpty()) {
            throw new BadRequestException("예약 내역이 없습니다.");
        }
    }

    private void validateDuplicatedWaiting(Reservation waitingReservation) {
        Optional<Reservation> existsReservation = reservationRepository.findByDateAndTimeIdAndThemeIdAndStatus(
                waitingReservation.getDate(),
                waitingReservation.getTime().getId(),
                waitingReservation.getTheme().getId(),
                ReservationStatus.WAITING
        );

        existsReservation.ifPresent((duplicated) -> {
            if (duplicated.isSameMember(waitingReservation)) {
                throw new BadRequestException("중복된 예약 대기입니다.");
            }
        });
    }

    private void validateRequestedTime(Reservation reservation, ReservationTime reservationTime) {
        LocalDateTime requestedDateTime = LocalDateTime.of(reservation.getDate(), reservationTime.getStartAt());
        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("이미 지난 날짜는 예약할 수 없습니다.");
        }
    }

    private Long findSequence(Reservation reservation) {
        List<Reservation> reservations = reservationRepository.findByDateAndThemeAndTimeOrderByCreatedAt(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getTime()
        );

        return (long) reservations.indexOf(reservation);
    }
}
