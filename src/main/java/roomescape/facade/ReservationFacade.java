package roomescape.facade;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.response.ReservationListResponse;
import roomescape.controller.dto.response.ReservationWaitListResponse;
import roomescape.controller.dto.response.ReservationWaitResponse;
import roomescape.controller.dto.response.WaitListResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.exception.custom.AlreadyReservedException;
import roomescape.exception.custom.CannotCreatePastReservationException;
import roomescape.exception.custom.CannotModifyPastReservationException;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.WaitInfo;

@Service
@Transactional(readOnly = true)
public class ReservationFacade {

    private final ReservationService reservationService;
    private final WaitService waitService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final Clock clock;

    public ReservationFacade(ReservationService reservationService, WaitService waitService,
                             ReservationTimeService reservationTimeService, ThemeService themeService, Clock clock) {
        this.reservationService = reservationService;
        this.waitService = waitService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaitResponse save(ReservationCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.findReservationTime(request.timeId());
        Theme theme = themeService.findTheme(request.themeId());

        if (isPast(request.date(), reservationTime)) {
            throw new CannotCreatePastReservationException();
        }

        return saveReservationOrWait(request, reservationTime, theme);
    }

    public ReservationWaitListResponse findByName(String name) {
        ReservationListResponse reservationListResponse = ReservationListResponse.from(
                reservationService.findByName(name));

        WaitListResponse waitListResponse = WaitListResponse.from(waitService.findByName(name));

        return new ReservationWaitListResponse(reservationListResponse, waitListResponse);
    }

    public ReservationWaitListResponse findAll() {
        ReservationListResponse reservationListResponse = ReservationListResponse.from(
                reservationService.findAll());

        WaitListResponse waitListResponse = WaitListResponse.from(waitService.findAll());

        return new ReservationWaitListResponse(reservationListResponse, waitListResponse);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.findReservation(id);
        validateDelete(reservation.getDate(), reservation.getTime());
        reservationService.delete(id);

        List<WaitInfo> waits = waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId());
        if (waits.isEmpty()) {
            return;
        }
        confirmFirstWait(waits.getFirst());
    }

    @Transactional
    public void deleteWait(Long id) {
        WaitInfo waitInfo = waitService.findWait(id);
        validateDelete(waitInfo.date(), waitInfo.time().toEntity());

        waitService.delete(id);
    }

    private void validateDelete(LocalDate reservationDate, ReservationTime reservationTime) {
        if (isPast(reservationDate, reservationTime)) {
            throw new CannotModifyPastReservationException();
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

    private ReservationWaitResponse saveReservationOrWait(ReservationCreateRequest request,
                                                          ReservationTime reservationTime, Theme theme) {
        Optional<Reservation> reservation = reservationService.findBySlot(request.date(), request.timeId(),
                request.themeId());
        if (reservation.isEmpty()) {
            return saveReservation(request, reservationTime, theme);
        }
        if (reservation.get().isSameUser(request.name())) {
            throw new AlreadyReservedException();
        }

        return saveWait(request, reservationTime, theme);
    }

    private ReservationWaitResponse saveReservation(ReservationCreateRequest request,
                                                    ReservationTime reservationTime,
                                                    Theme theme) {
        Reservation newReservationWithoutId = request.toReservation(reservationTime, theme);
        Reservation newReservation = reservationService.save(newReservationWithoutId);
        return ReservationWaitResponse.from(newReservation);
    }

    private ReservationWaitResponse saveWait(ReservationCreateRequest request, ReservationTime reservationTime,
                                             Theme theme) {
        Wait newWaitWithoutId = request.toWait(LocalDateTime.now(clock), reservationTime, theme);
        WaitInfo newWaitInfo = waitService.save(newWaitWithoutId);
        return ReservationWaitResponse.from(newWaitInfo);
    }

    private void confirmFirstWait(WaitInfo firstWait) {
        Reservation reservationWithoutId = new Reservation(
                firstWait.name(),
                firstWait.date(),
                firstWait.time().toEntity(),
                firstWait.theme().toEntity());
        reservationService.save(reservationWithoutId);
        waitService.delete(firstWait.id());
    }
}
