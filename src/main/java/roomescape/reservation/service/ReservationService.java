package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService,
                              ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public ReservationResult save(ReservationCommand command, LocalDateTime requestTime) {
        ReservationTime time = reservationTimeService.getById(command.timeId());
        Theme theme = themeService.findById(command.themeId());

        ReservationSlot slot = new ReservationSlot(command.date(), time, theme);
        validateSlotAvailable(null, command.name(), slot);
        Reservation newReservation = new Reservation(command.name(), slot, requestTime);

        try {
            Reservation result = reservationRepository.save(newReservation);
            return ReservationResult.from(result);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void update(ReservationUpdateCommand command, long id, String name, LocalDateTime requestTime) {
        Reservation reservation = getById(id);
        reservation.validateDeletableByUser(name, requestTime);

        ReservationTime newTime = null;
        if (command.timeId() != null) {
            newTime = reservationTimeService.getById(command.timeId());
        }

        ReservationSlot temporalSlot = reservation.generateTemporalSlot(command.date(), newTime);
        validateSlotAvailable(id, name, temporalSlot);
        Reservation updated = reservation.update(command.date(), newTime, name, requestTime);

        try {
            reservationRepository.save(updated);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateSlotAvailable(Long id, String name, ReservationSlot slot) {
        boolean isBooked = false;
        if (id == null) {
            isBooked = reservationRepository.hasBookingAtSameTime(name, slot);
        }

        boolean isBookedByOthers = false;
        if (id != null) {
            isBookedByOthers = reservationRepository.isAlreadyBookedByOthers(id, name, slot);
        }

        boolean isAlreadyWaiting = reservationWaitingRepository.hasWaitingAtSameTime(name, slot);

        if (isBooked || isBookedByOthers || isAlreadyWaiting) {
            throw new InvalidBusinessStateException(ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME);
        }
    }

    @Transactional
    public void deleteByUser(long id, String name, LocalDateTime requestTime) {
        Reservation reservation = getById(id);
        reservation.validateDeletableByUser(name, requestTime);
        processDeletion(reservation, requestTime);
    }

    @Transactional
    public void deleteByAdmin(long id, LocalDateTime requestTime) {
        Reservation reservation = getById(id);
        reservation.validateDeletableByAdmin(requestTime);
        processDeletion(reservation, requestTime);
    }

    private void processDeletion(Reservation reservation, LocalDateTime requestTime) {
        List<ReservationWaiting> waitings = reservationWaitingRepository.queryAllBySlotForUpdate(
                new ReservationSlot(reservation.getDate(), reservation.getTime(), reservation.getTheme())
        );

        reservationRepository.delete(reservation);
        promoteNextWaiting(waitings, requestTime);
    }

    public Reservation getById(long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND)
        );
    }

    private void promoteNextWaiting(List<ReservationWaiting> lockedWaitings, LocalDateTime requestTime) {
        for (ReservationWaiting waiting : lockedWaitings) {
            if (!reservationRepository.hasBookingAtSameTime(waiting.getName(), waiting.getSlot())) {
                createReservationFromWaiting(waiting, requestTime);
                return;
            }
        }
    }

    private void createReservationFromWaiting(ReservationWaiting waiting, LocalDateTime requestTime) {
        reservationWaitingRepository.delete(waiting);
        Reservation newReservation = waiting.toReservation(requestTime);
        reservationRepository.save(newReservation);
    }
}
