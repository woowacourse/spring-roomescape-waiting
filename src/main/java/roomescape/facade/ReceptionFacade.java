package roomescape.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.dto.WaitDetailDto;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.request.ServiceReservationCreateRequest;
import roomescape.service.dto.response.ServiceReceptionListResponse;
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

    public ServiceReceptionListResponse findByName(String name) {
        return ServiceReceptionListResponse.from(reservationService.findByName(name), waitService.findByName(name));
    }

    public ServiceReceptionListResponse findAll() {
        return ServiceReceptionListResponse.from(reservationService.findAll(), waitService.findAll());
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.findReservation(id);
        validateDelete(reservation.getDate(), reservation.getTime());
        reservationService.delete(id);

        List<WaitDetailDto> waits = waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId());
        if (waits.isEmpty()) {
            return;
        }
        confirmFirstWait(waits.getFirst());
    }

    @Transactional
    public void deleteWait(Long id) {
        WaitDetailDto waitDetailDto = waitService.findWait(id);
        validateDelete(waitDetailDto.reservationDate(), waitDetailDto.reservationTime().toEntity());

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
        return ServiceReceptionResponse.from(newReservation);
    }

    private ServiceReceptionResponse saveWait(ServiceReservationCreateRequest request, ReservationTime reservationTime,
                                              Theme theme) {
        Wait newWaitWithoutId = request.toWait(LocalDateTime.now(clock), reservationTime, theme);
        WaitDetailDto newWait = waitService.save(newWaitWithoutId);
        return ServiceReceptionResponse.from(newWait);
    }

    private void confirmFirstWait(WaitDetailDto firstOrder) {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest(firstOrder.name(),
                firstOrder.reservationDate(), firstOrder.reservationTime().id(), firstOrder.theme().id());

        Reservation reservationWithoutId = request.toReservation(firstOrder.reservationTime().toEntity(),
                firstOrder.theme().toEntity());
        reservationService.save(reservationWithoutId);
        waitService.delete(firstOrder.id());
    }
}
