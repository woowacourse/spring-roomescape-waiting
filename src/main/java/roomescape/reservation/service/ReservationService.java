package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.InvalidReservationDateValueException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.InvalidTimeStartAtValueException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationWaitingRepository reservationWaitingRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public Reservation save(ReservationCommand command) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                command.date(),
                command.timeId(),
                command.themeId())
        ) {
            throw new DuplicateReservationException();
        }

        ReservationTime time = getReservationTime(command.timeId());
        validateExpiry(command.date(), time.getStartAt());

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(ThemeNotFoundException::new);

        try {
            return reservationRepository.save(
                    Reservation.of(
                            command.name(),
                            command.date(),
                            time,
                            theme
                    )
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReservationException();
        }
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(TimeNotFoundException::new);
    }

    private void validateExpiry(LocalDate date, LocalTime startAt) {
        LocalDate nowDate = LocalDate.now(clock);

        if (nowDate.isAfter(date)) {
            throw new InvalidReservationDateValueException();
        }

        if (nowDate.equals(date) && LocalTime.now(clock).isAfter(startAt)) {
            throw new InvalidTimeStartAtValueException();
        }
    }

    public List<ReservationWithStatusResult> findAllByName(String name) {
        List<ReservationWithStatusResult> reserved = reservationRepository.findAllByName(name)
                .stream()
                .map(
                        reservation -> new ReservationWithStatusResult(
                                reservation.getId(),
                                reservation.getName(),
                                reservation.getDate(),
                                reservation.getTime(),
                                reservation.getTheme(),
                                "reserved",
                                0L
                        )
                )
                .toList();

        List<ReservationWithStatusResult> waiting = reservationWaitingRepository.findAllByName(name)
                .stream()
                .map(
                        reservationWaiting -> new ReservationWithStatusResult(
                                reservationWaiting.getId(),
                                reservationWaiting.getName(),
                                reservationWaiting.getDate(),
                                reservationWaiting.getTime(),
                                reservationWaiting.getTheme(),
                                "waiting",
                                calculateOrder(reservationWaiting)
                        )).toList();

        List<ReservationWithStatusResult> results = new ArrayList<>();

        results.addAll(reserved);
        results.addAll(waiting);

        return results;
    }

    private long calculateOrder(ReservationWaiting reservationWaiting) {
        return reservationWaitingRepository.countByDateAndTimeIdAndThemeIdAndIdLessThan(
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getId(),
                reservationWaiting.getTheme().getId(),
                reservationWaiting.getId()
        ) + 1;
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
    public void update(ReservationUpdateCommand command, Long id) {
        Reservation reservation = getReservation(id);

        validateExpiry(
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );

        Reservation updated = updateField(command, reservation);

        validateExpiry(
                updated.getDate(),
                updated.getTime().getStartAt()
        );

        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                updated.getDate(),
                updated.getTime().getId(),
                updated.getTheme().getId(),
                updated.getId()
        )) {
            throw new DuplicateReservationException();
        }

        try {
            reservationRepository.update(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReservationException();
        }
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
    }

    private Reservation updateField(ReservationUpdateCommand command, Reservation reservation) {
        Reservation result = reservation;

        if (command.date() != null) {
            result = reservation.updateDate(command.date());
        }

        if (command.timeId() != null) {
            result = result.updateTime(
                    getReservationTime(command.timeId())
            );
        }

        return result;
    }

    @Transactional
    public void deleteById(Long id) {
        int affectedRow = reservationRepository.deleteById(id);
        int nonAffected = 0;

        if (affectedRow == nonAffected) {
            throw new ReservationNotFoundException();
        }
    }

    public void validateNotExpired(Long id) {
        Reservation reservation = getReservation(id);

        validateExpiry(
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
