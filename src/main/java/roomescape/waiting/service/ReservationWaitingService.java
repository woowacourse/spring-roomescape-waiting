package roomescape.waiting.service;

import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;
import roomescape.waiting.exception.ReservationWaitingErrorCode;
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
        ReservationWaiting newReservationWaiting = buildValidReservationWaiting(command, requestTime);
        newReservationWaiting.validateExpiry(requestTime);

        try {
            ReservationWaiting saved = reservationWaitingRepository.save(newReservationWaiting);
            return ReservationWaitingResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationWaitingErrorCode.DUPLICATE_WAITING);
        }
    }

    @Transactional
    public void deleteById(Long id, String name, LocalDateTime requestTime) {
        ReservationWaiting reservationWaiting = getById(id);
        reservationWaiting.validateExpiry(requestTime);
        reservationWaiting.validateOwner(name);

        reservationWaitingRepository.delete(reservationWaiting);
    }

    private ReservationWaiting getById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND)
                );
    }

    private ReservationWaiting buildValidReservationWaiting(ReservationWaitingCommand command,
                                                            LocalDateTime requestTime) {
        ReservationTime time = reservationTimeService.getByIdForUpdate(command.timeId());
        Theme theme = themeService.findById(command.themeId());
        ReservationSlot slot = new ReservationSlot(command.date(), time, theme);

        validateTargetReservationExists(slot);
        validateNoSameTimeBooking(slot, command.name());
        validateNoSameTimeWaiting(slot, command.name(), requestTime);

        return new ReservationWaiting(
                command.name(),
                command.date(),
                time,
                theme,
                requestTime
        );
    }

    private void validateTargetReservationExists(ReservationSlot slot) {
        reservationRepository.findBySlot(slot)
                .orElseThrow(() -> new NotFoundException(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND));
    }

    private void validateNoSameTimeBooking(ReservationSlot slot, String name) {
        Reservation candidate = new Reservation(null, name, slot, slot.date().atStartOfDay());
        if (reservationRepository.hasBookingAtSameTime(candidate)) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }

    private void validateNoSameTimeWaiting(ReservationSlot slot, String name, LocalDateTime requestTime) {
        ReservationWaiting candidate = new ReservationWaiting(null, name, slot, requestTime);
        if (reservationWaitingRepository.hasWaitingAtSameTime(candidate)) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }
}
