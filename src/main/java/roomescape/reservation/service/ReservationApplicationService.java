package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.service.exception.WaitingExistsForSlotException;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.response.ThemeResponse;
import roomescape.waiting.domain.exception.WaitingNotFoundException;
import roomescape.waiting.repository.dto.WaitingWithRank;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.response.WaitingResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationApplicationService {

    private static final int RESERVABLE_DAYS_RANGE = 14;

    private final ReservationService reservationService;
    private final WaitingPromotionService waitingPromotionService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final WaitingService waitingService;
    private final Clock clock;

    public List<ReservationResponse> findAllReservations() {
        return reservationService.findAllReservations()
            .stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public ReservationsAndWaitingsResponse findReservationsAndWaitingsByCustomerName(final String customerName) {
        final List<Reservation> reservations = reservationService.findAllByCustomerNameAndAfterNow(customerName);
        final List<WaitingWithRank> waitingsWithRank = waitingService.findAllWithRankByCustomerNameAfterNow(
            customerName);

        return ReservationsAndWaitingsResponse.from(
            reservations,
            waitingsWithRank.stream()
                .map(waitingWithRank -> WaitingResponse.of(
                    waitingWithRank.waiting(),
                    waitingWithRank.rank()))
                .toList());
    }

    public List<ReservationTimesWithStatus> findReservationTimeStatuses(final LocalDate date, final Long themeId) {
        return reservationService.findReservationTimeStatuses(date, themeId);
    }

    public ReservationOptionResponse getReservationOptions() {
        final LocalDate today = LocalDate.now(clock);
        final List<LocalDate> dates = today.datesUntil(today.plusDays(RESERVABLE_DAYS_RANGE)).toList();

        final List<ThemeResponse> themes = themeService.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
        return new ReservationOptionResponse(dates, themes);
    }

    @Transactional
    public ReservationResponse create(final ReservationCreateRequest request) {
        final ReservationTime reservationTime = reservationTimeService.getById(request.timeId());
        final Theme theme = themeService.getById(request.themeId());

        final boolean alreadyExistsWaiting = waitingService.existsBySlot(
            request.date(),
            reservationTime.getId(),
            theme.getId()
        );
        if (alreadyExistsWaiting) {
            throw new WaitingExistsForSlotException();
        }

        Reservation reservation = reservationService.create(
            request.name(),
            request.date(),
            reservationTime,
            theme
        );
        return ReservationResponse.from(reservation);
    }

    public ReservationResponse updateByCustomer(final Long reservationId, final ReservationUpdateRequest request) {
        final Reservation oldReservation = reservationService.getReservation(reservationId);
        final ReservationTime time = reservationTimeService.getById(request.timeId());

        final Reservation reservation = reservationService.updateByCustomer(reservationId, request.date(), time);

        promoteIfFutureSlot(oldReservation);
        return ReservationResponse.from(reservation);
    }

    public ReservationResponse updateByAdmin(final Long reservationId, final ReservationUpdateRequest request) {
        final Reservation oldReservation = reservationService.getReservation(reservationId);
        final ReservationTime time = reservationTimeService.getById(request.timeId());

        final Reservation reservation = reservationService.updateByAdmin(reservationId, request.date(), time);

        promoteIfFutureSlot(oldReservation);
        return ReservationResponse.from(reservation);
    }

    public void cancelReservationByIdAndPromoteWaiting(final long reservationId) {
        final Reservation reservation = reservationService.getReservation(reservationId);
        reservationService.cancel(reservation);

        promoteIfFutureSlot(reservation);
    }

    public void deleteReservationById(final long reservationId) {
        final Reservation reservation = reservationService.getReservation(reservationId);
        reservationService.deleteById(reservationId);

        promoteIfFutureSlot(reservation);
    }

    private void promoteIfFutureSlot(final Reservation reservation) {
        if (!reservation.isFutureSlot(LocalDateTime.now(clock))) {
            return;
        }

        try {
            waitingPromotionService.promoteBySlot(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getThemeId()
            );
        } catch (WaitingNotFoundException | ReservationAlreadyExistsException | ReservationOptionChangedException e) {
            log.warn("대기 승격 실패 - reservationId={} slot=[date={} time={} theme={}]",
                reservation.getId(), reservation.getDate(), reservation.getTimeId(), reservation.getThemeId(), e);
        } catch (Exception e) {
            log.error("대기 승격 중 예상치 못한 오류 - reservationId={} slot=[date={} time={} theme={}]",
                reservation.getId(), reservation.getDate(), reservation.getTimeId(), reservation.getThemeId(), e);
        }
    }
}
