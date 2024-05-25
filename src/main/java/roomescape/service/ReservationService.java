package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.MyReservationResponse;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.exception.DuplicateReservationException;
import roomescape.service.exception.InvalidSearchDateException;
import roomescape.service.exception.PreviousTimeException;

@Service
public class ReservationService {
    private static final Long RESERVED_RANK = 0L;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> getReservedReservations() {
        return reservationRepository.findAllByStatus(Status.RESERVED)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getWaitings() {
        return reservationRepository.findAllByStatus(Status.WAITING)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> getReservationsByMember(final LoginMember member) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(member.id());
        return reservations.stream()
                .map(this::findStatus)
                .toList();
    }

    private MyReservationResponse findStatus(Reservation reservation) {
        if (reservation.isReserved()) {
            return MyReservationResponse.from(reservation, RESERVED_RANK);
        }
        return MyReservationResponse.from(reservation, findRank(reservation));
    }

    private Long findRank(Reservation reservation) {
        return reservationRepository.findAllByTimeIdAndThemeIdAndDateAndStatus(
                        reservation.getTime().getId(), reservation.getTheme().getId(),
                        reservation.getDate(), Status.WAITING)
                .stream()
                .filter(r -> r.getId() < reservation.getId())
                .count() + 1;
    }

    @Transactional
    public List<ReservationResponse> searchReservations(
            final ReservationSearchCondition condition) {
        validateDateRange(condition);
        List<Reservation> reservations
                = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                condition.themeId(), condition.memberId(),
                condition.dateFrom(), condition.dateTo());
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse addReservedReservation(
            final CreateReservationRequest reservationRequest,
            final Long memberId) {
        return createReservation(reservationRequest, memberId, Status.RESERVED);
    }

    public ReservationResponse addWaiting(
            final CreateReservationRequest reservationRequest,
            final Long memberId) {
        validateDuplicateWaiting(reservationRequest, memberId);
        return createReservation(reservationRequest, memberId, Status.WAITING);
    }

    private ReservationResponse createReservation(
            final CreateReservationRequest reservationRequest,
            final Long memberId,
            final Status status) {
        validateDuplicate(reservationRequest, status);
        final LocalDate date = reservationRequest.date();
        final ReservationTime time
                = reservationTimeRepository.findByIdOrThrow(reservationRequest.timeId());
        validateBeforeDay(date, time);

        final Theme theme = themeRepository.findByIdOrThrow(reservationRequest.themeId());
        final Member member = memberRepository.findByIdOrThrow(memberId);
        final Reservation reservation = new Reservation(null, member, date, time, theme, status);
        reservationRepository.save(reservation);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void deleteReservedReservation(final Long id) {
        final Reservation deleteReservation = reservationRepository.findByIdOrThrow(id);
        if (deleteReservation.isWaiting()) {
            throw new InvalidRequestException("이 예약은 현재 대기 상태입니다.");
        }
        reservationRepository.deleteById(id);
        reserveFirstWaitingIfPresent(deleteReservation);
    }

    public void deleteWaiting(final Long id) {
        final Reservation deleteWaiting = reservationRepository.findByIdOrThrow(id);
        if (deleteWaiting.isReserved()) {
            throw new InvalidRequestException("이 예약 대기는 현재 예약 상태입니다.");
        }
        reservationRepository.deleteById(id);
    }

    private void reserveFirstWaitingIfPresent(Reservation deleteReservation) {
        Optional<Reservation> firstWaitingReservation = reservationRepository
                .findFirstByTimeIdAndThemeIdAndDateAndStatus(
                        deleteReservation.getTime().getId(),
                        deleteReservation.getTheme().getId(),
                        deleteReservation.getDate(),
                        Status.WAITING);
        firstWaitingReservation
                .ifPresent(Reservation::reserveWaiting);
    }

    private void validateDateRange(final ReservationSearchCondition request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new InvalidSearchDateException("from은 to보다 이전 날짜여야 합니다.");
        }
    }

    private void validateDuplicate(final CreateReservationRequest reservationRequest,
                                   final Status status) {
        final boolean isExistReservation =
                reservationRepository.existsByThemeIdAndTimeIdAndDateAndStatus(
                        reservationRequest.themeId(),
                        reservationRequest.timeId(),
                        reservationRequest.date(),
                        Status.RESERVED);
        if (isExistReservation && Status.RESERVED == status) {
            throw new DuplicateReservationException("중복된 시간으로 예약이 불가합니다.");
        }
        if (!isExistReservation && Status.WAITING == status) {
            throw new DuplicateReservationException("예약이 존재하지 않을 경우 예약대기할 수 없습니다.");
        }
    }

    private void validateDuplicateWaiting(final CreateReservationRequest reservationRequest,
                                          final Long memberId) {
        final boolean isReservedByMember =
                reservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(
                        memberId,
                        reservationRequest.themeId(),
                        reservationRequest.timeId(),
                        reservationRequest.date());
        if (isReservedByMember) {
            throw new DuplicateReservationException("중복된 시간으로 예약대기가 불가합니다.");
        }
    }

    private void validateBeforeDay(final LocalDate date, final ReservationTime time) {
        final LocalDateTime reservationDateTime = date.atTime(time.getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new PreviousTimeException("지난 시간으로 예약할 수 없습니다.");
        }
    }
}
