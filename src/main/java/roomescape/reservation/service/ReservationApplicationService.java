package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.WaitingExistsForSlotException;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.waiting.repository.dto.WaitingWithRank;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.response.WaitingResponse;

@Service
@RequiredArgsConstructor
public class ReservationApplicationService {

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
        final List<WaitingWithRank> waitingsWithRank = waitingService.findAllWithRankByCustomerNameAfterNow(customerName);

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
        return reservationService.getReservationOptions();
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
        final Reservation reservation = reservationService.updateByCustomer(reservationId, request.date(), request.timeId());
        return ReservationResponse.from(reservation);
    }

    public ReservationResponse updateByAdmin(final Long reservationId, final ReservationUpdateRequest request) {
        final Reservation reservation = reservationService.updateByAdmin(reservationId, request.date(), request.timeId());
        return ReservationResponse.from(reservation);
    }

    public void cancelReservationByIdAndPromoteWaiting(final long reservationId) {
        final Reservation reservation = reservationService.getReservation(reservationId);
        reservationService.cancel(reservation.getId());

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

        waitingPromotionService.promoteBySlot(
            reservation.getDate(),
            reservation.getTimeId(),
            reservation.getThemeId()
        );
    }
}
