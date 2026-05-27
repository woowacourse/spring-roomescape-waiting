package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.service.ReservationTimeService;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final Clock clock;
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
    public Reservation save(ReservationCommand command, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                command.date(),
                time.getId(),
                theme.getId())) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
        validateExpiry(command.date(), time.getStartAt());
        try {
            return reservationRepository.save(
                    Reservation.of(
                            command.name(),
                            command.date(),
                            time,
                            theme));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }


    @Transactional
    public Reservation save(ReservationCommand command) {
        ReservationTime time = reservationTimeService.getReservationTime(command.timeId());
        Theme theme = themeService.getTheme(command.themeId());
        return save(command, time, theme);
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id) {
        ReservationTime time = null;
        if (command.timeId() != null) {
            time = reservationTimeService.getReservationTime(command.timeId());
        }
        update(command, id, time);
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id, ReservationTime time) {
        Reservation reservation = getReservation(id);
        validateExpiry(reservation.getDate(), reservation.getTime().getStartAt());
        Reservation updated = updateField(command, reservation, time);
        validateExpiry(updated.getDate(), updated.getTime().getStartAt());
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                updated.getDate(),
                updated.getTime().getId(),
                updated.getTheme().getId(),
                updated.getId())) {
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

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
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
        Reservation reservation = getReservation(id);
        validateExpiry(reservation.getDate(), reservation.getTime().getStartAt());
    }

    private void validateExpiry(LocalDate date, LocalTime startAt) {
        LocalDate nowDate = LocalDate.now(clock);
        if (nowDate.isAfter(date)) {
            throw new InvalidRequestValueException(ReservationErrorCode.INVALID_DATE.getMessage());
        }
        if (nowDate.equals(date) && LocalTime.now(clock).isAfter(startAt)) {
            throw new InvalidRequestValueException(TimeErrorCode.INVALID_START_AT.getMessage());
        }
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage()));
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
