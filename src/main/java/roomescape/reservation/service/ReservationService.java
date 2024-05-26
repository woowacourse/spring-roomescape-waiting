package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSaveRequest;
import roomescape.reservation.dto.ReservationSearchConditionRequest;
import roomescape.reservation.dto.ReservationWaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional
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

    public ReservationResponse saveReservationSuccess(
            ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember
    ) {
        Reservation reservation = createValidatedReservationOfStatus(reservationSaveRequest, loginMember, ReservationStatus.SUCCESS);
        validateDuplicatedReservationSuccess(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.toResponse(savedReservation);
    }

    public ReservationResponse saveReservationWaiting(
            ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember
    ) {
        Reservation reservation = createValidatedReservationOfStatus(reservationSaveRequest, loginMember, ReservationStatus.WAIT);
        validateDuplicatedReservationWaiting(reservation, loginMember);
        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.toResponse(savedReservation);
    }

    private Reservation createValidatedReservationOfStatus(
            ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember,
            ReservationStatus reservationStatus
    ) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationSaveRequest.getTimeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(reservationSaveRequest.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return reservationSaveRequest.toReservation(member, theme, reservationTime, reservationStatus);
    }

    private void validateDuplicatedReservationSuccess(Reservation reservation) {
        boolean existsReservation = reservationRepository.existsByDateAndTimeStartAtAndStatus(
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getStatus()
        );

        if (existsReservation) {
            throw new IllegalArgumentException("중복된 예약이 있습니다.");
        }
    }

    private void validateDuplicatedReservationWaiting(Reservation reservation, LoginMember loginMember) {
        List<ReservationStatus> reservationStatuses = reservationRepository.findStatusesByMemberIdAndDateAndTimeStartAt(
                loginMember.id(),
                reservation.getDate(),
                reservation.getStartAt()
        );

        if (reservationStatuses.contains(ReservationStatus.SUCCESS) || reservationStatuses.contains(ReservationStatus.WAIT)) {
            throw new IllegalArgumentException("예약이 완료되었거나, 대기 상태로 등록된 예약입니다.");
        }
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        return ReservationResponse.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByStatus(ReservationStatus reservationStatus) {
        List<Reservation> waitingReservations = reservationRepository.findAllByStatusFromDate(reservationStatus, LocalDate.now());

        return waitingReservations.stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findMemberReservations(LoginMember loginMember) {
        List<Reservation> memberReservations = reservationRepository.findAllByMemberIdFromDateOrderByDateAscTimeStartAtAscCreatedAtAsc(loginMember.id(), LocalDate.now());

        return memberReservations.stream()
                .map(reservation -> MemberReservationResponse.toResponse(
                        reservation,
                        reservationRepository.countWaitingRankBy(
                                reservation.getDate(),
                                reservation.getTime().getId(),
                                reservation.getTheme().getId(),
                                reservation.getCreatedAt()
                        )
                )).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationWaitingResponse> findWaitingReservations() {
        List<Reservation> waitingReservations = reservationRepository.findAllByStatusFromDate(ReservationStatus.WAIT, LocalDate.now());

        return waitingReservations.stream()
                .map(ReservationWaitingResponse::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllBySearchCondition(ReservationSearchConditionRequest request) {
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateBetweenOrderByDateAscTimeStartAtAscCreatedAtAsc(
                        request.themeId(),
                        request.memberId(),
                        request.dateFrom(),
                        request.dateTo()
                ).stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    public void cancelById(Long id) {
        Reservation canceledReservation = getCanceledReservation(id);
        updateFirstWaitingReservation(canceledReservation);
    }

    private Reservation getCanceledReservation(Long id) {
        Reservation successReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (successReservation.getDate().equals(LocalDate.now())) {
            throw new IllegalArgumentException("당일 예약은 취소할 수 없습니다.");
        }
        successReservation.updateStatus(ReservationStatus.CANCEL);

        return successReservation;
    }

    private void updateFirstWaitingReservation(Reservation canceledReservation) {
        reservationRepository.findFirstWaitingReservationBy(
                canceledReservation.getDate(),
                canceledReservation.getTime().getId(),
                canceledReservation.getTheme().getId()
        ).ifPresent(reservation -> reservation.updateStatus(ReservationStatus.SUCCESS));
    }
}
