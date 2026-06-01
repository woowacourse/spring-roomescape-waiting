package roomescape.facade;

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
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
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
        Theme theme = themeService.findTheme(request.themeId());

        if (isPast(request.reservationDate(), reservationTime)) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE);
        }

        return saveReservationOrWait(request, reservationTime, theme);
    }

    public List<ServiceReceptionResponse> findByName(String name) {
        List<ServiceReceptionResponse> receptions = new ArrayList<>();

        receptions.addAll(findReservationByName(name));
        receptions.addAll(findWaitByName(name));

        return receptions;
    }

    private List<ServiceReceptionResponse> findReservationByName(String name) {
        return reservationService.findByName(name).stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED))
                .toList();
    }

    private List<ServiceReceptionResponse> findWaitByName(String name) {
        return waitService.findByName(name).stream()
                .map(wait -> ServiceReceptionResponse.of(wait, waitService.calculateOrder(wait),
                        ReservationStatus.WAITING))
                .toList();
    }

    public List<ServiceReceptionResponse> findAll() {
        List<ServiceReceptionResponse> receptions = new ArrayList<>();

        receptions.addAll(findAllReservations());
        receptions.addAll(findAllWaits());

        return receptions;
    }

    private List<ServiceReceptionResponse> findAllReservations() {
        return reservationService.findAll().stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED))
                .toList();
    }

    private List<ServiceReceptionResponse> findAllWaits() {
        return waitService.findAll().stream()
                .map(wait -> ServiceReceptionResponse.of(wait, waitService.calculateOrder(wait),
                        ReservationStatus.WAITING))
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.findReservation(id);
        validateDelete(reservation.getDate(), reservation.getTime());
        reservationService.delete(id);

        List<Wait> waits = waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId());
        if (waits.isEmpty()) {
            return;
        }
        confirmFirstWait(waits.getFirst());
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

    private ServiceReceptionResponse saveReservationOrWait(ServiceReservationCreateRequest request,
                                                           ReservationTime reservationTime, Theme theme) {
        Optional<Reservation> reservation = reservationService.findBySlot(request.reservationDate(), request.timeId(),
                request.themeId());
        if (reservation.isEmpty()) {
            return saveReservation(request, reservationTime, theme);
        }
        if (reservation.get().getName().equals(request.name())) {
            throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_RESERVATION);
        }

        return saveWait(request, reservationTime, theme);
    }

    private ServiceReceptionResponse saveReservation(ServiceReservationCreateRequest request,
                                                     ReservationTime reservationTime,
                                                     Theme theme) {
        Reservation newReservationWithoutId = request.toReservation(reservationTime, theme);
        Reservation newReservation = reservationService.save(newReservationWithoutId);
        return ServiceReceptionResponse.of(newReservation, 0L, ReservationStatus.CONFIRMED);
    }

    private ServiceReceptionResponse saveWait(ServiceReservationCreateRequest request, ReservationTime reservationTime,
                                              Theme theme) {
        Wait newWaitWithoutId = request.toWait(LocalDateTime.now(clock), reservationTime, theme);
        Wait newWait = waitService.save(newWaitWithoutId);
        return ServiceReceptionResponse.of(newWait, waitService.calculateOrder(newWait),
                ReservationStatus.WAITING);
    }

    private void confirmFirstWait(Wait firstOrder) {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest(firstOrder.getName(),
                firstOrder.getReservationDate(), firstOrder.getTime().getId(), firstOrder.getTheme().getId());

        Reservation reservationWithoutId = request.toReservation(firstOrder.getTime(), firstOrder.getTheme());
        reservationService.save(reservationWithoutId);
        waitService.delete(firstOrder.getId());
    }
}
