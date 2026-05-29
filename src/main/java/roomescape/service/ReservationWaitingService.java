package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingDao reservationWaitingDao;
    private final ReservationDao reservationDao;
    private final ReservationService reservationService;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao,
                                     ReservationDao reservationDao,
                                     ReservationService reservationService,
                                     ReservationTimeDao reservationTimeDao,
                                     ThemeDao themeDao,
                                     Clock clock) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationDao = reservationDao;
        this.reservationService = reservationService;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.clock = clock;
    }

    @Transactional
    public Reservation saveWaiting(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = validateReservationTime(timeId);
        Theme theme = validateTheme(themeId);
        if (!reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            return reservationService.save(name, date, timeId, themeId);
        }
        if (reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        if (reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        try {
            ReservationWaiting waiting = reservationWaitingDao.saveWaiting(reservation);
            return waiting.reservation();
        } catch (DuplicateKeyException e) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
    }

    @Transactional
    public void deleteWaiting(long id) {
        reservationWaitingDao.findByWaitingId(id).ifPresent(waiting -> {
            waiting.reservation().validateCancellable(LocalDateTime.now(clock));
            reservationWaitingDao.deleteWaiting(id);
        });
    }

    @Transactional(readOnly = true)
    public List<ReservationWaiting> findAllWaitingByName(String username) {
        return reservationWaitingDao.findAllWaitingByName(username);
    }

    private ReservationTime validateReservationTime(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme validateTheme(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }
}
