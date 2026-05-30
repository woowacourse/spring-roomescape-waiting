package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

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
    public ReservationResult save(ReservationCommand command) {
        try {
            Reservation saved = reservationRepository.save(
                    buildNewReservation(command)
            );
            return ReservationResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    private Reservation buildNewReservation(ReservationCommand command) {
        ReservationTime time = reservationTimeService.findById(command.timeId());
        Theme theme = themeService.findById(command.themeId());

        Reservation newReservation = Reservation.of(command.name(), command.date(), time, theme);
        newReservation.validateExpiry();

        validateReservationUniqueness(command.date(), time, command.name());

        return newReservation;
    }

    private void validateReservationUniqueness(LocalDate date, ReservationTime time, String name) {
        if (reservationRepository.existsByDateAndTimeIdAndName(date, time.id(), name)) {
            throwDuplicateScheduleException();
        }
        checkWaitingScheduleDuplicate(date, time.id(), name);
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id, String name) {
        Reservation updated = buildUpdatedReservation(command, id, name);
        try {
            reservationRepository.update(updated);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    private Reservation buildUpdatedReservation(ReservationUpdateCommand command, Long id, String name) {
        Reservation reservation = findById(id);
        reservation.validateOwner(name);
        reservation.validateExpiry();

        ReservationTime newTime = null;
        if (command.timeId() != null) {
            newTime = reservationTimeService.findById(command.timeId());
        }

        Reservation updated = reservation.update(command.date(), newTime);
        updated.validateExpiry();
        validateUpdateUniqueness(updated.date(), updated.time(), name, id);
        return updated;
    }

    private void validateUpdateUniqueness(LocalDate date, ReservationTime time, String name,
                                          Long excludeId) {
        if (reservationRepository.existsByDateAndTimeIdAndNameAndIdNot(date, time.id(), name, excludeId)) {
            throwDuplicateScheduleException();
        }
        checkWaitingScheduleDuplicate(date, time.id(), name);
    }

    private void checkWaitingScheduleDuplicate(LocalDate date, Long timeId, String name) {
        if (reservationWaitingRepository.existsByDateAndTimeIdAndName(date, timeId, name)) {
            throwDuplicateScheduleException();
        }
    }

    private void throwDuplicateScheduleException() {
        throw new InvalidBusinessStateException(
                ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
    }

    public List<ReservationWithStatusResult> findAllByName(String name) {
        return reservationRepository.findAllByNameWithStatus(name);
    }

    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    public PopularThemesResult findPopularThemes(int period, int limit) {
        int oneDayDifference = 1;
        LocalDate to = LocalDate.now().minusDays(oneDayDifference);
        LocalDate from = to.minusDays(period).plusDays(oneDayDifference);
        return new PopularThemesResult(
                reservationRepository.findPopularThemes(from, to, limit)
        );
    }

    @Transactional
    public void deleteById(Long id, String name) {
        Reservation reservation = getValidatedReservation(id, name);
        deleteReservation(id);
        assignNextWaiting(reservation);
    }

    private Reservation getValidatedReservation(Long id, String name) {
        Reservation reservation = findById(id);
        if (name != null) {
            reservation.validateOwner(name);
        }
        reservation.validateExpiry();
        return reservation;
    }

    private void deleteReservation(Long id) {
        int affectedRow = reservationRepository.deleteById(id);
        if (affectedRow == 0) {
            throw new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
        }
    }

    private void assignNextWaiting(Reservation reservation) {
        List<ReservationWaiting> allReservationWaiting = reservationWaitingRepository.findAllByDateAndTimeIdAndThemeId(
                reservation.date(),
                reservation.time().id(),
                reservation.theme().id()
        );

        for (ReservationWaiting waiting : allReservationWaiting) {
            if (!reservationRepository.existsByDateAndTimeIdAndName(waiting.date(), waiting.time().id(),
                    waiting.name())) {
                createReservationFromWaiting(waiting);
                return;
            }
        }
    }

    private void createReservationFromWaiting(ReservationWaiting waiting) {
        reservationWaitingRepository.deleteById(waiting.id());

        Reservation newReservation = Reservation.of(
                waiting.name(),
                waiting.date(),
                waiting.time(),
                waiting.theme()
        );
        reservationRepository.save(newReservation);
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage()));
    }
}
