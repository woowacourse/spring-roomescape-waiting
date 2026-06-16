package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeRepository;
import roomescape.dao.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
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
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationDao reservationDao, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, Clock clock) {
        this.reservationDao = reservationDao;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
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
        try {
            return reservationDao.save(reservation);
        } catch (DuplicateKeyException e) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
    }

    @Transactional
    public Reservation update(long id, LocalDate date, long timeId) {
        Reservation reservation = reservationDao.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
        LocalDateTime now = LocalDateTime.now(clock);
        reservation.validateCancellable(now);
        ReservationTime time = validateReservationTime(timeId);
        Reservation updated = reservation.withUpdated(date, time, now);
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, reservation.getTheme().getId())) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        Reservation result = reservationDao.update(updated);
        approveFirstWaitingIfExists(reservation, now);
        return result;
    }

    @Transactional
    public void delete(long id) {
        Reservation reservation = reservationDao.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
        LocalDateTime now = LocalDateTime.now(clock);
        reservation.validateCancellable(now);
        reservationDao.delete(id);
        approveFirstWaitingIfExists(reservation, now);
    }

    private void approveFirstWaitingIfExists(Reservation slot, LocalDateTime now) {
        if (LocalDateTime.of(slot.getDate(), slot.getTime().getStartAt()).isBefore(now)) {
            return;
        }
        reservationDao.findFirstWaitingByDateAndTimeIdAndThemeIdForUpdate(
                        slot.getDate(), slot.getTime().getId(), slot.getTheme().getId())
                .ifPresent(waiting -> reservationDao.updateStatus(waiting.getId(), ReservationStatus.CONFIRMED));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByName(String username) {
        return reservationDao.findByName(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Transactional(readOnly = true)
    public boolean existsByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String name) {
        return reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name);
    }

    @Transactional(readOnly = true)
    public ReservationTime getTime(long timeId) {
        return validateReservationTime(timeId);
    }

    @Transactional(readOnly = true)
    public Theme getTheme(long themeId) {
        return validateTheme(themeId);
    }

    @Transactional(readOnly = true)
    public Page<Reservation> findAllWithCount(int page, int size) {
        List<Reservation> reservations = reservationDao.findAll(page, size);
        long totalCount = reservationDao.count();
        return new Page<>(reservations, totalCount);
    }

    private ReservationTime validateReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme validateTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }
}
