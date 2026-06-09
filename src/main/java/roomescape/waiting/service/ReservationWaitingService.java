package roomescape.waiting.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.exception.ReservationWaitingErrorCode;
import roomescape.waiting.repository.ReservationWaitingRepository;
import roomescape.waiting.service.dto.ReservationWaitingCommand;
import roomescape.waiting.service.dto.ReservationWaitingResult;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationWaitingService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @Transactional
    public ReservationWaitingResult save(ReservationWaitingCommand command, LocalDateTime requestTime) {
        ReservationTime time = reservationTimeService.getById(command.timeId());
        Theme theme = themeService.findById(command.themeId());
        ReservationSlot temporalSlot = new ReservationSlot(command.date(), time, theme);
        temporalSlot.validateNotExpired(requestTime);

        Reservation targetReservation = validateTargetReservationExists(temporalSlot);
        validateWaitingAvailable(command.name(), targetReservation, temporalSlot);

        ReservationWaiting newWaiting = new ReservationWaiting(null, command.name(), temporalSlot, requestTime);
        return saveReservationWaiting(newWaiting);
    }

    private void validateWaitingAvailable(String commandName, Reservation targetReservation, ReservationSlot slot) {
        boolean isSameNameAsTarget = Objects.equals(commandName, targetReservation.getName());
        boolean isAlreadyReserved = reservationRepository.hasBookingAtSameTime(commandName, slot);
        boolean isAlreadyWaiting = reservationWaitingRepository.hasWaitingAtSameTime(commandName, slot);

        if (isSameNameAsTarget || isAlreadyReserved || isAlreadyWaiting) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }

    private ReservationWaitingResult saveReservationWaiting(ReservationWaiting newWaiting) {
        ReservationWaiting saved = reservationWaitingRepository.save(newWaiting);
        return ReservationWaitingResult.from(saved);
    }

    @Transactional
    public void deleteOwnedWaitingById(long id, String name, LocalDateTime requestTime) {
        ReservationWaiting reservationWaiting = getById(id);
        reservationWaiting.validateDeletable(name, requestTime);

        reservationWaitingRepository.delete(reservationWaiting);
    }

    private ReservationWaiting getById(long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND)
                );
    }

    private Reservation validateTargetReservationExists(ReservationSlot slot) {
        return reservationRepository.findBySlot(slot)
                .orElseThrow(() -> new NotFoundException(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND));
    }
}
