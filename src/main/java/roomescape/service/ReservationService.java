package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
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
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public List<Reservation> findReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    public List<Reservation> findUserReservations(String name, int page, int size) {
         return reservationRepository.findUserReservations(name, page, size);
    }

    @Transactional
    public Reservation createReservation(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND));

        Reservation reservation = Reservation.create(name, new Schedule(date, time, theme), LocalDateTime.now(clock));
        checkDuplicated(reservation);
        return save(reservation);
    }

    @Transactional
    public Reservation updateReservation(long id, String name, LocalDate date, long timeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Reservation updated = reservation.changeSchedule(date, time, name, LocalDateTime.now(clock));
        checkDuplicated(updated);
        update(updated);
        return updated;
    }

    @Transactional
    public void deleteUserReservation(long id, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.checkCancellable(name, LocalDateTime.now(clock));
        reservationRepository.delete(reservation);
    }

    private void checkDuplicated(Reservation reservation) {
        Schedule schedule = reservation.getSchedule();
        boolean duplicated = reservationRepository.findBySchedule(schedule)
                .filter(found -> !reservation.isSameReservation(found))
                .isPresent();

        if (duplicated) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private Reservation save(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DuplicateKeyException e) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void update(Reservation reservation) {
        try {
            reservationRepository.update(reservation);
        } catch (DuplicateKeyException e) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
