package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.exception.AuthorizationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.exception.ReservationSlotHasWaitingException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.PopularThemeResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;
    private final ExpiryValidator expiryValidator;


    public ReservationService(ReservationRepository reservationRepository,
                              ReservationWaitingRepository reservationWaitingRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              Clock clock,
                              ExpiryValidator expiryValidator) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
        this.expiryValidator = expiryValidator;
    }

    public Reservation makeReservation(ReservationCommand command) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(
                command.date(), command.timeId(), command.themeId())) {
            throw new DuplicateReservationException();
        }

        validateDoNotHaveWaiting(command.date(), command.timeId(), command.themeId());

        ReservationTime time = getReservationTime(command.timeId());
        expiryValidator.validate(command.date(), time.getStartAt());

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(ThemeNotFoundException::new);

        try {
            return reservationRepository.save(
                    Reservation.of(command.name(), command.date(), time, theme)
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException();
        }
    }

    private void validateDoNotHaveWaiting(LocalDate date, Long timeId, Long themeId) {
        if (reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationSlotHasWaitingException();
        }
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(TimeNotFoundException::new);
    }

    public List<ReservationWithStatusResult> findReservationsByName(String name) {
        return reservationRepository.findAllByName(name);
    }

    public List<Reservation> findReservations() {
        return reservationRepository.findAll();
    }

    public List<PopularThemeResult> findPopularThemes(int period, int limit) {
        int oneDayDifference = 1;

        LocalDate to = LocalDate.now(clock).minusDays(oneDayDifference);
        LocalDate from = to.minusDays(period).plusDays(oneDayDifference);

        return reservationRepository.findPopularThemes(from, to, limit);
    }

    @Transactional
    public void updateReservation(ReservationUpdateCommand command, Long id, String name) {
        Reservation original = getReservation(id);

        validateReservationOwnership(original, name);
        expiryValidator.validate(
                original.getDate(),
                original.getReservationTime().getStartAt()
        );

        Reservation updated = updateField(command, original);

        expiryValidator.validate(
                updated.getDate(),
                updated.getReservationTime().getStartAt()
        );
        if (original.getDate().equals(updated.getDate())
                && original.getReservationTime().equals(updated.getReservationTime())) {
            return;
        }

        if (reservationRepository.existByDateAndTimeIdAndThemeIdExceptId(
                updated.getDate(),
                updated.getReservationTime().getId(),
                updated.getTheme().getId(),
                updated.getId()
        )) {
            throw new DuplicateReservationException();
        }

        validateDoNotHaveWaiting(
                updated.getDate(),
                updated.getReservationTime().getId(),
                updated.getTheme().getId()
        );

        try {
            reservationRepository.update(updated);

            reservationWaitingRepository.findFirstByReservationDateAndTimeIdAndThemeIdForUpdate(
                    original.getDate(), original.getReservationTime().getId(), original.getTheme().getId()
            ).ifPresent(this::promoteFirstWaitingForSameSlotToReservation);
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException();
        }
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findByIdForUpdate(id)
                .orElseThrow(ReservationNotFoundException::new);
    }

    private void validateReservationOwnership(Reservation reservation, String userName) {
        if (!reservation.hasSameName(userName)) {
            throw new AuthorizationException();
        }
    }

    private void promoteFirstWaitingForSameSlotToReservation(ReservationWaiting waiting) {
        int affectedRow = reservationWaitingRepository.deleteById(waiting.getId());
        int nonAffected = 0;

        if (affectedRow == nonAffected) {
            throw new ReservationWaitingNotFoundException();
        }

        try {
            reservationRepository.save(
                    Reservation.of(waiting.getName(), waiting.getDate(), waiting.getTime(), waiting.getTheme())
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException();
        }
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
    public void deleteReservationById(Long id, String name) {
        Reservation reservation = getReservation(id);

        validateReservationOwnership(reservation, name);
        expiryValidator.validate(
                reservation.getDate(),
                reservation.getReservationTime().getStartAt()
        );

        deleteReservation(reservation);
    }

    @Transactional
    public void deleteReservationById(Long id) {
        Reservation reservation = getReservation(id);
        deleteReservation(reservation);
    }

    private void deleteReservation(Reservation reservation) {
        int affectedRow = reservationRepository.deleteById(reservation.getId());
        int nonAffected = 0;

        if (affectedRow == nonAffected) {
            throw new ReservationNotFoundException();
        }

        reservationWaitingRepository.findFirstByReservationDateAndTimeIdAndThemeIdForUpdate(
                reservation.getDate(), reservation.getReservationTime().getId(), reservation.getTheme().getId()
        ).ifPresent(this::promoteFirstWaitingForSameSlotToReservation);
    }
}
