package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
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
    public ServiceReceptionResponse save(ServiceReservationCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.findReservationTime(request.timeId());
        if (isPast(request.reservationDate(), reservationTime)) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE);
        }

        Optional<Reservation> reservation = reservationService.findBySlot(request.reservationDate(), request.timeId(),
                request.themeId());
        if (reservation.isEmpty()) {
            Reservation newReservation = reservationService.save(request);
            return ServiceReceptionResponse.of(newReservation, 0L, ReservationStatus.CONFIRMED.name());
        }
        if (reservation.get().getName().equals(request.name())) {
            throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_RESERVATION);
        }

        return waitService.save(request.toWait(LocalDateTime.now(clock),
                reservationTime,
                themeService.findTheme(request.themeId())));
    }

    public List<ServiceReceptionResponse> findByName(String name) {
        List<ServiceReceptionResponse> receptions = new ArrayList<>();

        receptions.addAll(reservationService.findByName(name));
        receptions.addAll(waitService.findByName(name));

        return receptions;
    }

    public List<ServiceReceptionResponse> findAll() {
        List<ServiceReceptionResponse> receptions = new ArrayList<>();

        receptions.addAll(reservationService.findAll());
        receptions.addAll(waitService.findAll());

        return receptions;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.findReservation(id);
        validateDelete(reservation.getDate(), reservation.getTime());
        reservationService.delete(id);

        List<Wait> waits = waitService.findByReservation(reservation);
        if (waits.isEmpty()) {
            return;
        }
        Wait firstOrder = waits.getFirst();

        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest(firstOrder.getName(),
                firstOrder.getReservationDate(), firstOrder.getTime().getId(), firstOrder.getTheme().getId());
        reservationService.save(request);
        deleteWait(firstOrder.getId());
    }

    @Transactional
    public void deleteWait(Long id) {
        Wait wait = waitService.findWait(id);
        validateDelete(wait.getReservationDate(), wait.getTime());
        
        waitService.delete(id);
    }

    private void validateDelete(LocalDate reservationDate, ReservationTime reservationTime) {
        if (isPast(reservationDate, reservationTime)) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_DELETE);
        }
    }

    private boolean isPast(LocalDate reservationDate, ReservationTime reservationTime) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (reservationDate.isBefore(nowDate)) {
            return true;
        }
        if (reservationDate.isAfter(nowDate)) {
            return false;
        }
        return reservationTime.isPast(nowTime);
    }
}
