package roomescape.business.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.presentation.dto.ReservationMineResponse;
import roomescape.presentation.dto.ReservationRequest;
import roomescape.presentation.dto.ReservationResponse;
import roomescape.presentation.dto.WaitingRequest;
import roomescape.presentation.dto.WaitingResponse;
import roomescape.util.CurrentUtil;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final QueryService queryService;
    private final WaitingService waitingService;
    private final ReservationRepository reservationRepository;
    private final CurrentUtil currentUtil;

    public ReservationService(final QueryService queryService,
                              final WaitingService waitingService,
                              final ReservationRepository reservationRepository,
                              final CurrentUtil currentUtil) {
        this.queryService = queryService;
        this.waitingService = waitingService;
        this.reservationRepository = reservationRepository;
        this.currentUtil = currentUtil;
    }

    @Transactional
    public ReservationResponse insert(final ReservationRequest reservationRequest) {
        final LocalDate date = reservationRequest.date();
        final Long timeId = reservationRequest.timeId();
        final Long themeId = reservationRequest.themeId();

        validateIsDuplicate(date, timeId, themeId);
        final ReservationTime reservationTime = queryService.getReservationTimeById(timeId);
        validateDateAndTimeIsFuture(date, reservationTime.getStartAt());

        final Theme theme = queryService.getThemeById(themeId);
        final Member member = queryService.getMemberById(reservationRequest.memberId());

        final Reservation reservation = new Reservation(date, member, reservationTime, theme);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateIsDuplicate(final LocalDate date, final Long playTimeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, playTimeId, themeId)) {
            throw new DuplicateException("추가 하려는 예약과 같은 날짜, 시간, 테마의 예약이 이미 존재합니다.");
        }
    }

    private void validateDateAndTimeIsFuture(final LocalDate date, final LocalTime time) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        if (reservationDateTime.isBefore(currentUtil.getCurrentDateTime())) {
            throw new InvalidDateAndTimeException("방탈출 예약 날짜와 시간이 현재보다 과거일 수 없습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllFilter(final Long memberId, final Long themeId, final LocalDate startDate,
                                                   final LocalDate endDate) {
        return reservationRepository.findAllByFilter(memberId, themeId, startDate, endDate)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(final Long id) {
        final Reservation reservation = queryService.getReservationById(id);
        validateNotPast(reservation);
        reservationRepository.deleteById(id);
        reservationRepository.flush();

        promoteFirstWaitingToReservation(reservation);
    }

    private void promoteFirstWaitingToReservation(final Reservation reservation) {
        final Optional<WaitingResponse> waiting = waitingService.deleteFirstBySameConditionReservation(
                WaitingRequest.from(reservation));

        if (waiting.isEmpty()) {
            return;
        }
        final WaitingResponse newReservation = waiting.get();
        reservationRepository.save(
                new Reservation(newReservation.date(), newReservation.member(), newReservation.time(),
                        newReservation.theme()));
    }

    private void validateNotPast(final Reservation reservation) {
        if (reservation.isPast(currentUtil.getCurrentDateTime())) {
            throw new BadRequestException("이전 예약은 삭제할 수 없습니다.");
        }
    }

    public List<ReservationMineResponse> findByMemberId(final Long memberId) {
        final List<ReservationMineResponse> reservations = getByMemberId(memberId);
        final List<ReservationMineResponse> waitings = waitingService.findByMemberId(memberId);
        return Stream.concat(reservations.stream(), waitings.stream())
                .toList();
    }

    private List<ReservationMineResponse> getByMemberId(final Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(ReservationMineResponse::from)
                .toList();
    }
}
