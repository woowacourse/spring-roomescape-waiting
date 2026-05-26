package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.Page;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, Clock clock) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.clock = clock;
    }

    @Transactional
    public Reservation save(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = validateReservationTime(timeId);
        Theme theme = validateTheme(themeId);
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        return reservationDao.save(reservation);
    }

    @Transactional
    public Reservation update(long id, LocalDate date, long timeId) {
        Reservation reservation = reservationDao.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
        ReservationTime time = validateReservationTime(timeId);
        Reservation updated = reservation.withUpdated(date, time, LocalDateTime.now(clock));
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, reservation.getTheme().getId())) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        return reservationDao.update(updated);
    }

    @Transactional
    public void delete(long id) {
        reservationDao.findById(id).ifPresent(reservation -> {
            reservation.validateCancellable(LocalDateTime.now(clock));
            reservationDao.delete(id);
        });
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByName(String username) {
        return reservationDao.findByName(username);
    }

    @Transactional(readOnly = true)
    public Page<Reservation> findAllWithCount(int page, int size) {
        List<Reservation> reservations = reservationDao.findAll(page, size);
        long totalCount = reservationDao.count();
        return new Page<>(reservations, totalCount);
    }

    @Transactional
    public Reservation saveWaiting(String name, LocalDate date, long timeId, long themeId) {
        if (!reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            return save(name, date, timeId, themeId);
        }
        ReservationTime time = validateReservationTime(timeId);
        Theme theme = validateTheme(themeId);
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        return reservationDao.saveWaiting(reservation);
    }

    private ReservationTime validateReservationTime(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme validateTheme(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }

    @Transactional
    public void deleteWaiting(long id) {
        reservationDao.findByWaitingId(id).ifPresent(reservation -> {
            reservation.validateCancellable(LocalDateTime.now(clock));
            reservationDao.deleteWaiting(id);
        });
    }
}
