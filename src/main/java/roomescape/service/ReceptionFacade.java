package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.service.dto.request.ServiceReservationCreateRequest;
import roomescape.service.dto.response.ServiceReceptionResponse;

@Service
@Transactional(readOnly = true)
public class ReceptionFacade {

    private final ReservationService reservationService;
    private final WaitService waitService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final Clock clock;

    public ReceptionFacade(ReservationService reservationService, WaitService waitService,
                           ReservationTimeService reservationTimeService, ThemeService themeService, Clock clock) {
        this.reservationService = reservationService;
        this.waitService = waitService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.clock = clock;
    }

    @Transactional
    public ServiceReceptionResponse create(ServiceReservationCreateRequest request) {
        Optional<Reservation> reservation = reservationService.readBySlot(request.reservationDate(), request.timeId(),
                request.themeId());
        if (reservation.isEmpty()) {
            Reservation newReservation = reservationService.create(request);
            return ServiceReceptionResponse.of(newReservation, 0L, ReservationStatus.CONFIRMED.name());
        }
        if (reservation.get().getName().equals(request.name())) {
            throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_RESERVATION);
        }

        return waitService.create(request.toWait(LocalDateTime.now(clock),
                reservationTimeService.readReservationTime(request.timeId()),
                themeService.readTheme(request.themeId())));
    }

    public List<ServiceReceptionResponse> readByName(String name) {
        List<ServiceReceptionResponse> receptions = new ArrayList<>();

        receptions.addAll(reservationService.readByName(name));
        receptions.addAll(waitService.readByName(name));

        return receptions;
    }

    public List<ServiceReceptionResponse> readAll() {
        List<ServiceReceptionResponse> receptions = new ArrayList<>();

        receptions.addAll(reservationService.readAll());
        receptions.addAll(waitService.readAll());

        return receptions;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.readReservation(id);
        reservationService.delete(id);

        List<Wait> waits = waitService.readByReservation(reservation);
        if (waits.isEmpty()) {
            return;
        }
        Wait firstOrder = waits.getFirst();

        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest(firstOrder.getName(),
                firstOrder.getReservationDate(), firstOrder.getTime().getId(), firstOrder.getTheme().getId());
        reservationService.create(request);
        deleteWait(firstOrder.getId());
    }

    @Transactional
    public void deleteWait(Long id) {
        waitService.delete(id);
    }
}
