package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;

@Service
public class ReservationWaitingService {
    private final ReservationDao reservationDao;
    private final ReservationService reservationService;
    private final Clock clock;

    public ReservationWaitingService(ReservationDao reservationDao,
                                     ReservationService reservationService,
                                     Clock clock) {
        this.reservationDao = reservationDao;
        this.reservationService = reservationService;
        this.clock = clock;
    }

    @Transactional
    public Reservation saveWaiting(String name, LocalDate date, long timeId, long themeId) {
        if (!reservationService.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            return reservationService.save(name, date, timeId, themeId);
        }
        if (reservationService.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        if (reservationDao.existsWaitingByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
        ReservationTime time = reservationService.getTime(timeId);
        Theme theme = reservationService.getTheme(themeId);
        Reservation waiting = new Reservation(name, date, LocalDateTime.now(clock), time, theme, ReservationStatus.WAITING);
        try {
            return reservationDao.save(waiting);
        } catch (DuplicateKeyException e) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
    }

    @Transactional
    public void deleteWaiting(long id) {
        reservationDao.findWaitingById(id).ifPresent(reservation -> {
            reservation.validateCancellable(LocalDateTime.now(clock));
            reservationDao.delete(id);
        });
    }

    @Transactional(readOnly = true)
    public List<ReservationWaiting> findAllWaitingByName(String username) {
        return reservationDao.findAllWaitingByName(username);
    }
}