package roomescape.reservation.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationSearchCondRequest;
import roomescape.reservation.dto.response.MemberReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public ReservationResponse save(
            ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember,
            Status status
    ) {
        Reservation reservation = createValidatedReservation(reservationSaveRequest, loginMember, status);
        validateMemberReservation(reservation);  // TODO: 중복 로직 개선
        validateReservationWithStatus(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.toResponse(savedReservation);
    }

    private Reservation createValidatedReservation(
            ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember,
            Status status
    ) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationSaveRequest.getTimeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(reservationSaveRequest.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return reservationSaveRequest.toReservation(member, theme, reservationTime, status);
    }

    private void validateMemberReservation(Reservation reservation) {
        Optional<Reservation> duplicatedReservation = reservationRepository.findByDateAndReservationTimeStartAtAndThemeAndMember(
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getTheme(),
                reservation.getMember()
        );

        duplicatedReservation.ifPresent(ReservationService::throwExceptionByStatus);
    }

    private void validateReservationWithStatus(Reservation reservation) {
        Status reservationStatus = reservation.getStatus();
        Optional<Reservation> savedReservation = reservationRepository.findFirstByDateAndReservationTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        if (reservationStatus.isSuccess() && savedReservation.isPresent()) {
            throw new IllegalArgumentException("중복된 예약이 있습니다. 예약 대기를 걸어주세요.");
        }
        if (reservationStatus.isWait() && savedReservation.isEmpty()) {
            throw new IllegalArgumentException("추가된 예약이 없습니다. 예약을 추가해 주세요.");
        }
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        return ReservationResponse.toResponse(reservation);
    }

    private static void throwExceptionByStatus(Reservation memberReservation) {
        Status status = memberReservation.getStatus();

        if (status.isWait()) {
            throw new IllegalArgumentException("이미 회원이 예약 대기한 내역이 있습니다.");
        }
        if (status.isSuccess()) {
            throw new IllegalArgumentException("이미 회원이 예약한 내역이 있습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    public List<MemberReservationResponse> findMemberReservations(LoginMember loginMember) { // TODO : 쿼리 성능 개선
        List<Reservation> reservations = reservationRepository.findAllByMemberId(loginMember.id());
        List<WaitingWithRank> waitingWithRanks = reservationRepository.findWaitingWithRanksByMemberId(loginMember.id());

        return reservations.stream()
                .map(reservation -> createMemberReservationResponse(reservation, waitingWithRanks))
                .toList();
    }

    private MemberReservationResponse createMemberReservationResponse(
            Reservation reservation,
            List<WaitingWithRank> waitingWithRanks
    ) {
        if (reservation.getStatus().isWait()) {
            WaitingWithRank waitingWithRank = waitingWithRanks.stream()
                    .filter(waiting -> Objects.equals(waiting.getWaiting().getId(), reservation.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("예약 예약 내역이 없습니다."));

            return MemberReservationResponse.toWaitResponse(waitingWithRank);
        }
        return MemberReservationResponse.toResponse(reservation);
    }

    public List<WaitingResponse> findWaitingReservations() {
        return reservationRepository.findAllByStatus(Status.WAIT)
                .stream()
                .map(WaitingResponse::toResponse)
                .toList();
    }

    public List<ReservationResponse> findAllBySearchCond(ReservationSearchCondRequest request) {
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                        request.themeId(),
                        request.memberId(),
                        request.dateFrom(),
                        request.dateTo()
                ).stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void updateSuccess(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 내역이 없습니다."));
        validateExistReservation(reservation);
        reservation.setStatus(Status.SUCCESS);
    }

    private void validateExistReservation(Reservation reservation) {
        boolean existReservation = reservationRepository.existsByDateAndReservationTimeStartAtAndThemeAndStatus(
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getTheme(),
                Status.SUCCESS
        );
        if (existReservation) {
            throw new IllegalArgumentException("이미 확정된 예약이 있습니다.");
        }
    }
}
