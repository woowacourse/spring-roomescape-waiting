package roomescape.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.dto.TimeWithStatusResponse;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.ThemeService;

@Component
public class ReservationFacade {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationWaitingService reservationWaitingService;
    private final ThemeService themeService;

    public ReservationFacade(
            ReservationService reservationService,
            ReservationTimeService reservationTimeService,
            ReservationWaitingService reservationWaitingService,
            ThemeService themeService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.reservationWaitingService = reservationWaitingService;
        this.themeService = themeService;
    }

    @Transactional
    public Reservation addReservation(ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());
        Reservation reservation = Reservation.createWith(
                request.name(),
                request.date(),
                reservationTime,
                theme,
                LocalDateTime.now()
        );

        return reservationService.addReservation(reservation);
    }

    @Transactional
    public Reservation updateMyReservation(Long id, String name, ReservationUpdateRequest request) {
        Reservation existing = reservationService.getById(id);
        ReservationTime newTime = reservationTimeService.getById(request.timeId());
        Reservation updated = existing.updateWith(
                name,
                request.date(),
                newTime,
                LocalDateTime.now()
        );

        return reservationService.updateReservation(updated);
    }

    public List<TimeWithStatusResponse> getTimesWithAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = reservationTimeService.getReservationTimes();
        Set<Long> reservedTimeIds = reservationService.getReservedTimeIds(date, themeId);

        return times.stream()
                .map(time -> TimeWithStatusResponse.from(time, reservedTimeIds.contains(time.getId())))
                .toList();
    }

    @Transactional
    public void deleteTime(Long id) {
        reservationTimeService.deleteTime(id);
    }

    @Transactional
    public void deleteTheme(Long id) {
        themeService.deleteTheme(id);
    }

    @Transactional
    public ReservationWaiting addWaiting(ReservationWaitingRequest request) {
        Reservation reservation = reservationService.getById(request.reservationId());
        ReservationWaiting reservationWaiting = ReservationWaiting.createWith(
                request.name(),
                LocalDateTime.now(),
                reservation
        );

        return reservationWaitingService.addWaiting(reservationWaiting);
    }

}
