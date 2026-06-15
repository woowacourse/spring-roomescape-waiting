package roomescape.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.UserReservation;
import roomescape.service.event.ReservationSlotReleasedEvent;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final UserReservationRepository userReservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationService(
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository,
            UserReservationRepository userReservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            Clock clock,
            ApplicationEventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.userReservationRepository = userReservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    public List<Reservation> findReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    public List<UserReservation> findUserReservations(String name, int page, int size) {
        return userReservationRepository.findByName(name, page, size);
    }

    @Transactional
    public Reservation createReservation(String name, LocalDate date, long timeId, long themeId) {
        checkWaitingExists(date, timeId, themeId);

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND));

        Reservation reservation = Reservation.create(name, date, time, theme, LocalDateTime.now(clock));
        checkDuplicated(reservation);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateReservation(long id, String name, LocalDate date, long timeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Reservation updated = reservation.changeSchedule(date, time, name, LocalDateTime.now(clock));
        checkDuplicated(updated);
        reservationRepository.update(updated);

        eventPublisher.publishEvent(
                new ReservationSlotReleasedEvent(id, reservation.getDate(), reservation.getTime().getId(),
                        reservation.getTheme().getId()));

        return updated;
    }

    @Transactional
    public void deleteUserReservation(long id, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.checkCancellable(name, LocalDateTime.now(clock));
        reservationRepository.delete(reservation);

        eventPublisher.publishEvent(
                new ReservationSlotReleasedEvent(id, reservation.getDate(), reservation.getTime().getId(),
                        reservation.getTheme().getId()));
    }

    private void checkDuplicated(Reservation reservation) {
        boolean duplicated = reservationRepository.findBySchedule(
                        reservation.getDate(),
                        reservation.getTime().getId(),
                        reservation.getTheme().getId()
                )
                .filter(found -> !reservation.isSameReservation(found))
                .isPresent();

        if (duplicated) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void checkWaitingExists(LocalDate date, long timeId, long themeId) {
        boolean waitingExists = waitingRepository.findFirstBySchedule(date, timeId, themeId).isPresent();

        if (waitingExists) {
            throw new BusinessConflictException(ErrorCode.RESERVATION_NOT_ALLOWED_WITH_WAITING);
        }
    }
}
