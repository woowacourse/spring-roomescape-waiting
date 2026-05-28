package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
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
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.service.ReservationTimeService;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final Clock clock;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationService(ReservationRepository reservationRepository,
                              Clock clock,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
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

        validateExpiry(command.date(), time.startAt());
        validateReservationUniqueness(command, time, theme);

        return Reservation.of(command.name(), command.date(), time, theme);
    }

    private void validateReservationUniqueness(ReservationCommand command, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(command.date(), time.id(), theme.id())) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id) {
        ReservationTime time = null;
        if (command.timeId() != null) {
            time = reservationTimeService.findById(command.timeId());
        }
        Reservation reservation = getReservationDomain(id);
        validateExpiry(reservation.date(), reservation.time().startAt());
        Reservation updated = updateField(command, reservation, time);
        validateExpiry(updated.date(), updated.time().startAt());
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                updated.date(),
                updated.time().id(),
                updated.theme().id(),
                updated.id())
        ) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
        try {
            reservationRepository.update(updated);
        } catch (DataIntegrityViolationException e) {
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
        LocalDate to = LocalDate.now(clock).minusDays(oneDayDifference);
        LocalDate from = to.minusDays(period).plusDays(oneDayDifference);
        return new PopularThemesResult(
                reservationRepository.findPopularThemes(from, to, limit)
        );
    }

    @Transactional
    public void deleteById(Long id) {
        int affectedRow = reservationRepository.deleteById(id);
        if (affectedRow == 0) {
            throw new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
        }
    }

    public void validateNotExpired(Long id) {
        Reservation reservation = getReservationDomain(id);
        validateExpiry(reservation.date(), reservation.time().startAt());
    }

    private void validateExpiry(LocalDate date, LocalTime startAt) {
        ReservationDate.of(date, clock);
        LocalDate nowDate = LocalDate.now(clock);
        if (nowDate.equals(date) && LocalTime.now(clock).isAfter(startAt)) {
            throw new InvalidRequestValueException(TimeErrorCode.INVALID_START_AT.getMessage());
        }
    }

    public Reservation getReservationDomain(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage()));
    }

    public void validateOwnership(Long id, String name) {
        Reservation reservation = getReservationDomain(id);
        if (!reservation.hasSameName(name)) {
            throw new ForbiddenException(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
        }
    }

    private Reservation updateField(ReservationUpdateCommand command, Reservation reservation, ReservationTime time) {
        Reservation result = reservation;
        if (command.date() != null) {
            result = reservation.updateDate(command.date());
        }
        if (time != null) {
            result = result.updateTime(time);
        }
        return result;
    }
}
