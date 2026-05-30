package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

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
                    consistNewReservationObject(command)
            );
            return ReservationResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    private Reservation consistNewReservationObject(ReservationCommand command) {
        ReservationTime time = reservationTimeService.findById(command.timeId());
        Theme theme = themeService.findById(command.themeId());
        validateReservationUniqueness(command.date(), time, theme);

        Reservation newReservation = Reservation.of(command.name(), command.date(), time, theme);
        newReservation.validateExpiry();
        return newReservation;
    }

    private void validateReservationUniqueness(LocalDate date, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.id(), theme.id())) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id, String name) {
        Reservation updated = consistUpdatedReservation(command, id, name);
        try {
            reservationRepository.update(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    private Reservation consistUpdatedReservation(ReservationUpdateCommand command, Long id, String name) {
        Reservation reservation = findById(id);
        reservation.validateOwner(name);
        reservation.validateExpiry();

        ReservationTime newTime = null;
        if (command.timeId() != null) {
            newTime = reservationTimeService.findById(command.timeId());
        }

        Reservation updated = reservation.update(command.date(), newTime);
        updated.validateExpiry();
        validateUpdateUniqueness(updated.date(), updated.time(), updated.theme(), id);
        return updated;
    }

    private void validateUpdateUniqueness(LocalDate date, ReservationTime time, Theme theme, Long excludeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(date, time.id(), theme.id(), excludeId)) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
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
        Reservation reservation = findById(id);
        if (name != null) {
            reservation.validateOwner(name);
        }
        reservation.validateExpiry();

        int affectedRow = reservationRepository.deleteById(id);
        if (affectedRow == 0) {
            throw new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
        }

        Optional<ReservationWaiting> firstWaitingOpt = reservationWaitingRepository.findFirstByDateAndTimeIdAndThemeId(
                reservation.date(),
                reservation.time().id(),
                reservation.theme().id()
        );

        if (firstWaitingOpt.isPresent()) {
            ReservationWaiting waiting = firstWaitingOpt.get();
            Reservation newReservation = Reservation.of(waiting.name(), waiting.date(), waiting.time(), waiting.theme());
            reservationRepository.save(newReservation);
            reservationWaitingRepository.deleteById(waiting.id());
        }
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage()));
    }
}
